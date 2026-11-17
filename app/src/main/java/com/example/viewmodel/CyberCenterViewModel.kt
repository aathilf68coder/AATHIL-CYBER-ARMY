package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CyberRepository
import com.example.model.AttachedImageItem
import com.example.model.CustomerRegistration
import com.example.model.CyberServiceCatalog
import com.example.model.CyberServiceItem
import com.example.model.SecurityServiceOrder
import com.example.model.TerminalLogEntry
import com.example.model.UiState
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class RegistrationFormState(
    val customerName: String = "",
    val mobileNumber: String = "",
    val authorizedTarget: String = "",
    val instagramUsername: String = "",
    val authorizationDocName: String = "",
    val authorizationDocUri: String = "",
    val referenceImages: List<AttachedImageItem> = emptyList(),
    val authorizationConfirmed: Boolean = false,
    val isSaving: Boolean = false,
    val savedToCloud: Boolean = false,
    val validationError: String? = null
) {
    val isCompleteForOrder: Boolean
        get() = customerName.trim().length >= 2 &&
            mobileNumber.trim().length >= 6 &&
            authorizedTarget.trim().length >= 2 &&
            authorizationDocName.trim().isNotEmpty() &&
            authorizationConfirmed
}

data class CyberSessionUiState(
    val remainingSeconds: Int = DEFAULT_SESSION_SECONDS,
    val totalSeconds: Int = DEFAULT_SESSION_SECONDS,
    val sessionToken: String = generateSessionToken(),
    val isSessionExpired: Boolean = false,
    val isBootLoading: Boolean = true,
    val bootProgress: Float = 0f,
    val bootStatusLine: String = "ESTABLISHING ENCRYPTED TLS TUNNEL..."
) {
    val formattedCountdown: String
        get() {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return String.format(Locale.US, "%02d:%02d", mins, secs)
        }

    companion object {
        const val DEFAULT_SESSION_SECONDS = 15 * 60
        fun generateSessionToken(): String {
            val raw = UUID.randomUUID().toString().replace("-", "").uppercase(Locale.US)
            return "AEG-${raw.substring(0, 4)}-${raw.substring(4, 8)}"
        }
    }
}

class CyberCenterViewModel(
    private val repository: CyberRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _sessionState = MutableStateFlow(CyberSessionUiState())
    val sessionState: StateFlow<CyberSessionUiState> = _sessionState.asStateFlow()

    private val _formState = MutableStateFlow(RegistrationFormState())
    val formState: StateFlow<RegistrationFormState> = _formState.asStateFlow()

    private val _selectedService = MutableStateFlow<CyberServiceItem?>(null)
    val selectedService: StateFlow<CyberServiceItem?> = _selectedService.asStateFlow()

    private val _latestConfirmedOrder = MutableStateFlow<SecurityServiceOrder?>(null)
    val latestConfirmedOrder: StateFlow<SecurityServiceOrder?> = _latestConfirmedOrder.asStateFlow()

    private val _isConfirmingOrder = MutableStateFlow(false)
    val isConfirmingOrder: StateFlow<Boolean> = _isConfirmingOrder.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _errorBanner = MutableStateFlow<String?>(null)
    val errorBanner: StateFlow<String?> = _errorBanner.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<TerminalLogEntry>>(
        listOf(
            TerminalLogEntry(nowStamp(), "INIT", "Aegis Cyber encrypted portal initialized."),
            TerminalLogEntry(nowStamp(), "AUTH", "Zero-trust identity verified. Awaiting scope registration.")
        )
    )
    val terminalLogs: StateFlow<List<TerminalLogEntry>> = _terminalLogs.asStateFlow()

    val ordersState: StateFlow<UiState<List<SecurityServiceOrder>>> =
        repository.observeOrders(currentUserId)
            .map<List<SecurityServiceOrder>, UiState<List<SecurityServiceOrder>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing orders", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load order records"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    private var countdownJob: Job? = null
    private var hasHydratedFromCloud = false

    init {
        runBootSequence()
        startSessionCountdown()
        observeSavedRegistration()
    }

    private fun runBootSequence() {
        viewModelScope.launch {
            val steps = listOf(
                0.22f to "VERIFYING CRYPTOGRAPHIC SESSION KEYS...",
                0.48f to "LOADING OWASP & ANDROID AUDIT MODULES...",
                0.76f to "CONNECTING SECURE FIRESTORE VAULT...",
                1.00f to "AEGIS CYBER DEFENSE CONSOLE READY."
            )
            for ((progress, line) in steps) {
                _sessionState.update {
                    it.copy(bootProgress = progress, bootStatusLine = line)
                }
                delay(320L)
            }
            _sessionState.update { it.copy(isBootLoading = false) }
            appendLog("SYS", "Session ${_sessionState.value.sessionToken} active (15:00 TTL).")
        }
    }

    private fun startSessionCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                var expiredNow = false
                _sessionState.update { current ->
                    if (current.isSessionExpired) {
                        current
                    } else {
                        val next = (current.remainingSeconds - 1).coerceAtLeast(0)
                        if (next == 0) expiredNow = true
                        current.copy(
                            remainingSeconds = next,
                            isSessionExpired = next == 0
                        )
                    }
                }
                if (expiredNow) {
                    appendLog("WARN", "Secure session expired. Cryptographic token revoked.")
                    break
                }
            }
        }
    }

    fun requestNewSession() {
        val newToken = CyberSessionUiState.generateSessionToken()
        _sessionState.update {
            it.copy(
                remainingSeconds = CyberSessionUiState.DEFAULT_SESSION_SECONDS,
                totalSeconds = CyberSessionUiState.DEFAULT_SESSION_SECONDS,
                sessionToken = newToken,
                isSessionExpired = false
            )
        }
        appendLog("AUTH", "New encrypted session issued: $newToken (15:00 TTL).")
        startSessionCountdown()
    }

    private fun observeSavedRegistration() {
        viewModelScope.launch {
            repository.observeActiveRegistration(currentUserId)
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    Log.w(TAG, "Error observing saved registration", firestoreError ?: error)
                }
                .collect { saved: CustomerRegistration? ->
                    if (saved != null && !hasHydratedFromCloud) {
                        hasHydratedFromCloud = true
                        _formState.update { current ->
                            current.copy(
                                customerName = current.customerName.ifBlank { saved.customerName },
                                mobileNumber = current.mobileNumber.ifBlank { saved.mobileNumber },
                                authorizedTarget = current.authorizedTarget.ifBlank { saved.authorizedTarget },
                                instagramUsername = current.instagramUsername.ifBlank { saved.instagramUsername },
                                authorizationDocName = current.authorizationDocName.ifBlank { saved.authorizationDocName },
                                authorizationDocUri = current.authorizationDocUri.ifBlank { saved.authorizationDocUri },
                                referenceImages = current.referenceImages.ifEmpty {
                                    saved.referenceImageNames.map { name ->
                                        AttachedImageItem(uriString = "", displayName = name)
                                    }
                                },
                                authorizationConfirmed = current.authorizationConfirmed || saved.authorizationConfirmed,
                                savedToCloud = true
                            )
                        }
                        appendLog("VAULT", "Loaded encrypted registration profile for ${saved.customerName}.")
                    }
                }
        }
    }

    fun updateCustomerName(value: String) {
        _formState.update { it.copy(customerName = value, savedToCloud = false, validationError = null) }
    }

    fun updateMobileNumber(value: String) {
        _formState.update { it.copy(mobileNumber = value, savedToCloud = false, validationError = null) }
    }

    fun updateAuthorizedTarget(value: String) {
        _formState.update { it.copy(authorizedTarget = value, savedToCloud = false, validationError = null) }
    }

    fun updateInstagramUsername(value: String) {
        _formState.update { it.copy(instagramUsername = value, savedToCloud = false, validationError = null) }
    }

    fun setAuthorizationDocument(name: String, uriString: String) {
        _formState.update {
            it.copy(
                authorizationDocName = name,
                authorizationDocUri = uriString,
                savedToCloud = false,
                validationError = null
            )
        }
        appendLog("DOC", "Authorization document attached: $name")
    }

    fun addReferenceImages(items: List<AttachedImageItem>) {
        if (items.isEmpty()) return
        _formState.update { current ->
            val merged = (current.referenceImages + items)
                .distinctBy { it.displayName + it.uriString }
                .take(5)
            current.copy(referenceImages = merged, savedToCloud = false, validationError = null)
        }
        appendLog("IMG", "Attached ${items.size} reference image(s) (max 5).")
    }

    fun removeReferenceImage(index: Int) {
        _formState.update { current ->
            val updated = current.referenceImages.toMutableList().apply {
                if (index in indices) removeAt(index)
            }
            current.copy(referenceImages = updated, savedToCloud = false)
        }
    }

    fun setAuthorizationConfirmed(confirmed: Boolean) {
        _formState.update {
            it.copy(authorizationConfirmed = confirmed, savedToCloud = false, validationError = null)
        }
    }

    fun saveRegistration(onSavedSuccess: () -> Unit = {}) {
        val current = _formState.value
        val validationError = validateForm(current)
        if (validationError != null) {
            _formState.update { it.copy(validationError = validationError) }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true, validationError = null) }
            val result = repository.saveRegistration(
                userId = currentUserId,
                customerName = current.customerName,
                mobileNumber = current.mobileNumber,
                authorizedTarget = current.authorizedTarget,
                instagramUsername = current.instagramUsername,
                authorizationDocName = current.authorizationDocName,
                authorizationDocUri = current.authorizationDocUri,
                referenceImageNames = current.referenceImages.map { it.displayName },
                authorizationConfirmed = current.authorizationConfirmed
            )
            result.fold(
                onSuccess = {
                    hasHydratedFromCloud = true
                    _formState.update { it.copy(isSaving = false, savedToCloud = true) }
                    _actionMessage.value = "Customer profile & authorization stored securely."
                    appendLog("VAULT", "Customer registration encrypted & persisted to Firestore.")
                    onSavedSuccess()
                },
                onFailure = { err ->
                    _formState.update { it.copy(isSaving = false) }
                    _errorBanner.value = err.localizedMessage ?: "Failed to save customer registration."
                    appendLog("ERR", "Registration save error: ${err.localizedMessage}")
                }
            )
        }
    }

    fun selectService(service: CyberServiceItem) {
        _selectedService.value = service
        _latestConfirmedOrder.value = null
        appendLog("SVC", "Selected module: ${service.code} (${service.title})")
    }

    fun confirmOrder() {
        val service = _selectedService.value
        if (service == null) {
            _errorBanner.value = "Please select a cyber security service first."
            return
        }
        val form = _formState.value
        val validationError = validateForm(form)
        if (validationError != null) {
            _formState.update { it.copy(validationError = validationError) }
            _errorBanner.value = validationError
            return
        }

        viewModelScope.launch {
            _isConfirmingOrder.value = true
            // Ensure latest registration profile is persisted alongside the order
            val saveRes = repository.saveRegistration(
                userId = currentUserId,
                customerName = form.customerName,
                mobileNumber = form.mobileNumber,
                authorizedTarget = form.authorizedTarget,
                instagramUsername = form.instagramUsername,
                authorizationDocName = form.authorizationDocName,
                authorizationDocUri = form.authorizationDocUri,
                referenceImageNames = form.referenceImages.map { it.displayName },
                authorizationConfirmed = form.authorizationConfirmed
            )
            if (saveRes.isFailure) {
                _isConfirmingOrder.value = false
                _errorBanner.value = saveRes.exceptionOrNull()?.localizedMessage
                    ?: "Could not verify customer registration before order confirmation."
                return@launch
            }

            val orderCode = "AEG-${UUID.randomUUID().toString().take(6).uppercase(Locale.US)}"
            val orderRes = repository.confirmOrder(
                userId = currentUserId,
                orderCode = orderCode,
                sessionToken = _sessionState.value.sessionToken,
                serviceId = service.id,
                serviceTitle = service.title,
                serviceCategory = service.category,
                customerName = form.customerName,
                mobileNumber = form.mobileNumber,
                authorizedTarget = form.authorizedTarget,
                instagramUsername = form.instagramUsername,
                authorizationDocName = form.authorizationDocName,
                referenceImagesCount = form.referenceImages.size
            )
            _isConfirmingOrder.value = false
            orderRes.fold(
                onSuccess = { confirmed ->
                    _latestConfirmedOrder.value = confirmed
                    _formState.update { it.copy(savedToCloud = true) }
                    _actionMessage.value = "Order $orderCode confirmed! Payment instructions unlocked."
                    appendLog("ORDER", "Confirmed $orderCode for ${service.title}. Contact @aathil_407.")
                },
                onFailure = { err ->
                    _errorBanner.value = err.localizedMessage ?: "Failed to confirm order."
                    appendLog("ERR", "Order confirmation failed: ${err.localizedMessage}")
                }
            )
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            val result = repository.deleteOrder(currentUserId, orderId)
            result.fold(
                onSuccess = {
                    if (_latestConfirmedOrder.value?.id == orderId) {
                        _latestConfirmedOrder.value = null
                    }
                    _actionMessage.value = "Order record removed."
                    appendLog("DEL", "Removed archived order record $orderId.")
                },
                onFailure = { err ->
                    _errorBanner.value = err.localizedMessage ?: "Unable to remove order."
                }
            )
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun clearErrorBanner() {
        _errorBanner.value = null
    }

    fun getServiceById(serviceId: String): CyberServiceItem? =
        CyberServiceCatalog.services.firstOrNull { it.id == serviceId }

    private fun validateForm(state: RegistrationFormState): String? {
        if (state.customerName.trim().length < 2) {
            return "Enter your full name (at least 2 characters)."
        }
        if (state.mobileNumber.trim().length < 6) {
            return "Enter a valid mobile number (at least 6 digits)."
        }
        if (state.authorizedTarget.trim().length < 2) {
            return "Specify the authorized target website or application."
        }
        if (state.authorizationDocName.trim().isEmpty()) {
            return "Upload an Authorization Document to verify legal scope."
        }
        if (!state.authorizationConfirmed) {
            return "Confirm the explicit written authorization & compliance attestation."
        }
        return null
    }

    private fun appendLog(level: String, message: String) {
        _terminalLogs.update { current ->
            (listOf(TerminalLogEntry(nowStamp(), level, message)) + current).take(12)
        }
    }

    private companion object {
        const val TAG = "CyberCenterVM"

        fun nowStamp(): String =
            SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
    }
}

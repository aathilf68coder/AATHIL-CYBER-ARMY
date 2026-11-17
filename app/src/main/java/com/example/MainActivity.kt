package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.attemptAutoSignIn
import com.example.auth.authStateFlow
import com.example.auth.signOutUser
import com.example.data.CyberRepository
import com.example.ui.components.CyberBootLoadingScreen
import com.example.ui.components.MatrixBackground
import com.example.ui.components.SecurityCountdownBanner
import com.example.ui.components.SessionExpiredOverlay
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ContactOrdersScreen
import com.example.ui.screens.OrderConfirmationScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.ServicesScreen
import com.example.ui.theme.AegisCyberTheme
import com.example.ui.theme.AshMuted
import com.example.ui.theme.BloodRed
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.CrimsonSurfaceElevated
import com.example.ui.theme.DeepCrimsonContainer
import com.example.ui.theme.GhostWhite
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.PitchObsidian
import com.example.ui.theme.ScarletGlow
import com.example.ui.theme.TerminalBlack
import com.example.viewmodel.CyberCenterViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private enum class CyberDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val testTag: String
) {
    REGISTER("register", R.string.nav_register, Icons.Default.Assignment, "nav_register"),
    SERVICES("services", R.string.nav_services, Icons.Default.Security, "nav_services"),
    CONFIRM("confirm", R.string.nav_confirm, Icons.Default.CheckCircle, "nav_confirm"),
    CONTACT("contact", R.string.nav_contact, Icons.Default.ContactSupport, "nav_contact")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            AegisCyberTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            kotlinx.coroutines.delay(350L)
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            CyberCenterHost(
                currentUserId = user.uid,
                userEmail = user.email,
                onSignOut = {
                    signOutUser(
                        credentialManager = credentialManager,
                        scope = scope
                    )
                }
            )
        }
    }
}

@Composable
private fun CyberCenterHost(
    currentUserId: String,
    userEmail: String?,
    onSignOut: () -> Unit
) {
    val viewModel: CyberCenterViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                CyberCenterViewModel(
                    repository = CyberRepository(db),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val selectedService by viewModel.selectedService.collectAsStateWithLifecycle()
    val latestConfirmedOrder by viewModel.latestConfirmedOrder.collectAsStateWithLifecycle()
    val isConfirmingOrder by viewModel.isConfirmingOrder.collectAsStateWithLifecycle()
    val ordersState by viewModel.ordersState.collectAsStateWithLifecycle()
    val terminalLogs by viewModel.terminalLogs.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val errorBanner by viewModel.errorBanner.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(CyberDestination.REGISTER) }
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentTab) {
        scrollState.animateScrollTo(0)
    }

    LaunchedEffect(actionMessage) {
        val msg = actionMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessage()
        }
    }

    LaunchedEffect(errorBanner) {
        val err = errorBanner
        if (!err.isNullOrBlank()) {
            snackbarHostState.showSnackbar(err)
            viewModel.clearErrorBanner()
        }
    }

    if (currentTab != CyberDestination.REGISTER && !sessionState.isBootLoading && !sessionState.isSessionExpired) {
        BackHandler {
            currentTab = when (currentTab) {
                CyberDestination.CONFIRM -> CyberDestination.SERVICES
                CyberDestination.SERVICES -> CyberDestination.REGISTER
                CyberDestination.CONTACT -> CyberDestination.REGISTER
                CyberDestination.REGISTER -> CyberDestination.REGISTER
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(PitchObsidian)) {
        // Main authenticated UI
        AnimatedVisibility(
            visible = !sessionState.isBootLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 600.dp

                MatrixBackground(intensity = 0.72f)

                Scaffold(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = CrimsonSurfaceElevated,
                                contentColor = GhostWhite,
                                actionColor = ScarletGlow,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.border(
                                    1.dp,
                                    CrimsonBorder,
                                    RoundedCornerShape(10.dp)
                                )
                            )
                        }
                    },
                    bottomBar = {
                        if (!useNavigationRail) {
                            NavigationBar(
                                containerColor = TerminalBlack.copy(alpha = 0.96f),
                                contentColor = GhostWhite,
                                windowInsets = WindowInsets.navigationBars,
                                tonalElevation = 4.dp
                            ) {
                                CyberDestination.entries.forEach { dest ->
                                    val selected = currentTab == dest
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { currentTab = dest },
                                        icon = {
                                            Icon(
                                                imageVector = dest.icon,
                                                contentDescription = stringResource(dest.labelRes)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = stringResource(dest.labelRes),
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = NeonCrimson,
                                            selectedTextColor = ScarletGlow,
                                            indicatorColor = DeepCrimsonContainer,
                                            unselectedIconColor = AshMuted,
                                            unselectedTextColor = AshMuted
                                        ),
                                        modifier = Modifier.testTag(dest.testTag)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavigationRail) {
                            NavigationRail(
                                containerColor = TerminalBlack.copy(alpha = 0.95f),
                                contentColor = GhostWhite,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .windowInsetsPadding(WindowInsets.statusBars)
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                            ) {
                                Spacer(Modifier.height(12.dp))
                                CyberDestination.entries.forEach { dest ->
                                    val selected = currentTab == dest
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { currentTab = dest },
                                        icon = {
                                            Icon(
                                                imageVector = dest.icon,
                                                contentDescription = stringResource(dest.labelRes)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = stringResource(dest.labelRes),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = NeonCrimson,
                                            selectedTextColor = ScarletGlow,
                                            indicatorColor = DeepCrimsonContainer,
                                            unselectedIconColor = AshMuted,
                                            unselectedTextColor = AshMuted
                                        ),
                                        modifier = Modifier.testTag(dest.testTag)
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .windowInsetsPadding(WindowInsets.statusBars)
                        ) {
                            // Top Brand Bar + Live Security Countdown Timer
                            Surface(
                                color = TerminalBlack.copy(alpha = 0.92f),
                                border = BorderStroke(1.dp, CrimsonBorder)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "AEGIS CYBER",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = GhostWhite
                                            )
                                            Text(
                                                text = stringResource(R.string.brand_subtitle),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ScarletGlow
                                            )
                                        }
                                        Text(
                                            text = stringResource(R.string.official_instagram_handle),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = NeonCrimson
                                        )
                                    }
                                    SecurityCountdownBanner(
                                        formattedTime = sessionState.formattedCountdown,
                                        remainingSeconds = sessionState.remainingSeconds,
                                        sessionToken = sessionState.sessionToken
                                    )
                                }
                            }

                            // Scrollable Adaptive Workspace Content
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Column(
                                    modifier = Modifier.widthIn(max = 640.dp)
                                ) {
                                    AnimatedContent(
                                        targetState = currentTab,
                                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                                        label = "tabTransition"
                                    ) { tab ->
                                        when (tab) {
                                            CyberDestination.REGISTER -> {
                                                RegistrationScreen(
                                                    formState = formState,
                                                    terminalLogs = terminalLogs,
                                                    onNameChange = viewModel::updateCustomerName,
                                                    onMobileChange = viewModel::updateMobileNumber,
                                                    onTargetChange = viewModel::updateAuthorizedTarget,
                                                    onInstagramChange = viewModel::updateInstagramUsername,
                                                    onDocumentSelected = viewModel::setAuthorizationDocument,
                                                    onReferenceImagesSelected = viewModel::addReferenceImages,
                                                    onRemoveReferenceImage = viewModel::removeReferenceImage,
                                                    onAuthorizationConfirmedChange = viewModel::setAuthorizationConfirmed,
                                                    onSaveAndContinue = {
                                                        viewModel.saveRegistration {
                                                            currentTab = CyberDestination.SERVICES
                                                        }
                                                    }
                                                )
                                            }
                                            CyberDestination.SERVICES -> {
                                                ServicesScreen(
                                                    selectedServiceId = selectedService?.id,
                                                    onSelectService = { service ->
                                                        viewModel.selectService(service)
                                                        currentTab = CyberDestination.CONFIRM
                                                    }
                                                )
                                            }
                                            CyberDestination.CONFIRM -> {
                                                OrderConfirmationScreen(
                                                    selectedService = selectedService,
                                                    formState = formState,
                                                    sessionToken = sessionState.sessionToken,
                                                    isConfirming = isConfirmingOrder,
                                                    confirmedOrder = latestConfirmedOrder,
                                                    onEditRegistration = {
                                                        currentTab = CyberDestination.REGISTER
                                                    },
                                                    onChooseService = {
                                                        currentTab = CyberDestination.SERVICES
                                                    },
                                                    onConfirmOrder = viewModel::confirmOrder
                                                )
                                            }
                                            CyberDestination.CONTACT -> {
                                                ContactOrdersScreen(
                                                    ordersState = ordersState,
                                                    userEmail = userEmail,
                                                    onDeleteOrder = viewModel::deleteOrder,
                                                    onSignOut = onSignOut
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Professional Terminal Boot Loading Screen
        AnimatedVisibility(
            visible = sessionState.isBootLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CyberBootLoadingScreen(
                progress = sessionState.bootProgress,
                statusLine = sessionState.bootStatusLine,
                sessionToken = sessionState.sessionToken
            )
        }

        // Session Expired Lock Overlay when countdown reaches 00:00
        AnimatedVisibility(
            visible = !sessionState.isBootLoading && sessionState.isSessionExpired,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SessionExpiredOverlay(
                expiredToken = sessionState.sessionToken,
                onRequestNewSession = viewModel::requestNewSession
            )
        }
    }
}

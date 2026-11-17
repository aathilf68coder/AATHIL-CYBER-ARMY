package com.example.data

import android.content.Context
import com.example.R
import com.example.model.CustomerRegistration
import com.example.model.SecurityServiceOrder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CyberRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireAuthorizedUserId(expectedUserId: String): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        check(uid == expectedUserId) { "Authenticated session changed; refusing cross-user access." }
        return uid
    }

    fun observeActiveRegistration(userId: String): Flow<CustomerRegistration?> {
        val docRef = db.collection("users").document(userId)
            .collection("registrations").document(ACTIVE_REGISTRATION_DOC_ID)
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        CustomerRegistration::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                }
                throw error
            }
    }

    fun observeOrders(userId: String): Flow<List<SecurityServiceOrder>> {
        val colRef = db.collection("users").document(userId).collection("orders")
        return colRef
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    SecurityServiceOrder::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                }
                throw error
            }
    }

    suspend fun saveRegistration(
        userId: String,
        customerName: String,
        mobileNumber: String,
        authorizedTarget: String,
        instagramUsername: String,
        authorizationDocName: String,
        authorizationDocUri: String,
        referenceImageNames: List<String>,
        authorizationConfirmed: Boolean
    ): Result<Unit> {
        val docPath = "users/$userId/registrations/$ACTIVE_REGISTRATION_DOC_ID"
        return try {
            val uid = requireAuthorizedUserId(userId)
            val cleanName = customerName.trim()
            val cleanMobile = mobileNumber.trim()
            val cleanTarget = authorizedTarget.trim()
            val cleanIg = instagramUsername.trim().take(60)
            val cleanDocName = authorizationDocName.trim()
            val cleanDocUri = authorizationDocUri.trim().take(500)
            val cleanImages = referenceImageNames.map { it.trim().take(200) }.take(5)

            require(cleanName.length in 2..100) { "Customer name must be between 2 and 100 characters." }
            require(cleanMobile.length in 6..30) { "Mobile number must be between 6 and 30 characters." }
            require(cleanTarget.length in 2..200) { "Authorized target must be between 2 and 200 characters." }
            require(cleanDocName.length in 1..200) { "Authorization document is required." }
            require(authorizationConfirmed) { "Explicit written authorization must be confirmed." }

            val docRef = db.collection("users").document(uid)
                .collection("registrations").document(ACTIVE_REGISTRATION_DOC_ID)

            val existingSnapshot = docRef.get().await()
            if (existingSnapshot.exists()) {
                docRef.update(
                    mapOf(
                        "customerName" to cleanName,
                        "mobileNumber" to cleanMobile,
                        "authorizedTarget" to cleanTarget,
                        "instagramUsername" to cleanIg,
                        "authorizationDocName" to cleanDocName,
                        "authorizationDocUri" to cleanDocUri,
                        "referenceImageNames" to cleanImages,
                        "authorizationConfirmed" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "customerName" to cleanName,
                        "mobileNumber" to cleanMobile,
                        "authorizedTarget" to cleanTarget,
                        "instagramUsername" to cleanIg,
                        "authorizationDocName" to cleanDocName,
                        "authorizationDocUri" to cleanDocUri,
                        "referenceImageNames" to cleanImages,
                        "authorizationConfirmed" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun confirmOrder(
        userId: String,
        orderCode: String,
        sessionToken: String,
        serviceId: String,
        serviceTitle: String,
        serviceCategory: String,
        customerName: String,
        mobileNumber: String,
        authorizedTarget: String,
        instagramUsername: String,
        authorizationDocName: String,
        referenceImagesCount: Int
    ): Result<SecurityServiceOrder> {
        var docPath = "users/$userId/orders"
        return try {
            val uid = requireAuthorizedUserId(userId)
            val cleanCode = orderCode.trim().take(32)
            val cleanSession = sessionToken.trim().take(32)
            val cleanServiceId = serviceId.trim().take(60)
            val cleanServiceTitle = serviceTitle.trim().take(120)
            val cleanCategory = serviceCategory.trim().take(60)
            val cleanName = customerName.trim()
            val cleanMobile = mobileNumber.trim()
            val cleanTarget = authorizedTarget.trim()
            val cleanIg = instagramUsername.trim().take(60)
            val cleanDocName = authorizationDocName.trim()
            val boundedCount = referenceImagesCount.coerceIn(0, 5)

            require(cleanCode.length >= 4) { "Invalid order reference code." }
            require(cleanName.length in 2..100) { "Customer name must be between 2 and 100 characters." }
            require(cleanMobile.length in 6..30) { "Mobile number must be between 6 and 30 characters." }
            require(cleanTarget.length in 2..200) { "Authorized target must be between 2 and 200 characters." }
            require(cleanDocName.length in 1..200) { "Authorization document is required before confirming." }

            val docRef = db.collection("users").document(uid).collection("orders").document()
            docPath = docRef.path

            val payload = mapOf(
                "userId" to uid,
                "orderCode" to cleanCode,
                "sessionToken" to cleanSession,
                "serviceId" to cleanServiceId,
                "serviceTitle" to cleanServiceTitle,
                "serviceCategory" to cleanCategory,
                "customerName" to cleanName,
                "mobileNumber" to cleanMobile,
                "authorizedTarget" to cleanTarget,
                "instagramUsername" to cleanIg,
                "authorizationDocName" to cleanDocName,
                "referenceImagesCount" to boundedCount,
                "status" to SecurityServiceOrder.ORDER_STATUS_CONFIRMED,
                "paymentInstructionsUnlocked" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()

            Result.success(
                SecurityServiceOrder(
                    id = docRef.id,
                    userId = uid,
                    orderCode = cleanCode,
                    sessionToken = cleanSession,
                    serviceId = cleanServiceId,
                    serviceTitle = cleanServiceTitle,
                    serviceCategory = cleanCategory,
                    customerName = cleanName,
                    mobileNumber = cleanMobile,
                    authorizedTarget = cleanTarget,
                    instagramUsername = cleanIg,
                    authorizationDocName = cleanDocName,
                    referenceImagesCount = boundedCount.toLong(),
                    status = SecurityServiceOrder.ORDER_STATUS_CONFIRMED,
                    paymentInstructionsUnlocked = true
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteOrder(userId: String, orderId: String): Result<Unit> {
        val docPath = "users/$userId/orders/$orderId"
        return try {
            val uid = requireAuthorizedUserId(userId)
            require(orderId.isNotBlank()) { "Order ID cannot be blank." }
            db.collection("users").document(uid).collection("orders").document(orderId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    private companion object {
        const val ACTIVE_REGISTRATION_DOC_ID = "active"
    }
}

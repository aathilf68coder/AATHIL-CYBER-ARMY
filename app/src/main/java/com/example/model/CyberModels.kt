package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class CustomerRegistration(
    @DocumentId val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val mobileNumber: String = "",
    val authorizedTarget: String = "",
    val instagramUsername: String = "",
    val authorizationDocName: String = "",
    val authorizationDocUri: String = "",
    val referenceImageNames: List<String> = emptyList(),
    val authorizationConfirmed: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SecurityServiceOrder(
    @DocumentId val id: String = "",
    val userId: String = "",
    val orderCode: String = "",
    val sessionToken: String = "",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val serviceCategory: String = "",
    val customerName: String = "",
    val mobileNumber: String = "",
    val authorizedTarget: String = "",
    val instagramUsername: String = "",
    val authorizationDocName: String = "",
    val referenceImagesCount: Long = 0L,
    val status: String = ORDER_STATUS_CONFIRMED,
    val paymentInstructionsUnlocked: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    companion object {
        const val ORDER_STATUS_CONFIRMED = "SCOPE_AND_AUTH_CONFIRMED"
    }
}

data class CyberServiceItem(
    val id: String,
    val code: String,
    val title: String,
    val category: String,
    val turnaround: String,
    val summary: String,
    val deliverables: List<String>
)

data class AttachedImageItem(
    val uriString: String,
    val displayName: String
)

data class TerminalLogEntry(
    val timestamp: String,
    val level: String,
    val message: String
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

object CyberServiceCatalog {
    val services: List<CyberServiceItem> = listOf(
        CyberServiceItem(
            id = "instagram_security_audit",
            code = "SVC-01 // IG-AUDIT",
            title = "Instagram Account Security Audit",
            category = "SOCIAL_SEC",
            turnaround = "24H TURNAROUND",
            summary = "Authorized hardening review for Instagram credentials, active sessions, 2FA posture, connected apps, and recovery vectors.",
            deliverables = listOf(
                "Session & OAuth token hygiene report",
                "2FA & recovery endpoint hardening checklist",
                "Phishing & impersonation risk mitigation brief"
            )
        ),
        CyberServiceItem(
            id = "facebook_security_audit",
            code = "SVC-02 // FB-AUDIT",
            title = "Facebook Account Security Audit",
            category = "SOCIAL_SEC",
            turnaround = "24H TURNAROUND",
            summary = "Comprehensive authorized inspection of Business Manager permissions, login alerts, third-party integrations, and privacy locks.",
            deliverables = listOf(
                "Business & Page role access matrix",
                "Login activity & session anomaly review",
                "Account recovery lockdown protocol"
            )
        ),
        CyberServiceItem(
            id = "android_device_audit",
            code = "SVC-03 // AND-DEVICE",
            title = "Android Device Security Audit",
            category = "MOBILE_DEFENSE",
            turnaround = "24-48H TURNAROUND",
            summary = "Authorized mobile posture assessment covering OS patch status, sideloaded package risk, network telemetry, and lock-screen security.",
            deliverables = listOf(
                "Installed application risk & signature review",
                "System hardening & Play Protect baseline",
                "Encrypted communication & DNS recommendations"
            )
        ),
        CyberServiceItem(
            id = "camera_permission_check",
            code = "SVC-04 // CAM-GUARD",
            title = "Mobile Camera Permission Security Check",
            category = "PRIVACY_SHIELD",
            turnaround = "12-24H TURNAROUND",
            summary = "Zero-intrusion guided audit of background camera access grants, indicator compliance, and suspicious foreground services.",
            deliverables = listOf(
                "Camera permission entitlement review",
                "Background sensor indicator verification",
                "Least-privilege revocation playbook"
            )
        ),
        CyberServiceItem(
            id = "gallery_privacy_audit",
            code = "SVC-05 // MEDIA-VAULT",
            title = "Gallery Privacy & Security Audit",
            category = "PRIVACY_SHIELD",
            turnaround = "12-24H TURNAROUND",
            summary = "Authorized assessment of media storage scopes, cloud album sharing links, EXIF metadata exposure, and app photo access.",
            deliverables = listOf(
                "Media & Photo Picker scope audit",
                "Shared album & EXIF metadata exposure check",
                "Private vault & backup encryption guide"
            )
        ),
        CyberServiceItem(
            id = "rat_detection_removal",
            code = "SVC-06 // RAT-PURGE",
            title = "RAT Detection & Removal",
            category = "THREAT_RESPONSE",
            turnaround = "PRIORITY 12H",
            summary = "Authorized Remote Access Trojan triage, persistence mechanism identification, rogue accessibility service cleanup, and containment.",
            deliverables = listOf(
                "Persistence & device-admin vector triage",
                "Malicious APK & C2 indicator containment",
                "Post-incident credential rotation checklist"
            )
        ),
        CyberServiceItem(
            id = "website_vuln_assessment",
            code = "SVC-07 // WEB-VAPT",
            title = "Website Vulnerability Assessment",
            category = "WEB_PENTEST",
            turnaround = "48H TURNAROUND",
            summary = "Authorized OWASP-aligned web application testing covering headers, TLS, auth flows, injection surfaces, and API exposure.",
            deliverables = listOf(
                "OWASP Top 10 vulnerability matrix",
                "TLS, CORS & security header grading",
                "Actionable remediation patches for dev teams"
            )
        )
    )
}

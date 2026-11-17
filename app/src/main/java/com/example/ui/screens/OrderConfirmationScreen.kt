package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CyberServiceItem
import com.example.model.SecurityServiceOrder
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberGlowButton
import com.example.ui.components.CyberOutlinedButton
import com.example.ui.components.OfficialInstagramCard
import com.example.ui.components.copyTextToClipboard
import com.example.ui.theme.AshMuted
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.DeepCrimsonContainer
import com.example.ui.theme.GhostWhite
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.ScarletGlow
import com.example.ui.theme.SubtleDivider
import com.example.ui.theme.TerminalBlack
import com.example.viewmodel.RegistrationFormState

@Composable
fun OrderConfirmationScreen(
    selectedService: CyberServiceItem?,
    formState: RegistrationFormState,
    sessionToken: String,
    isConfirming: Boolean,
    confirmedOrder: SecurityServiceOrder?,
    onEditRegistration: () -> Unit,
    onChooseService: () -> Unit,
    onConfirmOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CyberCard {
            Text(
                text = "03 // ORDER CONFIRMATION",
                style = MaterialTheme.typography.headlineSmall,
                color = GhostWhite
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Review your selected cyber security service, customer authorization details, and encrypted order summary.",
                style = MaterialTheme.typography.bodySmall,
                color = AshMuted
            )
        }

        // Confirmed Receipt + Unlocked Payment Instructions
        if (confirmedOrder != null) {
            CyberCard(highlighted = true) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Order Confirmed",
                        tint = NeonCrimson,
                        modifier = Modifier.size(26.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ORDER CONFIRMED // SCOPE VERIFIED",
                            style = MaterialTheme.typography.titleSmall,
                            color = ScarletGlow
                        )
                        Text(
                            text = "Reference: ${confirmedOrder.orderCode}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = GhostWhite
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                SummaryRow("Service", confirmedOrder.serviceTitle)
                SummaryRow("Target", confirmedOrder.authorizedTarget)
                SummaryRow("Client", confirmedOrder.customerName)
                SummaryRow("Mobile", confirmedOrder.mobileNumber)
                if (confirmedOrder.instagramUsername.isNotBlank()) {
                    SummaryRow("Client IG", confirmedOrder.instagramUsername)
                }
                SummaryRow("Auth Doc", confirmedOrder.authorizationDocName)
                SummaryRow("Session", confirmedOrder.sessionToken)

                Spacer(Modifier.height(12.dp))
                CyberOutlinedButton(
                    text = "Copy Order ID (${confirmedOrder.orderCode})",
                    onClick = {
                        copyTextToClipboard(context, "Order Reference", confirmedOrder.orderCode)
                    },
                    icon = Icons.Default.ContentCopy,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "copy_order_code_button"
                )
            }

            // Payment instructions unlocked AFTER scope & authorization confirmed!
            OfficialInstagramCard(
                showPaymentInstructions = true,
                orderCode = confirmedOrder.orderCode
            )
        }

        // Selected Service Card
        if (selectedService == null) {
            CyberCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ScarletGlow
                    )
                    Text(
                        text = "No service selected yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = GhostWhite
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Select one of our 7 Cyber Security Services to generate your order summary.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AshMuted
                )
                Spacer(Modifier.height(12.dp))
                CyberGlowButton(
                    text = "Browse Cyber Services",
                    onClick = onChooseService,
                    icon = Icons.Default.Security,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "browse_services_button"
                )
            }
        } else {
            CyberCard(highlighted = confirmedOrder == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SELECTED SERVICE",
                            style = MaterialTheme.typography.labelSmall,
                            color = ScarletGlow
                        )
                        Text(
                            text = selectedService.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = GhostWhite
                        )
                        Text(
                            text = "${selectedService.code} // ${selectedService.turnaround}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AshMuted
                        )
                    }
                    CyberOutlinedButton(
                        text = "Change",
                        onClick = onChooseService,
                        testTag = "change_service_button"
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = selectedService.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = AshMuted
                )
            }
        }

        // Customer Details Card
        CyberCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CUSTOMER DETAILS",
                    style = MaterialTheme.typography.titleSmall,
                    color = ScarletGlow
                )
                CyberOutlinedButton(
                    text = "Edit",
                    onClick = onEditRegistration,
                    icon = Icons.Default.Edit,
                    testTag = "edit_registration_button"
                )
            }
            Spacer(Modifier.height(10.dp))
            SummaryRow("Customer Name", formState.customerName.ifBlank { "Not provided" })
            SummaryRow("Mobile Number", formState.mobileNumber.ifBlank { "Not provided" })
            SummaryRow("Target Asset", formState.authorizedTarget.ifBlank { "Not provided" })
            SummaryRow(
                "Instagram Handle",
                formState.instagramUsername.ifBlank { "Optional (not provided)" }
            )
            SummaryRow(
                "Authorization Doc",
                formState.authorizationDocName.ifBlank { "Required — missing" }
            )
            SummaryRow(
                "Reference Images",
                if (formState.referenceImages.isEmpty()) "None" else "${formState.referenceImages.size} attached"
            )
            SummaryRow(
                "Written Consent",
                if (formState.authorizationConfirmed) "CONFIRMED" else "Pending confirmation"
            )

            if (!formState.isCompleteForOrder) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepCrimsonContainer.copy(alpha = 0.55f))
                        .border(1.dp, NeonCrimson, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = NeonCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Complete required registration fields and upload your Authorization Document before confirming.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostWhite
                    )
                }
            }
        }

        // Order Summary & Confirm Button (when not yet confirmed for this selection)
        if (selectedService != null) {
            CyberCard {
                Text(
                    text = "ORDER SUMMARY",
                    style = MaterialTheme.typography.titleSmall,
                    color = ScarletGlow
                )
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerminalBlack)
                        .border(1.dp, CrimsonBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SummaryRow("Active Session", sessionToken)
                    SummaryRow("Selected Module", selectedService.title)
                    SummaryRow("Target Scope", formState.authorizedTarget.ifBlank { "Pending" })
                    SummaryRow("Intake Channel", stringResource(R.string.official_instagram_handle))
                    HorizontalDivider(color = SubtleDivider, modifier = Modifier.padding(vertical = 4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = ScarletGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Payment instructions unlock immediately after confirming scope & authorization.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AshMuted
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                CyberGlowButton(
                    text = stringResource(R.string.confirm_order),
                    onClick = {
                        if (!formState.isCompleteForOrder) {
                            onEditRegistration()
                        } else {
                            onConfirmOrder()
                        }
                    },
                    isLoading = isConfirming,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "confirm_order_button"
                )
            }
        }

        if (confirmedOrder == null) {
            OfficialInstagramCard(showPaymentInstructions = false)
        }
    }
}

@Composable
internal fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AshMuted,
            modifier = Modifier.weight(0.42f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = GhostWhite,
            modifier = Modifier.weight(0.58f)
        )
    }
}

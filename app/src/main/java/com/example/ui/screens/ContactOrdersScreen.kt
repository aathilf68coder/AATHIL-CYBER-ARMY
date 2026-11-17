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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.model.SecurityServiceOrder
import com.example.model.UiState
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberOutlinedButton
import com.example.ui.components.OfficialInstagramCard
import com.example.ui.components.copyTextToClipboard
import com.example.ui.theme.AshMuted
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.DeepCrimsonContainer
import com.example.ui.theme.GhostWhite
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.ScarletGlow
import com.example.ui.theme.TerminalBlack
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ContactOrdersScreen(
    ordersState: UiState<List<SecurityServiceOrder>>,
    userEmail: String?,
    onDeleteOrder: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasConfirmedOrder = ordersState is UiState.Success && ordersState.data.isNotEmpty()
    val latestCode = (ordersState as? UiState.Success)?.data?.firstOrNull()?.orderCode

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CyberCard {
            Text(
                text = "04 // CONTACT & MY ORDERS",
                style = MaterialTheme.typography.headlineSmall,
                color = GhostWhite
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Direct official Instagram support and your encrypted order history.",
                style = MaterialTheme.typography.bodySmall,
                color = AshMuted
            )
        }

        // Prominent @aathil_407 Contact & Payment Instructions Card
        OfficialInstagramCard(
            showPaymentInstructions = hasConfirmedOrder,
            orderCode = latestCode
        )

        // Ethical Authorization Policy Card
        CyberCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = NeonCrimson,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "ETHICAL SECURITY CHARTER",
                    style = MaterialTheme.typography.titleSmall,
                    color = ScarletGlow
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.ethical_policy_notice),
                style = MaterialTheme.typography.bodySmall,
                color = GhostWhite
            )
        }

        // User's Confirmed Orders History (User-scoped only — no admin dashboard)
        CyberCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = ScarletGlow,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "MY CONFIRMED ORDERS",
                    style = MaterialTheme.typography.titleSmall,
                    color = ScarletGlow
                )
            }
            Spacer(Modifier.height(12.dp))

            when (ordersState) {
                is UiState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = NeonCrimson,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Decrypting order vault...",
                            style = MaterialTheme.typography.bodySmall,
                            color = AshMuted
                        )
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = ordersState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonCrimson
                    )
                }
                is UiState.Success -> {
                    if (ordersState.data.isEmpty()) {
                        Text(
                            text = "No confirmed orders yet. Complete registration, select a security service, and confirm your order to unlock payment instructions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AshMuted
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ordersState.data.forEach { order ->
                                OrderHistoryItem(
                                    order = order,
                                    onCopyCode = {
                                        copyTextToClipboard(context, "Order Reference", order.orderCode)
                                    },
                                    onDelete = { onDeleteOrder(order.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Account Session Card
        CyberCard {
            Text(
                text = "CLIENT VAULT SESSION",
                style = MaterialTheme.typography.titleSmall,
                color = ScarletGlow
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Authenticated as: ${userEmail ?: "Google Verified Client"}",
                style = MaterialTheme.typography.bodySmall,
                color = AshMuted
            )
            Spacer(Modifier.height(12.dp))
            CyberOutlinedButton(
                text = "Sign Out Securely",
                onClick = onSignOut,
                icon = Icons.AutoMirrored.Filled.Logout,
                modifier = Modifier.fillMaxWidth(),
                testTag = "sign_out_button"
            )
        }
    }
}

@Composable
private fun OrderHistoryItem(
    order: SecurityServiceOrder,
    onCopyCode: () -> Unit,
    onDelete: () -> Unit
) {
    val dateText = order.createdAt?.toDate()?.let { date ->
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(date)
    } ?: "Pending sync"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TerminalBlack)
            .border(1.dp, CrimsonBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.orderCode,
                    style = MaterialTheme.typography.titleSmall,
                    color = NeonCrimson
                )
                Text(
                    text = order.serviceTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = GhostWhite
                )
            }
            Row {
                IconButton(onClick = onCopyCode) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy order ID",
                        tint = ScarletGlow,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove order",
                        tint = AshMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        SummaryRow("Target", order.authorizedTarget)
        SummaryRow("Auth Doc", order.authorizationDocName)
        SummaryRow("Timestamp", dateText)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "STATUS: SCOPE & AUTHORIZATION CONFIRMED // CONTACT @aathil_407",
            style = MaterialTheme.typography.labelSmall,
            color = ScarletGlow,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(DeepCrimsonContainer.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

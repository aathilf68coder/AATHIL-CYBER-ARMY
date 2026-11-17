package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CyberServiceCatalog
import com.example.model.CyberServiceItem
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberGlowButton
import com.example.ui.theme.AshMuted
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.DeepCrimsonContainer
import com.example.ui.theme.GhostWhite
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.ScarletGlow
import com.example.ui.theme.TerminalBlack

@Composable
fun ServicesScreen(
    selectedServiceId: String?,
    onSelectService: (CyberServiceItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CyberCard {
            Text(
                text = "02 // CYBER SECURITY SERVICES",
                style = MaterialTheme.typography.headlineSmall,
                color = GhostWhite
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Select an authorized assessment module below. All checks strictly require signed customer scope authorization.",
                style = MaterialTheme.typography.bodySmall,
                color = AshMuted
            )
        }

        CyberServiceCatalog.services.forEach { service ->
            val isSelected = service.id == selectedServiceId
            ServiceCard(
                service = service,
                selected = isSelected,
                onSelect = { onSelectService(service) }
            )
        }
    }
}

@Composable
private fun ServiceCard(
    service: CyberServiceItem,
    selected: Boolean,
    onSelect: () -> Unit
) {
    CyberCard(highlighted = selected) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DeepCrimsonContainer)
                        .border(1.dp, if (selected) NeonCrimson else CrimsonBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForService(service.id),
                        contentDescription = service.title,
                        tint = ScarletGlow,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = service.code,
                        style = MaterialTheme.typography.labelSmall,
                        color = ScarletGlow
                    )
                    Text(
                        text = service.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = GhostWhite
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = service.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = AshMuted
        )

        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TerminalBlack)
                .border(1.dp, CrimsonBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "DELIVERABLES // ${service.turnaround}",
                style = MaterialTheme.typography.labelSmall,
                color = ScarletGlow
            )
            service.deliverables.forEach { bullet ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ">",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCrimson
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = bullet,
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostWhite
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        CyberGlowButton(
            text = if (selected) "Selected — Proceed to Order" else stringResource(R.string.select_service),
            onClick = onSelect,
            icon = if (selected) Icons.Default.CheckCircle else Icons.Default.Security,
            modifier = Modifier.fillMaxWidth(),
            testTag = "select_service_${service.id}"
        )
    }
}

private fun iconForService(serviceId: String): ImageVector = when (serviceId) {
    "instagram_security_audit" -> Icons.Default.VerifiedUser
    "facebook_security_audit" -> Icons.Default.Public
    "android_device_audit" -> Icons.Default.Devices
    "camera_permission_check" -> Icons.Default.CameraAlt
    "gallery_privacy_audit" -> Icons.Default.PhotoLibrary
    "rat_detection_removal" -> Icons.Default.BugReport
    "website_vuln_assessment" -> Icons.Default.Language
    else -> Icons.Default.Security
}

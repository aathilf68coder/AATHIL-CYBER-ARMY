package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.TerminalLogEntry
import com.example.ui.theme.AshMuted
import com.example.ui.theme.BloodRed
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.CrimsonBorderBright
import com.example.ui.theme.CrimsonSurface
import com.example.ui.theme.CrimsonSurfaceElevated
import com.example.ui.theme.DeepCrimsonContainer
import com.example.ui.theme.GhostWhite
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.PitchObsidian
import com.example.ui.theme.ScarletGlow
import com.example.ui.theme.SubtleDivider
import com.example.ui.theme.TerminalBlack

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (highlighted) CrimsonBorderBright else CrimsonBorder,
        animationSpec = tween(250),
        label = "cardBorder"
    )
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (highlighted) {
                    Modifier.shadow(
                        elevation = 14.dp,
                        shape = shape,
                        ambientColor = NeonCrimson,
                        spotColor = NeonCrimson
                    )
                } else {
                    Modifier
                }
            ),
        shape = shape,
        color = CrimsonSurface.copy(alpha = 0.92f),
        border = BorderStroke(if (highlighted) 1.5.dp else 1.dp, borderColor),
        tonalElevation = if (highlighted) 6.dp else 2.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CrimsonSurfaceElevated.copy(alpha = if (highlighted) 0.72f else 0.45f),
                            TerminalBlack.copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun CyberGlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    testTag: String = ""
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glowPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val shape = RoundedCornerShape(10.dp)
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = BloodRed,
            contentColor = GhostWhite,
            disabledContainerColor = SubtleDivider,
            disabledContentColor = AshMuted
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
            .drawBehind {
                if (enabled && !isLoading) {
                    drawRoundRect(
                        color = NeonCrimson.copy(alpha = glowAlpha * 0.45f),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                    )
                }
            }
            .border(
                width = 1.dp,
                color = if (enabled) ScarletGlow.copy(alpha = 0.8f) else SubtleDivider,
                shape = shape
            )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = GhostWhite,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "PROCESSING...",
                style = MaterialTheme.typography.labelLarge
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CyberOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    testTag: String = ""
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (enabled) CrimsonBorderBright else SubtleDivider),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = DeepCrimsonContainer.copy(alpha = 0.35f),
            contentColor = ScarletGlow,
            disabledContentColor = AshMuted
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SecurityCountdownBanner(
    formattedTime: String,
    remainingSeconds: Int,
    sessionToken: String,
    modifier: Modifier = Modifier
) {
    val isUrgent = remainingSeconds <= 60
    val infiniteTransition = rememberInfiniteTransition(label = "timerPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isUrgent) 450 else 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = TerminalBlack.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, if (isUrgent) NeonCrimson else CrimsonBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            DeepCrimsonContainer.copy(alpha = 0.55f),
                            TerminalBlack,
                            DeepCrimsonContainer.copy(alpha = 0.4f)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonCrimson.copy(alpha = pulseAlpha))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.countdown_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = ScarletGlow,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "TOKEN $sessionToken // TLS-256",
                    style = MaterialTheme.typography.labelSmall,
                    color = AshMuted
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepCrimsonContainer.copy(alpha = 0.85f))
                    .border(1.dp, NeonCrimson.copy(alpha = pulseAlpha), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Session timer",
                    tint = NeonCrimson.copy(alpha = pulseAlpha),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = formattedTime,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NeonCrimson.copy(alpha = pulseAlpha),
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.testTag("session_countdown_text")
                )
            }
        }
    }
}

@Composable
fun CyberBootLoadingScreen(
    progress: Float,
    statusLine: String,
    sessionToken: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(280),
        label = "bootProgress"
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchObsidian),
        contentAlignment = Alignment.Center
    ) {
        MatrixBackground(intensity = 0.9f)
        Column(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            coil.compose.AsyncImage(
                model = R.drawable.img_cyber_hero_1794944990834,
                contentDescription = "Aegis Cyber security emblem",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CrimsonBorderBright, RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "AEGIS CYBER",
                style = MaterialTheme.typography.displaySmall,
                color = GhostWhite
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.brand_subtitle),
                style = MaterialTheme.typography.labelMedium,
                color = ScarletGlow,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(24.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = NeonCrimson,
                trackColor = SubtleDivider
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = statusLine,
                style = MaterialTheme.typography.bodySmall,
                color = ScarletGlow,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "SESSION // $sessionToken",
                style = MaterialTheme.typography.labelSmall,
                color = AshMuted
            )
        }
    }
}

@Composable
fun SessionExpiredOverlay(
    expiredToken: String,
    onRequestNewSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchObsidian.copy(alpha = 0.96f)),
        contentAlignment = Alignment.Center
    ) {
        MatrixBackground(intensity = 0.65f)
        CyberCard(
            highlighted = true,
            modifier = Modifier
                .widthIn(max = 460.dp)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(DeepCrimsonContainer)
                        .border(1.5.dp, NeonCrimson, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Session expired lock",
                        tint = NeonCrimson,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "SESSION EXPIRED",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NeonCrimson,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Security token $expiredToken has reached 00:00 and been revoked to protect your registration and authorization records.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GhostWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                CyberGlowButton(
                    text = stringResource(R.string.request_new_session),
                    onClick = onRequestNewSession,
                    icon = Icons.Default.Refresh,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "request_new_session_button"
                )
            }
        }
    }
}

@Composable
fun CyberTerminalFeed(
    logs: List<TerminalLogEntry>,
    modifier: Modifier = Modifier
) {
    CyberCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = "Terminal log",
                tint = ScarletGlow,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "CYBER TERMINAL STREAM",
                style = MaterialTheme.typography.titleSmall,
                color = ScarletGlow
            )
        }
        Spacer(Modifier.height(10.dp))
        logs.take(4).forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "[${entry.timestamp}]",
                    style = MaterialTheme.typography.labelSmall,
                    color = AshMuted
                )
                Text(
                    text = entry.level,
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCrimson,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = entry.message,
                    style = MaterialTheme.typography.labelSmall,
                    color = GhostWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun OfficialInstagramCard(
    showPaymentInstructions: Boolean,
    orderCode: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val handle = stringResource(R.string.official_instagram_handle)
    val igUrl = stringResource(R.string.official_instagram_url)

    CyberCard(
        highlighted = showPaymentInstructions,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepCrimsonContainer)
                    .border(1.dp, NeonCrimson, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Official Instagram verification",
                    tint = ScarletGlow,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "OFFICIAL CONTACT CHANNEL",
                    style = MaterialTheme.typography.labelMedium,
                    color = ScarletGlow
                )
                Text(
                    text = handle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = NeonCrimson,
                    modifier = Modifier.testTag("official_instagram_handle_text")
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.official_contact_prompt),
            style = MaterialTheme.typography.bodyMedium,
            color = GhostWhite
        )

        if (showPaymentInstructions) {
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepCrimsonContainer.copy(alpha = 0.55f))
                    .border(1.dp, CrimsonBorderBright, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = ScarletGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "PAYMENT & COORDINATION INSTRUCTIONS",
                            style = MaterialTheme.typography.titleSmall,
                            color = ScarletGlow
                        )
                    }
                    if (!orderCode.isNullOrBlank()) {
                        Text(
                            text = "1. Copy your confirmed Order ID ($orderCode) and send it via DM to $handle on Instagram.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GhostWhite
                        )
                    } else {
                        Text(
                            text = "1. Send your confirmed Order ID via DM to $handle on Instagram.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GhostWhite
                        )
                    }
                    Text(
                        text = "2. Our security engineer validates your signed Authorization Document and audit scope.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostWhite
                    )
                    Text(
                        text = "3. Official payment invoice & encrypted assessment schedule are issued directly through $handle once scope clearance completes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostWhite
                    )
                }
            }
        } else {
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalBlack)
                    .border(1.dp, SubtleDivider, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AshMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Payment instructions unlock automatically after service scope and authorization are confirmed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AshMuted
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CyberOutlinedButton(
                text = "Copy $handle",
                onClick = {
                    copyTextToClipboard(context, "Instagram Handle", handle)
                },
                icon = Icons.Default.ContentCopy,
                modifier = Modifier.weight(1f),
                testTag = "copy_instagram_button"
            )
            CyberGlowButton(
                text = "Open Instagram",
                onClick = {
                    openExternalUrl(context, igUrl)
                },
                icon = Icons.AutoMirrored.Filled.OpenInNew,
                modifier = Modifier.weight(1f),
                testTag = "open_instagram_button"
            )
        }
    }
}

fun copyTextToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "$label copied: $text", Toast.LENGTH_SHORT).show()
}

fun openExternalUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Unable to open browser. Handle: @aathil_407", Toast.LENGTH_SHORT).show()
    }
}

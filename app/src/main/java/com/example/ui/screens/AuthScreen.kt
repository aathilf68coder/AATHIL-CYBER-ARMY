package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.onGoogleSignInClicked
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberGlowButton
import com.example.ui.components.MatrixBackground
import com.example.ui.theme.AshMuted
import com.example.ui.theme.CrimsonBorderBright
import com.example.ui.theme.GhostWhite
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.ScarletGlow

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        MatrixBackground(intensity = 0.85f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                coil.compose.AsyncImage(
                    model = R.drawable.img_cyber_hero_1794944990834,
                    contentDescription = "Aegis Cyber Operations Shield",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, CrimsonBorderBright, RoundedCornerShape(16.dp))
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "AEGIS CYBER",
                    style = MaterialTheme.typography.displaySmall,
                    color = GhostWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.brand_subtitle),
                    style = MaterialTheme.typography.labelLarge,
                    color = ScarletGlow,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))

                CyberCard(highlighted = true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NeonCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "ENCRYPTED CLIENT AUTHENTICATION",
                            style = MaterialTheme.typography.titleSmall,
                            color = ScarletGlow
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Sign in with Google to securely store your customer registration, authorization documents, and confirmed cyber audit orders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GhostWhite
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = ScarletGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.ethical_policy_notice),
                            style = MaterialTheme.typography.bodySmall,
                            color = AshMuted
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = NeonCrimson,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Official Support: ${stringResource(R.string.official_instagram_handle)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ScarletGlow
                        )
                    }

                    if (!authError.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = authError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCrimson
                        )
                    }

                    Spacer(Modifier.height(18.dp))
                    CyberGlowButton(
                        text = stringResource(R.string.sign_in_with_google),
                        onClick = {
                            authError = null
                            isSigningIn = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isSigningIn = false
                                    onAuthSuccess()
                                },
                                onAuthError = { msg ->
                                    isSigningIn = false
                                    authError = msg
                                },
                                scope = scope,
                                onAuthCancelled = {
                                    isSigningIn = false
                                }
                            )
                        },
                        isLoading = isSigningIn,
                        icon = Icons.Default.Lock,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "google_sign_in_button"
                    )
                }
            }
        }
    }
}

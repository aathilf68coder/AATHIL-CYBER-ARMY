package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AttachedImageItem
import com.example.model.TerminalLogEntry
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberGlowButton
import com.example.ui.components.CyberOutlinedButton
import com.example.ui.components.CyberTerminalFeed
import com.example.ui.theme.AshMuted
import com.example.ui.theme.BloodRed
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.CrimsonBorderBright
import com.example.ui.theme.DeepCrimsonContainer
import com.example.ui.theme.GhostWhite
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.ScarletGlow
import com.example.ui.theme.SubtleDivider
import com.example.ui.theme.TerminalBlack
import com.example.viewmodel.RegistrationFormState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegistrationScreen(
    formState: RegistrationFormState,
    terminalLogs: List<TerminalLogEntry>,
    onNameChange: (String) -> Unit,
    onMobileChange: (String) -> Unit,
    onTargetChange: (String) -> Unit,
    onInstagramChange: (String) -> Unit,
    onDocumentSelected: (name: String, uri: String) -> Unit,
    onReferenceImagesSelected: (List<AttachedImageItem>) -> Unit,
    onRemoveReferenceImage: (Int) -> Unit,
    onAuthorizationConfirmedChange: (Boolean) -> Unit,
    onSaveAndContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Zero-permission Storage Access Framework (SAF) Document Picker
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = resolveDisplayName(context, uri) ?: "authorization_document.pdf"
            onDocumentSelected(fileName, uri.toString())
        }
    }

    // Zero-permission Android Photo Picker for optional reference images
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val items = uris.mapIndexed { idx, uri ->
                AttachedImageItem(
                    uriString = uri.toString(),
                    displayName = resolveDisplayName(context, uri) ?: "reference_${idx + 1}.jpg"
                )
            }
            onReferenceImagesSelected(items)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner Card
        CyberCard(highlighted = true) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, CrimsonBorder, RoundedCornerShape(10.dp))
            ) {
                AsyncImage(
                    model = R.drawable.img_cyber_hero_1794944990834,
                    contentDescription = "Aegis Cyber hero emblem",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "01 // CUSTOMER REGISTRATION",
                style = MaterialTheme.typography.headlineSmall,
                color = GhostWhite
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Register target details and explicit authorization documentation before initiating any security audit.",
                style = MaterialTheme.typography.bodySmall,
                color = AshMuted
            )
        }

        // Registration Form Card
        CyberCard {
            Text(
                text = "CLIENT & TARGET IDENTIFICATION",
                style = MaterialTheme.typography.titleSmall,
                color = ScarletGlow
            )
            Spacer(Modifier.height(12.dp))

            CyberTextField(
                value = formState.customerName,
                onValueChange = onNameChange,
                label = "Customer Name *",
                placeholder = "Full legal or business name",
                leadingIcon = Icons.Default.Badge,
                imeAction = ImeAction.Next,
                testTag = "input_customer_name"
            )
            Spacer(Modifier.height(12.dp))

            CyberTextField(
                value = formState.mobileNumber,
                onValueChange = onMobileChange,
                label = "Customer Mobile Number *",
                placeholder = "+94 77 000 0000",
                leadingIcon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
                testTag = "input_mobile_number"
            )
            Spacer(Modifier.height(12.dp))

            CyberTextField(
                value = formState.authorizedTarget,
                onValueChange = onTargetChange,
                label = "Authorized Target Website / Application *",
                placeholder = "https://target.com or com.company.app",
                leadingIcon = Icons.Default.Language,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next,
                testTag = "input_authorized_target"
            )
            Spacer(Modifier.height(12.dp))

            CyberTextField(
                value = formState.instagramUsername,
                onValueChange = onInstagramChange,
                label = "Instagram Username (Optional)",
                placeholder = "@your_handle",
                leadingIcon = Icons.Default.AlternateEmail,
                imeAction = ImeAction.Done,
                testTag = "input_instagram_username"
            )

            Spacer(Modifier.height(18.dp))
            Text(
                text = "AUTHORIZATION & EVIDENCE ARTIFACTS",
                style = MaterialTheme.typography.titleSmall,
                color = ScarletGlow
            )
            Spacer(Modifier.height(10.dp))

            // Authorization Document Upload Box (Required)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalBlack,
                border = BorderStroke(
                    1.dp,
                    if (formState.authorizationDocName.isNotBlank()) CrimsonBorderBright else CrimsonBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (formState.authorizationDocName.isNotBlank()) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Default.Description
                            },
                            contentDescription = null,
                            tint = if (formState.authorizationDocName.isNotBlank()) ScarletGlow else AshMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Authorization Document *",
                                style = MaterialTheme.typography.titleMedium,
                                color = GhostWhite
                            )
                            Text(
                                text = if (formState.authorizationDocName.isNotBlank()) {
                                    "Attached: ${formState.authorizationDocName}"
                                } else {
                                    "Signed LoA / Permission PDF or Image"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (formState.authorizationDocName.isNotBlank()) ScarletGlow else AshMuted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    CyberOutlinedButton(
                        text = if (formState.authorizationDocName.isNotBlank()) {
                            "Replace Authorization Doc"
                        } else {
                            "Upload Authorization Document"
                        },
                        onClick = {
                            documentLauncher.launch(arrayOf("application/pdf", "image/*", "text/plain"))
                        },
                        icon = Icons.Default.UploadFile,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "upload_auth_doc_button"
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Reference Images Upload Box (Optional)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TerminalBlack,
                border = BorderStroke(1.dp, CrimsonBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PermMedia,
                            contentDescription = null,
                            tint = ScarletGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reference Images (Optional)",
                                style = MaterialTheme.typography.titleMedium,
                                color = GhostWhite
                            )
                            Text(
                                text = if (formState.referenceImages.isEmpty()) {
                                    "Attach up to 5 screenshots or scope visuals"
                                } else {
                                    "${formState.referenceImages.size} image(s) attached"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = AshMuted
                            )
                        }
                    }

                    if (formState.referenceImages.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            formState.referenceImages.forEachIndexed { index, item ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DeepCrimsonContainer.copy(alpha = 0.6f))
                                        .border(1.dp, CrimsonBorder, RoundedCornerShape(8.dp))
                                        .padding(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (item.uriString.isNotBlank()) {
                                            AsyncImage(
                                                model = item.uriString,
                                                contentDescription = item.displayName,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                            )
                                        }
                                        Text(
                                            text = item.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GhostWhite,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.width(96.dp)
                                        )
                                        IconButton(
                                            onClick = { onRemoveReferenceImage(index) },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove image",
                                                tint = ScarletGlow,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    CyberOutlinedButton(
                        text = "Upload Reference Images",
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        icon = Icons.Default.PermMedia,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "upload_ref_images_button"
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Explicit Written Authorization Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepCrimsonContainer.copy(alpha = 0.35f))
                    .border(
                        1.dp,
                        if (formState.authorizationConfirmed) CrimsonBorderBright else SubtleDivider,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onAuthorizationConfirmedChange(!formState.authorizationConfirmed) }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = formState.authorizationConfirmed,
                    onCheckedChange = onAuthorizationConfirmedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = BloodRed,
                        uncheckedColor = AshMuted,
                        checkmarkColor = GhostWhite
                    ),
                    modifier = Modifier.testTag("auth_confirmation_checkbox")
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.ethical_policy_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = GhostWhite
                )
            }

            if (!formState.validationError.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = formState.validationError,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonCrimson
                )
            }

            Spacer(Modifier.height(16.dp))
            CyberGlowButton(
                text = "Save & Choose Service",
                onClick = onSaveAndContinue,
                isLoading = formState.isSaving,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth(),
                testTag = "save_registration_button"
            )
        }

        CyberTerminalFeed(logs = terminalLogs)
    }
}

@Composable
private fun CyberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall, color = AshMuted.copy(alpha = 0.7f)) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = ScarletGlow
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCrimson,
            unfocusedBorderColor = CrimsonBorder,
            focusedLabelColor = ScarletGlow,
            unfocusedLabelColor = AshMuted,
            cursorColor = NeonCrimson,
            focusedTextColor = GhostWhite,
            unfocusedTextColor = GhostWhite,
            focusedContainerColor = TerminalBlack,
            unfocusedContainerColor = TerminalBlack
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    )
}

private fun resolveDisplayName(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx) else null
                } else {
                    null
                }
            }
    } catch (_: Exception) {
        uri.lastPathSegment
    }
}

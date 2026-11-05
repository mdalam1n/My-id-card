package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.DocType
import com.example.data.model.DocumentRecord
import com.example.ui.theme.VaultEmerald
import com.example.ui.viewmodel.DocumentViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DocumentViewModel,
    onNavigateToDocument: (DocType) -> Unit,
    onNavigateToPersonalCards: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val documentsMap by viewModel.documents.collectAsState()
    val personalCards by viewModel.personalCards.collectAsState()

    var isEditingName by remember { mutableStateOf(false) }
    var nameInput by remember(userProfile.name) { mutableStateOf(userProfile.name) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Avatar Photo Picker
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setProfileImage(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "My Personal Document",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        color = VaultEmerald.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = VaultEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "100% Offline",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = VaultEmerald
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. TOP SECTION: Profile Picture and Name
            item {
                UserProfileHeaderCard(
                    userName = userProfile.name,
                    profileImagePath = userProfile.profileImagePath,
                    isEditingName = isEditingName,
                    nameInput = nameInput,
                    onNameInputChange = { nameInput = it },
                    onStartEditName = {
                        nameInput = userProfile.name
                        isEditingName = true
                    },
                    onSaveName = {
                        viewModel.updateUserName(nameInput)
                        isEditingName = false
                    },
                    onCancelEditName = {
                        nameInput = userProfile.name
                        isEditingName = false
                    },
                    onPickAvatar = {
                        avatarPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemoveAvatar = {
                        viewModel.removeProfileImage()
                    }
                )
            }

            // Section Label
            item {
                Text(
                    text = "My Documents & Vault",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // 2. MAIN MENU OPTIONS:
            // a) My Ekama (ID)
            item {
                val ekamaDoc = documentsMap[DocType.EKAMA]
                MenuOptionCard(
                    title = DocType.EKAMA.title,
                    subtitle = DocType.EKAMA.subtitle,
                    icon = Icons.Default.Badge,
                    statusText = ekamaDoc?.statusText ?: "Not Added",
                    isComplete = ekamaDoc?.isComplete ?: false,
                    thumbnailPath = ekamaDoc?.frontImagePath,
                    testTag = "menu_ekama_button",
                    onClick = { onNavigateToDocument(DocType.EKAMA) }
                )
            }

            // b) My Driving Licence
            item {
                val licenseDoc = documentsMap[DocType.DRIVING_LICENCE]
                MenuOptionCard(
                    title = DocType.DRIVING_LICENCE.title,
                    subtitle = DocType.DRIVING_LICENCE.subtitle,
                    icon = Icons.Default.DirectionsCar,
                    statusText = licenseDoc?.statusText ?: "Not Added",
                    isComplete = licenseDoc?.isComplete ?: false,
                    thumbnailPath = licenseDoc?.frontImagePath,
                    testTag = "menu_driving_licence_button",
                    onClick = { onNavigateToDocument(DocType.DRIVING_LICENCE) }
                )
            }

            // c) My Home ID
            item {
                val homeIdDoc = documentsMap[DocType.HOME_ID]
                MenuOptionCard(
                    title = DocType.HOME_ID.title,
                    subtitle = DocType.HOME_ID.subtitle,
                    icon = Icons.Default.Home,
                    statusText = homeIdDoc?.statusText ?: "Not Added",
                    isComplete = homeIdDoc?.isComplete ?: false,
                    thumbnailPath = homeIdDoc?.frontImagePath,
                    testTag = "menu_home_id_button",
                    onClick = { onNavigateToDocument(DocType.HOME_ID) }
                )
            }

            // d) My Passport
            item {
                val passportDoc = documentsMap[DocType.PASSPORT]
                MenuOptionCard(
                    title = DocType.PASSPORT.title,
                    subtitle = DocType.PASSPORT.subtitle,
                    icon = Icons.Default.Flight,
                    statusText = passportDoc?.statusText ?: "Not Added",
                    isComplete = passportDoc?.isComplete ?: false,
                    thumbnailPath = passportDoc?.frontImagePath,
                    testTag = "menu_passport_button",
                    onClick = { onNavigateToDocument(DocType.PASSPORT) }
                )
            }

            // e) Personal Card's (Supports up to 5 custom card slots/entries)
            item {
                MenuOptionCard(
                    title = "Personal Card's",
                    subtitle = "Supports up to 5 custom card slots/entries",
                    icon = Icons.Default.CreditCard,
                    statusText = "${personalCards.size} / 5 Cards",
                    isComplete = personalCards.isNotEmpty(),
                    thumbnailPath = personalCards.firstOrNull { !it.frontImagePath.isNullOrEmpty() }?.frontImagePath,
                    badgeColor = MaterialTheme.colorScheme.primary,
                    testTag = "menu_personal_cards_button",
                    onClick = onNavigateToPersonalCards
                )
            }

            // f) Exit
            item {
                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showExitDialog = true }
                        .testTag("menu_exit_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                            shape = CircleShape,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Exit App",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Safely close the application",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Bottom privacy footer
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Private & Secure Storage",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "All images are saved inside this device's private app directory (filesDir) and never uploaded to any cloud server.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Exit Application?") },
            text = {
                Text("Are you sure you want to close My Personal Document? Your data remains securely saved on this device.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        (context as? Activity)?.finishAffinity()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_exit_button")
                ) {
                    Text("Exit Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Stay")
                }
            }
        )
    }
}

@Composable
fun UserProfileHeaderCard(
    userName: String,
    profileImagePath: String?,
    isEditingName: Boolean,
    nameInput: String,
    onNameInputChange: (String) -> Unit,
    onStartEditName: () -> Unit,
    onSaveName: () -> Unit,
    onCancelEditName: () -> Unit,
    onPickAvatar: () -> Unit,
    onRemoveAvatar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_profile_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture with Tap-to-Change and camera badge
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clickable { onPickAvatar() }
                    .testTag("profile_picture_container"),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(92.dp)
                ) {
                    if (!profileImagePath.isNullOrEmpty() && File(profileImagePath).exists()) {
                        AsyncImage(
                            model = File(profileImagePath),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .testTag("profile_picture_image")
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Placeholder",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }
                }

                // Camera Badge overlay at bottom-right
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Picture",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tap photo to change profile picture",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!profileImagePath.isNullOrEmpty() && File(profileImagePath).exists()) {
                TextButton(
                    onClick = onRemoveAvatar,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Remove Photo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // User Name: Display or Edit field
            if (isEditingName) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = onNameInputChange,
                        label = { Text("Your Name") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("user_name_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onSaveName,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .size(44.dp)
                            .testTag("save_name_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Name",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onStartEditName() }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("user_name_display_row")
                ) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Name",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MenuOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    statusText: String,
    isComplete: Boolean,
    thumbnailPath: String? = null,
    badgeColor: Color? = null,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon or mini thumbnail preview if available
            if (!thumbnailPath.isNullOrEmpty() && File(thumbnailPath).exists()) {
                AsyncImage(
                    model = File(thumbnailPath),
                    contentDescription = "$title Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                )
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = (badgeColor ?: if (isComplete) VaultEmerald else MaterialTheme.colorScheme.surfaceVariant).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeColor ?: if (isComplete) VaultEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

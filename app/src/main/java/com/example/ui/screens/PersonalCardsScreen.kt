package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.PersonalCard
import com.example.ui.theme.VaultEmerald
import com.example.ui.viewmodel.DocumentViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalCardsScreen(
    viewModel: DocumentViewModel,
    onNavigateBack: () -> Unit
) {
    val personalCards by viewModel.personalCards.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<PersonalCard?>(null) }
    var cardToDelete by remember { mutableStateOf<PersonalCard?>(null) }

    var photoTarget by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && photoTarget != null) {
            val (cardId, isFront) = photoTarget!!
            if (isFront) {
                viewModel.savePersonalCardFrontImage(cardId, uri)
            } else {
                viewModel.savePersonalCardBackImage(cardId, uri)
            }
        }
        photoTarget = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Personal Card's",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_card_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Card")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Cards Saved",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${personalCards.size} Cards",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "Add unlimited custom personal cards (Work ID, Gym, Insurance, etc.) with Apple Wallet style view.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (personalCards.isEmpty()) {
                item {
                    EmptyCardsPlaceholder(onAddClick = { showAddDialog = true })
                }
            } else {
                items(personalCards, key = { it.id }) { card ->
                    PersonalCardItem(
                        card = card,
                        onEditClick = { editingCard = card },
                        onDeleteCardClick = { cardToDelete = card },
                        onFrontImageClick = { path ->
                            viewModel.openFullScreenPreview("${card.title} - Front Side", path)
                        },
                        onBackImageClick = { path ->
                            viewModel.openFullScreenPreview("${card.title} - Back Side", path)
                        },
                        onUploadFrontClick = {
                            photoTarget = Pair(card.id, true)
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onUploadBackClick = {
                            photoTarget = Pair(card.id, false)
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onDeleteFrontClick = {
                            viewModel.deletePersonalCardFrontImage(card.id)
                        },
                        onDeleteBackClick = {
                            viewModel.deletePersonalCardBackImage(card.id)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddDialog) {
        AddOrEditCardDialog(
            initialTitle = "",
            initialCardNumber = "",
            initialNotes = "",
            isEditing = false,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, number, notes ->
                viewModel.addPersonalCard(title, number, notes)
                showAddDialog = false
            }
        )
    }

    editingCard?.let { card ->
        AddOrEditCardDialog(
            initialTitle = card.title,
            initialCardNumber = card.cardNumber,
            initialNotes = card.notes,
            isEditing = true,
            onDismiss = { editingCard = null },
            onConfirm = { title, number, notes ->
                viewModel.updatePersonalCardDetails(card.id, title, number, notes)
                editingCard = null
            }
        )
    }

    cardToDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete '${card.title}'?") },
            text = {
                Text("This will permanently remove this card and all its stored images from your device.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePersonalCard(card.id)
                        cardToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_delete_card_button")
                ) {
                    Text("Delete Card")
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PersonalCardItem(
    card: PersonalCard,
    onEditClick: () -> Unit,
    onDeleteCardClick: () -> Unit,
    onFrontImageClick: (String) -> Unit,
    onBackImageClick: (String) -> Unit,
    onUploadFrontClick: () -> Unit,
    onUploadBackClick: () -> Unit,
    onDeleteFrontClick: () -> Unit,
    onDeleteBackClick: () -> Unit
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
            .testTag("personal_card_${card.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (card.cardNumber.isNotEmpty()) {
                        Text(
                            text = "No: ${card.cardNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (card.notes.isNotEmpty()) {
                        Text(
                            text = card.notes,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.testTag("edit_card_button_${card.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Card Info",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDeleteCardClick,
                    modifier = Modifier.testTag("delete_card_button_${card.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Card",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Surface(
                color = if (card.isComplete) VaultEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = card.statusText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (card.isComplete) VaultEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MiniSideSlot(
                        label = "Front Side",
                        imagePath = card.frontImagePath,
                        onImageClick = onFrontImageClick,
                        onUploadClick = onUploadFrontClick,
                        onDeleteClick = onDeleteFrontClick
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    MiniSideSlot(
                        label = "Back Side",
                        imagePath = card.backImagePath,
                        onImageClick = onBackImageClick,
                        onUploadClick = onUploadBackClick,
                        onDeleteClick = onDeleteBackClick
                    )
                }
            }
        }
    }
}

@Composable
fun MiniSideSlot(
    label: String,
    imagePath: String?,
    onImageClick: (String) -> Unit,
    onUploadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (!imagePath.isNullOrEmpty() && File(imagePath).exists()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.58f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onImageClick(imagePath) }
            ) {
                AsyncImage(
                    model = File(imagePath),
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(2.dp)
                            .size(14.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedButton(
                    onClick = onUploadClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Change", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete", style = MaterialTheme.typography.labelSmall)
                }
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.58f)
                    .clickable { onUploadClick() }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Add $label",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add $label",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCardsPlaceholder(onAddClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Style,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Personal Cards Added Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Save unlimited custom cards such as Work ID, Health Insurance, Gym Pass, or Library Card.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAddClick,
                modifier = Modifier.testTag("empty_add_card_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Your First Card")
            }
        }
    }
}

@Composable
fun AddOrEditCardDialog(
    initialTitle: String,
    initialCardNumber: String,
    initialNotes: String,
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, cardNumber: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var cardNumber by remember { mutableStateOf(initialCardNumber) }
    var notes by remember { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditing) "Edit Card Details" else "Add New Personal Card")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Card Name / Title *") },
                    placeholder = { Text("e.g. Work ID, Gym Card, Insurance") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_title_input")
                )

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("Card / Member Number (Optional)") },
                    placeholder = { Text("e.g. EMP-4091") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_number_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Expiry Date (Optional)") },
                    placeholder = { Text("e.g. Valid thru 2029") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        if (title.isBlank()) "Personal Card" else title,
                        cardNumber,
                        notes
                    )
                },
                modifier = Modifier.testTag("save_card_dialog_button")
            ) {
                Text(if (isEditing) "Save Changes" else "Add Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DocType
import com.example.data.model.DocumentRecord
import com.example.data.model.FullScreenPreviewTarget
import com.example.data.model.PersonalCard
import com.example.data.model.UserProfile
import com.example.data.repository.DocumentRepository
import com.example.data.storage.ImageStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DocumentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DocumentRepository(application)
    private val storageManager = ImageStorageManager(application)

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val documents: StateFlow<Map<DocType, DocumentRecord>> = repository.documents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val personalCards: StateFlow<List<PersonalCard>> = repository.personalCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _fullScreenTarget = MutableStateFlow<FullScreenPreviewTarget?>(null)
    val fullScreenTarget: StateFlow<FullScreenPreviewTarget?> = _fullScreenTarget.asStateFlow()

    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    fun clearFeedback() {
        _userFeedback.value = null
    }

    fun openFullScreenPreview(title: String, imagePath: String) {
        if (storageManager.doesFileExist(imagePath)) {
            _fullScreenTarget.value = FullScreenPreviewTarget(title = title, imagePath = imagePath)
        } else {
            _userFeedback.value = "Image file not found on local storage"
        }
    }

    fun closeFullScreenPreview() {
        _fullScreenTarget.value = null
    }

    fun updateUserName(name: String) {
        repository.saveUserName(name)
        _userFeedback.value = "Name updated"
    }

    fun setProfileImage(uri: Uri) {
        viewModelScope.launch {
            val oldPath = userProfile.value.profileImagePath
            val savedPath = storageManager.saveProfileImage(uri, oldPath)
            if (savedPath != null) {
                repository.saveUserProfileImage(savedPath)
                _userFeedback.value = "Profile picture updated"
            } else {
                _userFeedback.value = "Failed to save profile picture"
            }
        }
    }

    fun removeProfileImage() {
        viewModelScope.launch {
            val oldPath = userProfile.value.profileImagePath
            storageManager.deleteFileByPath(oldPath)
            repository.saveUserProfileImage(null)
            _userFeedback.value = "Profile picture removed"
        }
    }

    fun saveDocumentFrontImage(docType: DocType, uri: Uri) {
        viewModelScope.launch {
            val currentDoc = documents.value[docType]
            val oldPath = currentDoc?.frontImagePath
            val newPath = storageManager.saveDocumentImage(uri, docType.id, "front", oldPath)
            if (newPath != null) {
                repository.updateDocument(docType, frontImagePath = newPath)
                _userFeedback.value = "${docType.title} front image saved"
            } else {
                _userFeedback.value = "Failed to save image"
            }
        }
    }

    fun saveDocumentBackImage(docType: DocType, uri: Uri) {
        viewModelScope.launch {
            val currentDoc = documents.value[docType]
            val oldPath = currentDoc?.backImagePath
            val newPath = storageManager.saveDocumentImage(uri, docType.id, "back", oldPath)
            if (newPath != null) {
                repository.updateDocument(docType, backImagePath = newPath)
                _userFeedback.value = "${docType.title} back image saved"
            } else {
                _userFeedback.value = "Failed to save image"
            }
        }
    }

    fun deleteDocumentFrontImage(docType: DocType) {
        viewModelScope.launch {
            val currentDoc = documents.value[docType]
            storageManager.deleteFileByPath(currentDoc?.frontImagePath)
            repository.updateDocument(docType, clearFront = true)
            _userFeedback.value = "Front image removed"
        }
    }

    fun deleteDocumentBackImage(docType: DocType) {
        viewModelScope.launch {
            val currentDoc = documents.value[docType]
            storageManager.deleteFileByPath(currentDoc?.backImagePath)
            repository.updateDocument(docType, clearBack = true)
            _userFeedback.value = "Back image removed"
        }
    }

    fun updateDocumentDetails(docType: DocType, number: String, notes: String) {
        repository.updateDocument(docType, documentNumber = number, notes = notes)
        _userFeedback.value = "Details saved"
    }

    fun addPersonalCard(title: String, cardNumber: String, notes: String): Boolean {
        val created = repository.addPersonalCard(title, cardNumber, notes)
        return if (created != null) {
            _userFeedback.value = "Card added (${created.title})"
            true
        } else {
            _userFeedback.value = "Failed to add card"
            false
        }
    }

    fun updatePersonalCardDetails(cardId: String, title: String, cardNumber: String, notes: String) {
        repository.updatePersonalCard(cardId, title = title, cardNumber = cardNumber, notes = notes)
        _userFeedback.value = "Card details updated"
    }

    fun savePersonalCardFrontImage(cardId: String, uri: Uri) {
        viewModelScope.launch {
            val currentCard = personalCards.value.firstOrNull { it.id == cardId }
            val oldPath = currentCard?.frontImagePath
            val newPath = storageManager.saveCardImage(uri, cardId, "front", oldPath)
            if (newPath != null) {
                repository.updatePersonalCard(cardId, frontImagePath = newPath)
                _userFeedback.value = "Front image saved"
            } else {
                _userFeedback.value = "Failed to save front image"
            }
        }
    }

    fun savePersonalCardBackImage(cardId: String, uri: Uri) {
        viewModelScope.launch {
            val currentCard = personalCards.value.firstOrNull { it.id == cardId }
            val oldPath = currentCard?.backImagePath
            val newPath = storageManager.saveCardImage(uri, cardId, "back", oldPath)
            if (newPath != null) {
                repository.updatePersonalCard(cardId, backImagePath = newPath)
                _userFeedback.value = "Back image saved"
            } else {
                _userFeedback.value = "Failed to save back image"
            }
        }
    }

    fun deletePersonalCardFrontImage(cardId: String) {
        viewModelScope.launch {
            val currentCard = personalCards.value.firstOrNull { it.id == cardId }
            storageManager.deleteFileByPath(currentCard?.frontImagePath)
            repository.updatePersonalCard(cardId, clearFront = true)
            _userFeedback.value = "Front image removed"
        }
    }

    fun deletePersonalCardBackImage(cardId: String) {
        viewModelScope.launch {
            val currentCard = personalCards.value.firstOrNull { it.id == cardId }
            storageManager.deleteFileByPath(currentCard?.backImagePath)
            repository.updatePersonalCard(cardId, clearBack = true)
            _userFeedback.value = "Back image removed"
        }
    }

    fun deletePersonalCard(cardId: String) {
        viewModelScope.launch {
            val card = repository.deletePersonalCard(cardId)
            if (card != null) {
                storageManager.deleteFileByPath(card.frontImagePath)
                storageManager.deleteFileByPath(card.backImagePath)
                _userFeedback.value = "Card '${card.title}' removed"
            }
        }
    }
}

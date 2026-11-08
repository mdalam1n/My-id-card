package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.DocType
import com.example.data.model.DocumentRecord
import com.example.data.model.PersonalCard
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class DocumentRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("personal_docs_store", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _documents = MutableStateFlow(loadDocuments())
    val documents: StateFlow> = _documents.asStateFlow()

    private val _personalCards = MutableStateFlow(loadPersonalCards())
    val personalCards: StateFlow<List<PersonalCard>> = _personalCards.asStateFlow()

    private fun loadUserProfile(): UserProfile {
        val name = prefs.getString("user_name", "My Name") ?: "My Name"
        val imagePath = prefs.getString("user_profile_image", null)
        return UserProfile(name = name, profileImagePath = imagePath)
    }

    private fun loadDocuments(): Map<DocType, DocumentRecord> {
        val map = mutableMapOf<DocType, DocumentRecord>()
        for (docType in DocType.entries) {
            val prefix = "doc_${docType.id}_"
            val docNumber = prefs.getString("${prefix}num", "") ?: ""
            val notes = prefs.getString("${prefix}notes", "") ?: ""
            val frontPath = prefs.getString("${prefix}front", null)
            val backPath = prefs.getString("${prefix}back", null)
            val updated = prefs.getLong("${prefix}updated", System.currentTimeMillis())

            map[docType] = DocumentRecord(
                id = docType.id,
                docType = docType,
                title = docType.title,
                documentNumber = docNumber,
                notes = notes,
                frontImagePath = frontPath,
                backImagePath = backPath,
                lastUpdated = updated
            )
        }
        return map
    }

    private fun loadPersonalCards(): List<PersonalCard> {
        val jsonStr = prefs.getString("personal_cards_json", null)
        if (jsonStr.isNullOrEmpty()) {
            return emptyList()
        }
        val list = mutableListOf<PersonalCard>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    PersonalCard(
                        id = obj.getString("id"),
                        slotIndex = obj.getInt("slotIndex"),
                        title = obj.getString("title"),
                        cardNumber = obj.optString("cardNumber", ""),
                        notes = obj.optString("notes", ""),
                        frontImagePath = if (obj.isNull("frontImagePath")) null else obj.getString("frontImagePath"),
                        backImagePath = if (obj.isNull("backImagePath")) null else obj.getString("backImagePath"),
                        lastUpdated = obj.optLong("lastUpdated", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedBy { it.slotIndex }
    }

    fun saveUserName(newName: String) {
        val trimmed = newName.trim()
        val finalName = if (trimmed.isEmpty()) "My Name" else trimmed
        prefs.edit().putString("user_name", finalName).apply()
        _userProfile.value = _userProfile.value.copy(name = finalName)
    }

    fun saveUserProfileImage(imagePath: String?) {
        prefs.edit().putString("user_profile_image", imagePath).apply()
        _userProfile.value = _userProfile.value.copy(profileImagePath = imagePath)
    }

    fun updateDocument(
        docType: DocType,
        documentNumber: String? = null,
        notes: String? = null,
        frontImagePath: String? = null,
        backImagePath: String? = null,
        clearFront: Boolean = false,
        clearBack: Boolean = false
    ) {
        val current = _documents.value[docType] ?: DocumentRecord(
            id = docType.id,
            docType = docType,
            title = docType.title
        )

        val newFront = if (clearFront) null else (frontImagePath ?: current.frontImagePath)
        val newBack = if (clearBack) null else (backImagePath ?: current.backImagePath)
        val newNum = documentNumber ?: current.documentNumber
        val newNotes = notes ?: current.notes
        val newUpdated = System.currentTimeMillis()

        val prefix = "doc_${docType.id}_"
        prefs.edit()
            .putString("${prefix}num", newNum)
            .putString("${prefix}notes", newNotes)
            .putString("${prefix}front", newFront)
            .putString("${prefix}back", newBack)
            .putLong("${prefix}updated", newUpdated)
            .apply()

        val updatedRecord = current.copy(
            documentNumber = newNum,
            notes = newNotes,
            frontImagePath = newFront,
            backImagePath = newBack,
            lastUpdated = newUpdated
        )

        val updatedMap = _documents.value.toMutableMap()
        updatedMap[docType] = updatedRecord
        _documents.value = updatedMap
    }

    fun addPersonalCard(
        title: String,
        cardNumber: String = "",
        notes: String = ""
    ): PersonalCard? {
        val currentCards = _personalCards.value
        // Removed the 5-card restriction completely for unlimited personal cards

        val nextSlot = currentCards.size

        val newCard = PersonalCard(
            id = "card_${System.currentTimeMillis()}_$nextSlot",
            slotIndex = nextSlot,
            title = if (title.isBlank()) "Personal Card ${nextSlot + 1}" else title.trim(),
            cardNumber = cardNumber.trim(),
            notes = notes.trim(),
            frontImagePath = null,
            backImagePath = null,
            lastUpdated = System.currentTimeMillis()
        )

        val updatedList = (currentCards + newCard).sortedBy { it.slotIndex }
        saveCardsList(updatedList)
        return newCard
    }

    fun updatePersonalCard(
        cardId: String,
        title: String? = null,
        cardNumber: String? = null,
        notes: String? = null,
        frontImagePath: String? = null,
        backImagePath: String? = null,
        clearFront: Boolean = false,
        clearBack: Boolean = false
    ) {
        val currentList = _personalCards.value
        val updatedList = currentList.map { card ->
            if (card.id == cardId) {
                card.copy(
                    title = title?.trim() ?: card.title,
                    cardNumber = cardNumber?.trim() ?: card.cardNumber,
                    notes = notes?.trim() ?: card.notes,
                    frontImagePath = if (clearFront) null else (frontImagePath ?: card.frontImagePath),
                    backImagePath = if (clearBack) null else (backImagePath ?: card.backImagePath),
                    lastUpdated = System.currentTimeMillis()
                )
            } else {
                card
            }
        }
        saveCardsList(updatedList)
    }

    fun deletePersonalCard(cardId: String): PersonalCard? {
        val currentList = _personalCards.value
        val cardToDelete = currentList.firstOrNull { it.id == cardId }
        val updatedList = currentList.filter { it.id != cardId }
        saveCardsList(updatedList)
        return cardToDelete
    }

    private fun saveCardsList(cards: List<PersonalCard>) {
        val jsonArray = JSONArray()
        for (card in cards) {
            val obj = JSONObject().apply {
                put("id", card.id)
                put("slotIndex", card.slotIndex)
                put("title", card.title)
                put("cardNumber", card.cardNumber)
                put("notes", card.notes)
                if (card.frontImagePath != null) {
                    put("frontImagePath", card.frontImagePath)
                } else {
                    put("frontImagePath", JSONObject.NULL)
                }
                if (card.backImagePath != null) {
                    put("backImagePath", card.backImagePath)
                } else {
                    put("backImagePath", JSONObject.NULL)
                }
                put("lastUpdated", card.lastUpdated)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("personal_cards_json", jsonArray.toString()).apply()
        _personalCards.value = cards
    }
}

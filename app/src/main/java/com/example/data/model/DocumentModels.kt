package com.example.data.model

enum class DocType(
    val id: String,
    val title: String,
    val subtitle: String
) {
    EKAMA(
        id = "ekama",
        title = "My Ekama (ID)",
        subtitle = "Resident & National Identity"
    ),
    DRIVING_LICENCE(
        id = "driving_licence",
        title = "My Driving Licence",
        subtitle = "Driver's License & Permits"
    ),
    HOME_ID(
        id = "home_id",
        title = "My Home ID",
        subtitle = "Residential & Property ID"
    ),
    PASSPORT(
        id = "passport",
        title = "My Passport",
        subtitle = "International Travel Document"
    );

    companion object {
        fun fromId(id: String): DocType {
            return entries.firstOrNull { it.id == id } ?: EKAMA
        }
    }
}

data class DocumentRecord(
    val id: String,
    val docType: DocType,
    val title: String,
    val documentNumber: String = "",
    val notes: String = "",
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val isComplete: Boolean
        get() = !frontImagePath.isNullOrEmpty() && !backImagePath.isNullOrEmpty()

    val hasAnyImage: Boolean
        get() = !frontImagePath.isNullOrEmpty() || !backImagePath.isNullOrEmpty()

    val statusText: String
        get() = when {
            isComplete -> "Front & Back Added"
            !frontImagePath.isNullOrEmpty() -> "Front Side Added"
            !backImagePath.isNullOrEmpty() -> "Back Side Added"
            else -> "Not Added"
        }
}

data class PersonalCard(
    val id: String,
    val slotIndex: Int, // 0 to 4 (1 to 5)
    val title: String = "Personal Card",
    val cardNumber: String = "",
    val notes: String = "",
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val isComplete: Boolean
        get() = !frontImagePath.isNullOrEmpty() && !backImagePath.isNullOrEmpty()

    val hasAnyImage: Boolean
        get() = !frontImagePath.isNullOrEmpty() || !backImagePath.isNullOrEmpty()

    val statusText: String
        get() = when {
            isComplete -> "Both Sides Added"
            !frontImagePath.isNullOrEmpty() -> "Front Side Added"
            !backImagePath.isNullOrEmpty() -> "Back Side Added"
            else -> "No Images Yet"
        }
}

data class UserProfile(
    val name: String = "Document Owner",
    val profileImagePath: String? = null
)

data class FullScreenPreviewTarget(
    val title: String,
    val imagePath: String
)

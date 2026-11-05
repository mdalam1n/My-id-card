package com.example

import com.example.data.model.DocType
import com.example.data.model.DocumentRecord
import com.example.data.model.PersonalCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testDocTypeDefinitions() {
        assertEquals("ekama", DocType.EKAMA.id)
        assertEquals("driving_licence", DocType.DRIVING_LICENCE.id)
        assertEquals("home_id", DocType.HOME_ID.id)
        assertEquals("passport", DocType.PASSPORT.id)

        assertEquals(DocType.EKAMA, DocType.fromId("ekama"))
        assertEquals(DocType.DRIVING_LICENCE, DocType.fromId("driving_licence"))
    }

    @Test
    fun testDocumentRecordCompletionStatus() {
        val emptyDoc = DocumentRecord(
            id = "ekama",
            docType = DocType.EKAMA,
            title = "My Ekama (ID)"
        )
        assertFalse(emptyDoc.isComplete)
        assertFalse(emptyDoc.hasAnyImage)
        assertEquals("Not Added", emptyDoc.statusText)

        val frontOnly = emptyDoc.copy(frontImagePath = "/data/user/0/app/files/doc_front.jpg")
        assertFalse(frontOnly.isComplete)
        assertTrue(frontOnly.hasAnyImage)
        assertEquals("Front Side Added", frontOnly.statusText)

        val completeDoc = frontOnly.copy(backImagePath = "/data/user/0/app/files/doc_back.jpg")
        assertTrue(completeDoc.isComplete)
        assertTrue(completeDoc.hasAnyImage)
        assertEquals("Front & Back Added", completeDoc.statusText)
    }

    @Test
    fun testPersonalCardStatus() {
        val card = PersonalCard(
            id = "card_1",
            slotIndex = 0,
            title = "Gym Card",
            cardNumber = "GYM-123",
            frontImagePath = "/path/front.jpg",
            backImagePath = "/path/back.jpg"
        )
        assertTrue(card.isComplete)
        assertEquals("Both Sides Added", card.statusText)
    }
}

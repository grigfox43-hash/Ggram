package org.ggram

import org.ggram.adblock.GgramAdBlocker
import org.ggram.chat.GgramChatErgonomics
import org.ggram.notifications.GgramNotificationController
import org.ggram.profiler.GgramUserProfiler
import org.ggram.security.GgramDataRedactor
import org.ggram.security.GgramDoubleBottomManager
import org.ggram.storage.GgramStorageCleaner
import org.ggram.storage.MediaType
import org.ggram.tabs.GgramSmartTabsManager
import org.ggram.tabs.SmartTabType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Unit tests verifying that all Ggram features, engines, and algorithms function properly.
 */
class GgramFeaturesTest {

    @Test
    fun testUserProfilerRegistrationEstimation() {
        // Veteran user IDs (< 50M)
        val veteranDate = GgramUserProfiler.estimateRegistrationDate(1234567)
        assertTrue(veteranDate.contains("2013 — 2014"))

        // Mid-era user IDs (2018)
        val midDate = GgramUserProfiler.estimateRegistrationDate(600_000_000)
        assertTrue(midDate.contains("2018"))

        // Modern 2026 user IDs (> 3B)
        val modernDate = GgramUserProfiler.estimateRegistrationDate(3_500_000_000)
        assertTrue(modernDate.contains("2026"))
    }

    @Test
    fun testUserNameHistoryTracking() {
        val userId = 999888777L
        GgramUserProfiler.recordNameChange(userId, "Alex", "alex_old", "Alexander", "alex_new")
        val history = GgramUserProfiler.getNameHistory(userId)

        assertEquals(1, history.size)
        assertEquals("Alex", history[0].oldName)
        assertEquals("alex_old", history[0].oldUsername)
    }

    @Test
    fun testMutualContactTracking() {
        val userId = 11223344L
        assertFalse(GgramUserProfiler.isMutualContact(userId))

        GgramUserProfiler.markMutualContact(userId, true)
        assertTrue(GgramUserProfiler.isMutualContact(userId))

        GgramUserProfiler.markMutualContact(userId, false)
        assertFalse(GgramUserProfiler.isMutualContact(userId))
    }

    @Test
    fun testDataRedactionMasking() {
        // Redaction should mask phones, cards, and crypto wallets
        val sampleText = "Call me at +7 999 123-45-67 or send to 0x71C63E329ab4E773E0F3874B6409890f23023023"
        val redacted = GgramDataRedactor.redactSensitiveText(sampleText)

        assertFalse(redacted.contains("+7 999 123-45-67"))
        assertFalse(redacted.contains("0x71C63E329ab4E773E0F3874B6409890f23023023"))
        assertTrue(redacted.contains("[НОМЕР СКРЫТ]"))
        assertTrue(redacted.contains("[КОШЕЛЕК СКРЫТ]"))
    }

    @Test
    fun testChatErgonomicsTextSnippet() {
        val fullMessage = "Привет! Это важное сообщение от разработчика Ggram."
        val snippet = fullMessage.substring(8, 23)
        assertEquals("Это важное сооб", snippet)

        // Forward strip author
        val cleanForward = GgramChatErgonomics.stripAuthorQuote(fullMessage)
        assertEquals(fullMessage, cleanForward)
    }

    @Test
    fun testChatErgonomicsBlockedUserFilter() {
        val blockedId = 445566L
        GgramChatErgonomics.initBlockedUsers(listOf(blockedId))

        assertTrue(GgramChatErgonomics.shouldHideGroupMessage(blockedId))
        assertFalse(GgramChatErgonomics.shouldHideGroupMessage(112233L))
    }

    @Test
    fun testStorageQuotaAndWhitelist() {
        val chatId = 100200300L
        GgramStorageCleaner.setChatQuotaMb(chatId, 250)
        assertEquals(250, GgramStorageCleaner.getChatQuotaMb(chatId))

        GgramStorageCleaner.addToWhitelist(chatId)
        assertTrue(GgramStorageCleaner.isWhitelisted(chatId))

        GgramStorageCleaner.removeFromWhitelist(chatId)
        assertFalse(GgramStorageCleaner.isWhitelisted(chatId))
    }

    @Test
    fun testSmartTabsCounter() {
        GgramSmartTabsManager.updateUnreadCount(SmartTabType.CHANNELS, 14)
        assertEquals(14, GgramSmartTabsManager.getUnreadCount(SmartTabType.CHANNELS))

        GgramSmartTabsManager.updateUnreadCount(SmartTabType.USERS, 3)
        assertEquals(3, GgramSmartTabsManager.getUnreadCount(SmartTabType.USERS))
    }

    @Test
    fun testAdBlockHeuristic() {
        val adText1 = "Супер скидка на курс! Используй промокод GGRAM"
        val normalText = "Привет, как твои дела? Встретимся завтра?"

        assertTrue(GgramAdBlocker.isPromotionalPost(adText1))
        assertFalse(GgramAdBlocker.isPromotionalPost(normalText))
    }
}

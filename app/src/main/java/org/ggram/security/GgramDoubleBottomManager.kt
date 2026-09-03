package org.ggram.security

import android.content.Context
import android.util.Log
import org.ggram.config.GgramConfig
import java.security.MessageDigest

/**
 * GgramDoubleBottomManager - Multi-passcode & hidden chats security vault.
 * Supports primary PIN and secondary Fake/Panic PIN.
 */
object GgramDoubleBottomManager {

    private const val TAG = "GgramDoubleBottom"

    private val hiddenChatIds = HashSet<Long>()
    private var isUnlockedWithFakePin = false

    fun init(context: Context) {
        Log.i(TAG, "GgramDoubleBottomManager initialized")
        loadHiddenChats()
    }

    /**
     * Verifies entered PIN code.
     * Returns:
     * - PinResult.REAL: Standard unlock
     * - PinResult.FAKE: Decoy / Panic unlock (hides all secret chats)
     * - PinResult.INVALID: Incorrect code
     */
    enum class PinResult {
        REAL,
        FAKE,
        INVALID
    }

    fun verifyPasscode(inputPin: String): PinResult {
        val inputHash = hashPin(inputPin)
        val realHash = GgramConfig.realPinHash
        val fakeHash = GgramConfig.fakePinHash

        return when {
            realHash.isNotEmpty() && inputHash == realHash -> {
                isUnlockedWithFakePin = false
                Log.i(TAG, "Unlocked with primary master PIN")
                PinResult.REAL
            }
            fakeHash.isNotEmpty() && inputHash == fakeHash -> {
                isUnlockedWithFakePin = true
                Log.w(TAG, "⚠️ Unlocked with Fake/Panic PIN. Concealing secret vault!")
                PinResult.FAKE
            }
            else -> PinResult.INVALID
        }
    }

    fun isChatVisible(chatId: Long): Boolean {
        if (!GgramConfig.isDoubleBottomEnabled) return true
        if (isUnlockedWithFakePin && hiddenChatIds.contains(chatId)) {
            return false // Hidden from fake profile
        }
        return true
    }

    fun setChatHidden(chatId: Long, isHidden: Boolean) {
        if (isHidden) {
            hiddenChatIds.add(chatId)
        } else {
            hiddenChatIds.remove(chatId)
        }
        saveHiddenChats()
    }

    fun isChatMarkedHidden(chatId: Long): Boolean {
        return hiddenChatIds.contains(chatId)
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun loadHiddenChats() {
        // Loads stored hidden chat IDs
    }

    private fun saveHiddenChats() {
        // Persists hidden chat IDs
    }
}

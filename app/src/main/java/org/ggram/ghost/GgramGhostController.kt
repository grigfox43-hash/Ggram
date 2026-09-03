package org.ggram.ghost

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import org.ggram.R
import org.ggram.config.GgramConfig

/**
 * GgramGhostController - Privacy and stealth manager.
 * Intercepts read receipts, typing state broadcasts, online presence, and stories views.
 * Also provides safety confirmation modals for voice notes, video circles, and calls.
 */
object GgramGhostController {

    private const val TAG = "GgramGhostController"

    // Set of dialog IDs where read receipts were explicitly permitted (e.g. after user reply)
    private val permittedReadChats = HashSet<Long>()

    fun init(context: Context) {
        Log.i(TAG, "GgramGhostController initialized")
    }

    /**
     * Determines whether messages.readHistory should be broadcast to Telegram MTProto servers.
     */
    fun shouldSendReadHistory(chatId: Long): Boolean {
        if (!GgramConfig.isGhostReadEnabled) {
            return true
        }

        // If Read on Reply is enabled and user sent a message in this chat, allow read receipt
        if (GgramConfig.isReadOnReply && permittedReadChats.contains(chatId)) {
            return true
        }

        Log.d(TAG, "Ghost Mode: Blocked read receipt for chat $chatId")
        return false
    }

    /**
     * Callback triggered when user sends a message.
     * If Read on Reply is enabled, mark this chat as permitted to update read history.
     */
    fun onMessageSent(chatId: Long) {
        if (GgramConfig.isReadOnReply) {
            permittedReadChats.add(chatId)
            Log.d(TAG, "Read on Reply triggered for chat $chatId")
        }
    }

    /**
     * Blocks messages.setTyping, recording audio, or uploading presence indicators.
     */
    fun shouldSendTypingIndicator(): Boolean {
        return !GgramConfig.isGhostTypingEnabled
    }

    /**
     * Blocks online status updates to keep user permanently offline.
     */
    fun shouldSendOnlineStatus(): Boolean {
        return !GgramConfig.isGhostOnlineEnabled
    }

    /**
     * Blocks stories.readStories so user remains anonymous when viewing stories.
     */
    fun shouldSendStoriesRead(): Boolean {
        return !GgramConfig.isGhostStoriesEnabled
    }

    /**
     * Helper to show action confirmation modal.
     */
    fun showConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        positiveText: String,
        onConfirmed: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { _, _ -> onConfirmed() }
            .setNegativeButton(context.getString(R.string.action_cancel), null)
            .show()
    }
}

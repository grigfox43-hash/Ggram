package org.ggram.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import org.ggram.config.GgramConfig

/**
 * GgramChatErgonomics - Chat flow and interaction enhancements.
 * 1. Partial text selection and copying.
 * 2. 1-tap forward without author/quote.
 * 3. Jump to first message in history.
 * 4. Filter blocked users completely in group feeds.
 */
object GgramChatErgonomics {

    private const val TAG = "GgramErgonomics"
    private val blockedUserIds = HashSet<Long>()

    fun initBlockedUsers(ids: Collection<Long>) {
        blockedUserIds.clear()
        blockedUserIds.addAll(ids)
    }

    /**
     * Determines whether a message from a blocked user should be hidden in group chats.
     */
    fun shouldHideGroupMessage(senderId: Long): Boolean {
        if (!GgramConfig.isHideBlockedInGroups) return false
        return blockedUserIds.contains(senderId)
    }

    /**
     * Copies a selected snippet of a message to the system clipboard.
     */
    fun copyTextSnippet(context: Context, fullText: String, startIndex: Int, endIndex: Int): String {
        val snippet = if (startIndex in 0..endIndex && endIndex <= fullText.length) {
            fullText.substring(startIndex, endIndex)
        } else {
            fullText
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Ggram snippet", snippet)
        clipboard.setPrimaryClip(clip)
        Log.i(TAG, "Copied snippet: '${snippet.take(20)}...'")
        return snippet
    }

    /**
     * Prepares message content for forwarding without original author attribution.
     */
    fun stripAuthorQuote(originalMessageText: String): String {
        return originalMessageText // Plain clean text sent as user's own message
    }

    /**
     * Calculates the target message ID to jump to the very beginning of a chat or channel.
     */
    fun getFirstMessageTargetId(): Int {
        return 1 // Earliest message sequence ID in MTProto dialog history
    }
}

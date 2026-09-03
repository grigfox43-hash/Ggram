package org.ggram.antirecall

import android.content.Context
import android.util.Log
import org.ggram.config.GgramConfig
import java.util.concurrent.ConcurrentHashMap

data class DeletedMessageRecord(
    val messageId: Long,
    val chatId: Long,
    val senderName: String,
    val originalText: String,
    val deletedTimestamp: Long
)

data class MessageEditRecord(
    val revisionId: Int,
    val previousText: String,
    val newText: String,
    val timestamp: Long
)

/**
 * GgramAntiRecallManager - Manages retention of deleted messages, edit history, and disappearing media.
 */
object GgramAntiRecallManager {

    private const val TAG = "GgramAntiRecall"

    private val deletedMessagesMap = ConcurrentHashMap<Long, DeletedMessageRecord>()
    private val messageEditsMap = ConcurrentHashMap<Long, MutableList<MessageEditRecord>>()

    fun init(context: Context) {
        Log.i(TAG, "GgramAntiRecallManager initialized")
    }

    /**
     * Intercepts SQLite message deletion calls.
     * If anti-recall is enabled, saves the message state and marks it as deleted instead of purging.
     */
    fun onMessageDeleted(chatId: Long, messageId: Long, text: String, sender: String) {
        if (!GgramConfig.isAntiRecallDeleted) return

        val record = DeletedMessageRecord(
            messageId = messageId,
            chatId = chatId,
            senderName = sender,
            originalText = text,
            deletedTimestamp = System.currentTimeMillis()
        )
        deletedMessagesMap[messageId] = record
        Log.d(TAG, "Preserved deleted message ID $messageId in chat $chatId")
    }

    /**
     * Tracks message edits, keeping a chronological revision trail.
     */
    fun onMessageEdited(messageId: Long, oldText: String, newText: String) {
        if (!GgramConfig.isAntiRecallEdits) return

        val edits = messageEditsMap.getOrPut(messageId) { mutableListOf() }
        val revision = MessageEditRecord(
            revisionId = edits.size + 1,
            previousText = oldText,
            newText = newText,
            timestamp = System.currentTimeMillis()
        )
        edits.add(revision)
        Log.d(TAG, "Stored revision #${revision.revisionId} for message $messageId")
    }

    fun isMessageDeleted(messageId: Long): Boolean {
        return deletedMessagesMap.containsKey(messageId)
    }

    fun getDeletedRecord(messageId: Long): DeletedMessageRecord? {
        return deletedMessagesMap[messageId]
    }

    fun getEditHistory(messageId: Long): List<MessageEditRecord> {
        return messageEditsMap[messageId] ?: emptyList()
    }
}

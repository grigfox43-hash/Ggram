package org.ggram.storage

import android.content.Context
import android.util.Log
import org.ggram.config.GgramConfig
import java.io.File
import java.util.concurrent.ConcurrentHashMap

enum class MediaType {
    VIDEO_CIRCLES,
    VOICE_NOTES,
    STICKERS,
    VIDEOS,
    PHOTOS,
    DOCUMENTS
}

/**
 * GgramStorageCleaner - Advanced cache quota and selective storage cleaner.
 * Prevents Telegram from bloating phone storage with granular cleanup policies.
 */
object GgramStorageCleaner {

    private const val TAG = "GgramStorage"

    // Quotas in megabytes per chat (-1 = unlimited)
    private val chatQuotasMb = ConcurrentHashMap<Long, Int>()
    private val whitelistChatIds = HashSet<Long>()

    fun setChatQuotaMb(chatId: Long, quotaMb: Int) {
        chatQuotasMb[chatId] = quotaMb
    }

    fun getChatQuotaMb(chatId: Long): Int {
        return chatQuotasMb[chatId] ?: GgramConfig.defaultChannelQuotaMb
    }

    fun addToWhitelist(chatId: Long) {
        whitelistChatIds.add(chatId)
    }

    fun removeFromWhitelist(chatId: Long) {
        whitelistChatIds.remove(chatId)
    }

    fun isWhitelisted(chatId: Long): Boolean {
        return whitelistChatIds.contains(chatId)
    }

    /**
     * Cleans cache files matching specific media type (e.g. video circles or stickers).
     */
    fun cleanMediaByType(cacheDir: File, type: MediaType): Long {
        var freedBytes = 0L
        val extensions = when (type) {
            MediaType.VIDEO_CIRCLES, MediaType.VIDEOS -> listOf(".mp4", ".mov")
            MediaType.VOICE_NOTES -> listOf(".ogg", ".opus", ".mp3")
            MediaType.STICKERS -> listOf(".tgs", ".webm", ".webp")
            MediaType.PHOTOS -> listOf(".jpg", ".jpeg", ".png")
            MediaType.DOCUMENTS -> listOf(".pdf", ".zip", ".apk")
        }

        cacheDir.listFiles()?.forEach { file ->
            if (file.isFile && extensions.any { file.name.endsWith(it, ignoreCase = true) }) {
                val size = file.length()
                if (file.delete()) {
                    freedBytes += size
                }
            }
        }
        Log.i(TAG, "Freed $freedBytes bytes cleaning ${type.name}")
        return freedBytes
    }

    /**
     * Enforces storage quota on a specific chat directory.
     */
    fun enforceChatQuota(chatDir: File, chatId: Long): Long {
        if (isWhitelisted(chatId)) {
            Log.d(TAG, "Chat $chatId is whitelisted from storage cleaner")
            return 0L
        }

        val quotaMb = getChatQuotaMb(chatId)
        if (quotaMb <= 0) return 0L

        val maxBytes = quotaMb.toLong() * 1024 * 1024
        val files = chatDir.listFiles()?.sortedBy { it.lastModified() } ?: return 0L
        var totalSize = files.sumOf { it.length() }
        var freed = 0L

        for (file in files) {
            if (totalSize <= maxBytes) break
            val size = file.length()
            if (file.delete()) {
                totalSize -= size
                freed += size
            }
        }
        return freed
    }
}

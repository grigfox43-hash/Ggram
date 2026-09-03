package org.ggram.profiler

import android.content.Context
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

data class NameHistoryEntry(
    val oldName: String,
    val oldUsername: String?,
    val timestamp: Long
)

/**
 * GgramUserProfiler - Deep user intelligence and profile inspector.
 * 1. Estimates Telegram account registration date from numeric ID.
 * 2. Determines mutual contact status.
 * 3. Tracks historical name & username changes.
 */
object GgramUserProfiler {

    private const val TAG = "GgramProfiler"
    private val nameHistoryMap = ConcurrentHashMap<Long, MutableList<NameHistoryEntry>>()
    private val mutualContactsSet = HashSet<Long>()

    /**
     * Estimates account registration date based on Telegram's sequential user ID allocation.
     */
    fun estimateRegistrationDate(userId: Long): String {
        return when {
            userId <= 0 -> "Неизвестно"
            userId < 50_000_000 -> "2013 — 2014 год (Ветеран Telegram)"
            userId < 150_000_000 -> "2015 год"
            userId < 300_000_000 -> "2016 год"
            userId < 500_000_000 -> "2017 год"
            userId < 750_000_000 -> "2018 год"
            userId < 1_000_000_000 -> "2019 — 2020 год"
            userId < 1_500_000_000 -> "2021 год"
            userId < 2_000_000_000 -> "2022 — 2023 год"
            userId < 3_000_000_000 -> "2024 — 2025 год"
            else -> "2026 год (Новый аккаунт)"
        }
    }

    /**
     * Records a name or username change if detected.
     */
    fun recordNameChange(userId: Long, currentName: String, currentUsername: String?, newName: String, newUsername: String?) {
        if (currentName != newName || currentUsername != newUsername) {
            val list = nameHistoryMap.getOrPut(userId) { mutableListOf() }
            val entry = NameHistoryEntry(currentName, currentUsername, System.currentTimeMillis())
            list.add(entry)
            Log.i(TAG, "Recorded name change for user $userId: '$currentName' -> '$newName'")
        }
    }

    fun getNameHistory(userId: Long): List<NameHistoryEntry> {
        return nameHistoryMap[userId] ?: emptyList()
    }

    fun markMutualContact(userId: Long, isMutual: Boolean) {
        if (isMutual) mutualContactsSet.add(userId) else mutualContactsSet.remove(userId)
    }

    fun isMutualContact(userId: Long): Boolean {
        return mutualContactsSet.contains(userId)
    }
}

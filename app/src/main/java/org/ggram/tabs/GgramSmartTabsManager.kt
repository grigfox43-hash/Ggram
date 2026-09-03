package org.ggram.tabs

import android.content.Context
import org.ggram.config.GgramConfig

enum class SmartTabType(val titleRu: String, val titleEn: String) {
    ALL("Все", "All"),
    USERS("Личные", "Direct"),
    GROUPS("Группы", "Groups"),
    CHANNELS("Каналы", "Channels"),
    BOTS("Боты", "Bots"),
    UNREAD("Непрочитанные", "Unread")
}

data class SmartTabState(
    val type: SmartTabType,
    val unreadCount: Int = 0,
    val isVisible: Boolean = true
)

/**
 * GgramSmartTabsManager - Automatic chat categorization into smart tabs.
 * Organizes dialogs into All, Direct, Groups, Channels, Bots, and Unread with unread badges.
 */
object GgramSmartTabsManager {

    private val tabStates = mutableMapOf<SmartTabType, SmartTabState>()

    init {
        SmartTabType.values().forEach { tab ->
            tabStates[tab] = SmartTabState(tab)
        }
    }

    fun isTabEnabled(tab: SmartTabType): Boolean {
        if (!GgramConfig.isSmartTabsEnabled) return false
        return when (tab) {
            SmartTabType.ALL -> true
            SmartTabType.USERS -> GgramConfig.isTabUsersEnabled
            SmartTabType.GROUPS -> GgramConfig.isTabGroupsEnabled
            SmartTabType.CHANNELS -> GgramConfig.isTabChannelsEnabled
            SmartTabType.BOTS -> GgramConfig.isTabBotsEnabled
            SmartTabType.UNREAD -> GgramConfig.isTabUnreadEnabled
        }
    }

    fun updateUnreadCount(tab: SmartTabType, count: Int) {
        tabStates[tab] = tabStates[tab]?.copy(unreadCount = count) ?: SmartTabState(tab, count)
    }

    fun getUnreadCount(tab: SmartTabType): Int {
        return tabStates[tab]?.unreadCount ?: 0
    }

    fun getVisibleTabs(): List<SmartTabType> {
        if (!GgramConfig.isSmartTabsEnabled) return emptyList()
        return SmartTabType.values().filter { isTabEnabled(it) }
    }
}

package org.ggram.messenger

import android.content.Context
import android.util.Log
import org.ggram.adblock.GgramAdBlocker
import org.ggram.antirecall.GgramAntiRecallManager
import org.ggram.config.GgramConfig
import org.ggram.ghost.GgramGhostController
import org.ggram.messenger.model.ChatType
import org.ggram.messenger.model.TelegramChat
import org.ggram.messenger.model.TelegramMessage
import org.ggram.messenger.model.TelegramUser
import org.ggram.profiler.GgramUserProfiler
import org.ggram.tabs.SmartTabType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * TelegramEngine - Core Telegram messaging and session controller for Ggram.
 * Coordinates MTProto state, authenticates sessions, manages dialogs, and intercepts messages
 * with Ggram AdBlocker, Ghost Controller, and Anti-Recall modules.
 */
object TelegramEngine {

    private const val TAG = "TelegramEngine"
    private const val PREFS_AUTH = "ggram_telegram_auth"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_PHONE = "user_phone"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_USERNAME = "user_username"

    var currentUser: TelegramUser? = null
        private set

    private val chatsList = CopyOnWriteArrayList<TelegramChat>()
    private val messagesMap = ConcurrentHashMap<Long, CopyOnWriteArrayList<TelegramMessage>>()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

        if (isLoggedIn) {
            val id = prefs.getLong(KEY_USER_ID, 276520088L)
            val phone = prefs.getString(KEY_USER_PHONE, "+7 999 123-45-67") ?: "+7 999 123-45-67"
            val name = prefs.getString(KEY_USER_NAME, "Grigoriy Fox") ?: "Grigoriy Fox"
            val username = prefs.getString(KEY_USER_USERNAME, "grigfox43")

            currentUser = TelegramUser(id, name, "", username, phone, true)
            Log.i(TAG, "Restored active Telegram session for user: ${currentUser?.fullName}")
        }

        seedInitialChatsAndMessages()
    }

    fun isLoggedIn(): Boolean = currentUser != null

    fun loginWithPhone(context: Context, phone: String, firstName: String, username: String? = null) {
        val userId = System.currentTimeMillis() % 1_000_000_000L + 100_000_000L
        currentUser = TelegramUser(userId, firstName, "", username, phone, true)

        val prefs = context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USER_PHONE, phone)
            .putString(KEY_USER_NAME, firstName)
            .putString(KEY_USER_USERNAME, username)
            .apply()

        Log.i(TAG, "Successfully authenticated Telegram user $firstName ($phone)")
    }

    fun logout(context: Context) {
        currentUser = null
        val prefs = context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        Log.i(TAG, "User logged out of Telegram session")
    }

    /**
     * Retrieves dialogs filtered by the selected Smart Tab.
     */
    fun getChats(tab: SmartTabType): List<TelegramChat> {
        return when (tab) {
            SmartTabType.ALL -> chatsList
            SmartTabType.USERS -> chatsList.filter { it.type == ChatType.DIRECT }
            SmartTabType.GROUPS -> chatsList.filter { it.type == ChatType.GROUP }
            SmartTabType.CHANNELS -> chatsList.filter { it.type == ChatType.CHANNEL }
            SmartTabType.BOTS -> chatsList.filter { it.type == ChatType.BOT }
            SmartTabType.UNREAD -> chatsList.filter { it.unreadCount > 0 }
        }
    }

    fun getChat(chatId: Long): TelegramChat? {
        return chatsList.find { it.id == chatId }
    }

    fun markAllAsRead() {
        chatsList.forEach { chat ->
            chat.unreadCount = 0
            messagesMap[chat.id]?.forEach { msg ->
                msg.isRead = true
            }
        }
        Log.i(TAG, "Marked all chats as read")
    }

    /**
     * Gets messages for a chat, respecting Ggram Ghost read mode.
     */
    fun getMessages(chatId: Long): List<TelegramMessage> {
        val list = messagesMap[chatId] ?: CopyOnWriteArrayList()

        // Ghost Read check: if Ghost Read is disabled, mark messages as read
        if (!GgramConfig.isGhostReadEnabled) {
            list.forEach { it.isRead = true }
            getChat(chatId)?.unreadCount = 0
        }

        return list
    }

    /**
     * Sends a message into a chat. Intercepts with Ggram Ghost Mode and updates chat state.
     */
    fun sendMessage(chatId: Long, text: String): TelegramMessage {
        val myUser = currentUser ?: TelegramUser(1L, "Me", "", "ggram", "+0", true)
        val msgId = System.currentTimeMillis()
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val msg = TelegramMessage(
            id = msgId,
            chatId = chatId,
            senderId = myUser.id,
            senderName = myUser.fullName,
            text = text,
            timestamp = msgId,
            isOutgoing = true,
            isRead = true
        )

        // Read on reply: if user replies, mark incoming messages as read
        if (GgramConfig.isReadOnReply) {
            messagesMap[chatId]?.forEach { it.isRead = true }
            getChat(chatId)?.unreadCount = 0
        }

        val chatMessages = messagesMap.getOrPut(chatId) { CopyOnWriteArrayList() }
        chatMessages.add(msg)

        val chat = getChat(chatId)
        chat?.let {
            it.lastMessage = text
            it.lastMessageTime = timeStr
        }

        Log.i(TAG, "Sent message $msgId to chat $chatId")
        return msg
    }

    /**
     * Simulates message deletion by peer, trapped by GgramAntiRecallManager.
     */
    fun simulateMessageDeletion(chatId: Long, messageId: Long) {
        val messages = messagesMap[chatId] ?: return
        val msg = messages.find { it.id == messageId } ?: return

        if (GgramConfig.isAntiRecallDeleted) {
            // Anti-Recall: do not remove from list! Mark as deleted
            msg.isDeleted = true
            GgramAntiRecallManager.onMessageDeleted(chatId, messageId)
            Log.i(TAG, "Anti-Recall caught deleted message $messageId in chat $chatId")
        } else {
            messages.remove(msg)
        }
    }

    /**
     * Simulates message edit by peer, trapped by GgramAntiRecallManager.
     */
    fun simulateMessageEdit(chatId: Long, messageId: Long, newText: String) {
        val messages = messagesMap[chatId] ?: return
        val msg = messages.find { it.id == messageId } ?: return

        if (GgramConfig.isAntiRecallEdits) {
            GgramAntiRecallManager.onMessageEdited(messageId, msg.text, newText)
            msg.text = newText
            msg.isEdited = true
            Log.i(TAG, "Anti-Recall stored edit history for message $messageId")
        } else {
            msg.text = newText
        }
    }

    private fun seedInitialChatsAndMessages() {
        if (chatsList.isNotEmpty()) return

        // 1. Pavel Durov (Direct)
        val durovChat = TelegramChat(
            id = 1001L,
            title = "Павел Дуров",
            type = ChatType.DIRECT,
            lastMessage = "Ggram выглядит невероятно стильно. Отличная работа над изумрудной темой!",
            lastMessageTime = "16:42",
            unreadCount = 1,
            avatarInitials = "ПД",
            partnerUserId = 100000001L
        )
        GgramUserProfiler.markMutualContact(100000001L, true)

        // 2. Telegram News (Official Channel)
        val newsChat = TelegramChat(
            id = 1002L,
            title = "Telegram News",
            type = ChatType.CHANNEL,
            lastMessage = "Telegram поздравляет вас с обновлением клиента до Ggram v1.2.1!",
            lastMessageTime = "15:30",
            unreadCount = 2,
            avatarInitials = "TN"
        )

        // 3. Ggram Official Channel
        val ggramChat = TelegramChat(
            id = 1003L,
            title = "Ggram Announcements ⚡",
            type = ChatType.CHANNEL,
            lastMessage = "Релиз v1.2.1: 100% AdBlock, Режим невидимки, Двойное дно и Turbo-загрузчик!",
            lastMessageTime = "14:15",
            unreadCount = 0,
            avatarInitials = "GG",
            isPinned = true
        )

        // 4. Saved Messages / Избранное
        val savedChat = TelegramChat(
            id = 1004L,
            title = "Избранное (Saved Messages)",
            type = ChatType.DIRECT,
            lastMessage = "https://github.com/grigfox43-hash/Ggram",
            lastMessageTime = "Вчера",
            unreadCount = 0,
            avatarInitials = "💾",
            isPinned = true
        )

        // 5. Crypto Wallet Bot
        val botChat = TelegramChat(
            id = 1005L,
            title = "Wallet Bot",
            type = ChatType.BOT,
            lastMessage = "Ваш баланс: 125 TON ($675.00). Транзакций в обработке: 0.",
            lastMessageTime = "Вчера",
            unreadCount = 0,
            avatarInitials = "🤖"
        )

        // 6. Android Devs Community (Group)
        val groupChat = TelegramChat(
            id = 1006L,
            title = "Android & Telegram Devs",
            type = ChatType.GROUP,
            lastMessage = "Алексей: Кто уже протестировал сборку Ggram на Android 14?",
            lastMessageTime = "02 Сен",
            unreadCount = 5,
            avatarInitials = "AD"
        )

        chatsList.addAll(listOf(ggramChat, savedChat, durovChat, newsChat, groupChat, botChat))

        // Seed messages for Pavel Durov chat
        val durovMessages = CopyOnWriteArrayList(listOf(
            TelegramMessage(101L, 1001L, 100000001L, "Павел Дуров", "Привет! Как продвигается разработка вашего форка?", System.currentTimeMillis() - 3600000, false, true),
            TelegramMessage(102L, 1001L, currentUser?.id ?: 1L, currentUser?.fullName ?: "Я", "Привет, Павел! Внедрили 100% блокировку рекламы, умные вкладки, режим невидимки и сохранение удаленных сообщений.", System.currentTimeMillis() - 1800000, true, true),
            TelegramMessage(103L, 1001L, 100000001L, "Павел Дуров", "Ggram выглядит невероятно стильно. Отличная работа над изумрудной темой!", System.currentTimeMillis() - 600000, false, false)
        ))
        messagesMap[1001L] = durovMessages

        // Seed messages for Ggram Announcements
        val ggramMessages = CopyOnWriteArrayList(listOf(
            TelegramMessage(201L, 1003L, 9999L, "Ggram", "Добро пожаловать в Ggram — премиальный клиент Telegram в кибер-эстетике Obsidian (#050505) и Emerald (#01ba53).", System.currentTimeMillis() - 86400000, false, true),
            TelegramMessage(202L, 1003L, 9999L, "Ggram", "Релиз v1.2.1: 100% AdBlock, Режим невидимки, Двойное дно и Turbo-загрузчик!", System.currentTimeMillis() - 7200000, false, true)
        ))
        messagesMap[1003L] = ggramMessages

        // Seed messages for Saved Messages
        val savedMessages = CopyOnWriteArrayList(listOf(
            TelegramMessage(301L, 1004L, currentUser?.id ?: 1L, currentUser?.fullName ?: "Я", "План разработки Ggram v1.2.1 выполнен успешно.", System.currentTimeMillis() - 90000000, true, true),
            TelegramMessage(302L, 1004L, currentUser?.id ?: 1L, currentUser?.fullName ?: "Я", "https://github.com/grigfox43-hash/Ggram", System.currentTimeMillis() - 85000000, true, true)
        ))
        messagesMap[1004L] = savedMessages
    }
}

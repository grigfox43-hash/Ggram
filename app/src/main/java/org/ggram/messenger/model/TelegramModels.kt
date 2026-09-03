package org.ggram.messenger.model

enum class ChatType {
    DIRECT,
    GROUP,
    CHANNEL,
    BOT
}

data class TelegramUser(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val username: String?,
    val phone: String,
    val isOnline: Boolean = true
) {
    val fullName: String get() = if (lastName.isNotEmpty()) "$firstName $lastName" else firstName
}

data class TelegramChat(
    val id: Long,
    val title: String,
    val type: ChatType,
    var lastMessage: String,
    var lastMessageTime: String,
    var unreadCount: Int = 0,
    val avatarInitials: String,
    var isMuted: Boolean = false,
    var isPinned: Boolean = false,
    val partnerUserId: Long? = null
)

data class TelegramMessage(
    val id: Long,
    val chatId: Long,
    val senderId: Long,
    val senderName: String,
    var text: String,
    val timestamp: Long,
    val isOutgoing: Boolean,
    var isRead: Boolean = true,
    var isDeleted: Boolean = false,
    var isEdited: Boolean = false,
    val mediaType: String? = null
)

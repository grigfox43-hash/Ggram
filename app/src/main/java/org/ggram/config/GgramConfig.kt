package org.ggram.config

import android.content.Context
import android.content.SharedPreferences

/**
 * Centralized reactive configuration singleton for Ggram.
 * Persists all user preferences, stealth toggles, adblock filters, and UI styles.
 */
object GgramConfig {

    private const val PREFS_NAME = "ggram_preferences"

    // Keys
    private const val KEY_GHOST_READ = "ghost_read_mode"
    private const val KEY_GHOST_READ_REPLY = "ghost_read_on_reply"
    private const val KEY_GHOST_TYPING = "ghost_stealth_typing"
    private const val KEY_GHOST_ONLINE = "ghost_lock_offline"
    private const val KEY_GHOST_STORIES = "ghost_stealth_stories"

    private const val KEY_CONFIRM_VOICE = "confirm_voice_msg"
    private const val KEY_CONFIRM_VIDEO = "confirm_video_msg"
    private const val KEY_CONFIRM_STICKERS = "confirm_stickers"
    private const val KEY_CONFIRM_CALLS = "confirm_calls"

    private const val KEY_ADBLOCK_SPONSORED = "adblock_sponsored"
    private const val KEY_ADBLOCK_CHANNEL_POSTS = "adblock_channel_posts"
    private const val KEY_ADBLOCK_PREMIUM = "adblock_premium_upsell"

    private const val KEY_ANTIRECALL_DELETED = "antirecall_save_deleted"
    private const val KEY_ANTIRECALL_EDITS = "antirecall_edit_history"
    private const val KEY_ANTIRECALL_MEDIA = "antirecall_save_media"

    private const val KEY_BYPASS_FLAG_SECURE = "bypass_flag_secure"
    private const val KEY_BYPASS_NOFORWARDS = "bypass_noforwards"

    private const val KEY_UI_BOTTOM_NAV = "ui_bottom_nav"
    private const val KEY_UI_BUBBLE_RADIUS = "ui_bubble_radius"
    private const val KEY_UI_SHOW_ID_DC = "ui_show_id_dc"
    private const val KEY_UI_UNLIMITED_PINS = "ui_unlimited_pins"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Ghost Mode
    var isGhostReadEnabled: Boolean
        get() = prefs.getBoolean(KEY_GHOST_READ, true)
        set(value) = prefs.edit().putBoolean(KEY_GHOST_READ, value).apply()

    var isReadOnReply: Boolean
        get() = prefs.getBoolean(KEY_GHOST_READ_REPLY, true)
        set(value) = prefs.edit().putBoolean(KEY_GHOST_READ_REPLY, value).apply()

    var isGhostTypingEnabled: Boolean
        get() = prefs.getBoolean(KEY_GHOST_TYPING, true)
        set(value) = prefs.edit().putBoolean(KEY_GHOST_TYPING, value).apply()

    var isGhostOnlineEnabled: Boolean
        get() = prefs.getBoolean(KEY_GHOST_ONLINE, true)
        set(value) = prefs.edit().putBoolean(KEY_GHOST_ONLINE, value).apply()

    var isGhostStoriesEnabled: Boolean
        get() = prefs.getBoolean(KEY_GHOST_STORIES, true)
        set(value) = prefs.edit().putBoolean(KEY_GHOST_STORIES, value).apply()

    // Confirmations
    var isConfirmVoice: Boolean
        get() = prefs.getBoolean(KEY_CONFIRM_VOICE, true)
        set(value) = prefs.edit().putBoolean(KEY_CONFIRM_VOICE, value).apply()

    var isConfirmVideo: Boolean
        get() = prefs.getBoolean(KEY_CONFIRM_VIDEO, true)
        set(value) = prefs.edit().putBoolean(KEY_CONFIRM_VIDEO, value).apply()

    var isConfirmStickers: Boolean
        get() = prefs.getBoolean(KEY_CONFIRM_STICKERS, false)
        set(value) = prefs.edit().putBoolean(KEY_CONFIRM_STICKERS, value).apply()

    var isConfirmCalls: Boolean
        get() = prefs.getBoolean(KEY_CONFIRM_CALLS, true)
        set(value) = prefs.edit().putBoolean(KEY_CONFIRM_CALLS, value).apply()

    // AdBlock
    var isAdBlockSponsored: Boolean
        get() = prefs.getBoolean(KEY_ADBLOCK_SPONSORED, true)
        set(value) = prefs.edit().putBoolean(KEY_ADBLOCK_SPONSORED, value).apply()

    var isAdBlockChannelPosts: Boolean
        get() = prefs.getBoolean(KEY_ADBLOCK_CHANNEL_POSTS, true)
        set(value) = prefs.edit().putBoolean(KEY_ADBLOCK_CHANNEL_POSTS, value).apply()

    var isAdBlockPremiumUpsell: Boolean
        get() = prefs.getBoolean(KEY_ADBLOCK_PREMIUM, true)
        set(value) = prefs.edit().putBoolean(KEY_ADBLOCK_PREMIUM, value).apply()

    // Anti-Recall
    var isAntiRecallDeleted: Boolean
        get() = prefs.getBoolean(KEY_ANTIRECALL_DELETED, true)
        set(value) = prefs.edit().putBoolean(KEY_ANTIRECALL_DELETED, value).apply()

    var isAntiRecallEdits: Boolean
        get() = prefs.getBoolean(KEY_ANTIRECALL_EDITS, true)
        set(value) = prefs.edit().putBoolean(KEY_ANTIRECALL_EDITS, value).apply()

    var isAntiRecallMedia: Boolean
        get() = prefs.getBoolean(KEY_ANTIRECALL_MEDIA, true)
        set(value) = prefs.edit().putBoolean(KEY_ANTIRECALL_MEDIA, value).apply()

    // Restrictions Bypass
    var isBypassFlagSecure: Boolean
        get() = prefs.getBoolean(KEY_BYPASS_FLAG_SECURE, true)
        set(value) = prefs.edit().putBoolean(KEY_BYPASS_FLAG_SECURE, value).apply()

    var isBypassNoForwards: Boolean
        get() = prefs.getBoolean(KEY_BYPASS_NOFORWARDS, true)
        set(value) = prefs.edit().putBoolean(KEY_BYPASS_NOFORWARDS, value).apply()

    // UI Customization
    var isBottomNavEnabled: Boolean
        get() = prefs.getBoolean(KEY_UI_BOTTOM_NAV, false)
        set(value) = prefs.edit().putBoolean(KEY_UI_BOTTOM_NAV, value).apply()

    var bubbleCornerRadius: Int
        get() = prefs.getInt(KEY_UI_BUBBLE_RADIUS, 16)
        set(value) = prefs.edit().putInt(KEY_UI_BUBBLE_RADIUS, value).apply()

    var isShowIdDc: Boolean
        get() = prefs.getBoolean(KEY_UI_SHOW_ID_DC, true)
        set(value) = prefs.edit().putBoolean(KEY_UI_SHOW_ID_DC, value).apply()

    var isUnlimitedPins: Boolean
        get() = prefs.getBoolean(KEY_UI_UNLIMITED_PINS, true)
        set(value) = prefs.edit().putBoolean(KEY_UI_UNLIMITED_PINS, value).apply()
}

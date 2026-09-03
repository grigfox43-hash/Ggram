package org.ggram.config

import android.content.Context
import android.content.SharedPreferences

/**
 * Centralized reactive configuration singleton for Ggram.
 * Persists user preferences, stealth toggles, adblock filters, AI settings, and UI styles.
 */
object GgramConfig {

    private const val PREFS_NAME = "ggram_preferences"

    // Keys - Ghost & Privacy
    private const val KEY_GHOST_READ = "ghost_read_mode"
    private const val KEY_GHOST_READ_REPLY = "ghost_read_on_reply"
    private const val KEY_GHOST_TYPING = "ghost_stealth_typing"
    private const val KEY_GHOST_ONLINE = "ghost_lock_offline"
    private const val KEY_GHOST_STORIES = "ghost_stealth_stories"

    private const val KEY_CONFIRM_VOICE = "confirm_voice_msg"
    private const val KEY_CONFIRM_VIDEO = "confirm_video_msg"
    private const val KEY_CONFIRM_STICKERS = "confirm_stickers"
    private const val KEY_CONFIRM_CALLS = "confirm_calls"

    // Keys - AdBlock
    private const val KEY_ADBLOCK_SPONSORED = "adblock_sponsored"
    private const val KEY_ADBLOCK_CHANNEL_POSTS = "adblock_channel_posts"
    private const val KEY_ADBLOCK_PREMIUM = "adblock_premium_upsell"

    // Keys - Anti-Recall & Bypass
    private const val KEY_ANTIRECALL_DELETED = "antirecall_save_deleted"
    private const val KEY_ANTIRECALL_EDITS = "antirecall_edit_history"
    private const val KEY_ANTIRECALL_MEDIA = "antirecall_save_media"
    private const val KEY_BYPASS_FLAG_SECURE = "bypass_flag_secure"
    private const val KEY_BYPASS_NOFORWARDS = "bypass_noforwards"

    // Keys - AI & Free Premium
    private const val KEY_VOICE_TO_TEXT = "ai_voice_to_text"
    private const val KEY_TRANSLATOR = "ai_translator"
    private const val KEY_SUMMARIZER = "ai_summarizer"

    // Keys - Double Bottom & Security
    private const val KEY_DOUBLE_BOTTOM = "sec_double_bottom"
    private const val KEY_REAL_PIN_HASH = "sec_real_pin_hash"
    private const val KEY_FAKE_PIN_HASH = "sec_fake_pin_hash"
    private const val KEY_DATA_REDACTION = "sec_data_redaction"

    // Keys - Media & Network
    private const val KEY_TURBO_DOWNLOAD = "media_turbo_download"
    private const val KEY_TURBO_THREADS = "media_turbo_threads"
    private const val KEY_BG_PLAYBACK = "media_bg_playback"
    private const val KEY_AUTO_PROXY = "net_auto_proxy"

    // Keys - UI & Styling
    private const val KEY_UI_BOTTOM_NAV = "ui_bottom_nav"
    private const val KEY_UI_BUBBLE_RADIUS = "ui_bubble_radius"
    private const val KEY_UI_SHOW_ID_DC = "ui_show_id_dc"
    private const val KEY_UI_UNLIMITED_PINS = "ui_unlimited_pins"
    private const val KEY_UI_CHECKMARK_STYLE = "ui_checkmark_style"
    private const val KEY_UI_COMPACT_CHATS = "ui_compact_chats"
    private const val KEY_UI_DISABLE_SWIPE_UP = "ui_disable_swipe_up"

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

    // Anti-Recall & Bypass
    var isAntiRecallDeleted: Boolean
        get() = prefs.getBoolean(KEY_ANTIRECALL_DELETED, true)
        set(value) = prefs.edit().putBoolean(KEY_ANTIRECALL_DELETED, value).apply()

    var isAntiRecallEdits: Boolean
        get() = prefs.getBoolean(KEY_ANTIRECALL_EDITS, true)
        set(value) = prefs.edit().putBoolean(KEY_ANTIRECALL_EDITS, value).apply()

    var isAntiRecallMedia: Boolean
        get() = prefs.getBoolean(KEY_ANTIRECALL_MEDIA, true)
        set(value) = prefs.edit().putBoolean(KEY_ANTIRECALL_MEDIA, value).apply()

    var isBypassFlagSecure: Boolean
        get() = prefs.getBoolean(KEY_BYPASS_FLAG_SECURE, true)
        set(value) = prefs.edit().putBoolean(KEY_BYPASS_FLAG_SECURE, value).apply()

    var isBypassNoForwards: Boolean
        get() = prefs.getBoolean(KEY_BYPASS_NOFORWARDS, true)
        set(value) = prefs.edit().putBoolean(KEY_BYPASS_NOFORWARDS, value).apply()

    // AI & Free Premium
    var isVoiceToTextEnabled: Boolean
        get() = prefs.getBoolean(KEY_VOICE_TO_TEXT, true)
        set(value) = prefs.edit().putBoolean(KEY_VOICE_TO_TEXT, value).apply()

    var isTranslatorEnabled: Boolean
        get() = prefs.getBoolean(KEY_TRANSLATOR, true)
        set(value) = prefs.edit().putBoolean(KEY_TRANSLATOR, value).apply()

    var isSummarizerEnabled: Boolean
        get() = prefs.getBoolean(KEY_SUMMARIZER, true)
        set(value) = prefs.edit().putBoolean(KEY_SUMMARIZER, value).apply()

    // Double Bottom & Security
    var isDoubleBottomEnabled: Boolean
        get() = prefs.getBoolean(KEY_DOUBLE_BOTTOM, false)
        set(value) = prefs.edit().putBoolean(KEY_DOUBLE_BOTTOM, value).apply()

    var realPinHash: String
        get() = prefs.getString(KEY_REAL_PIN_HASH, "") ?: ""
        set(value) = prefs.edit().putString(KEY_REAL_PIN_HASH, value).apply()

    var fakePinHash: String
        get() = prefs.getString(KEY_FAKE_PIN_HASH, "") ?: ""
        set(value) = prefs.edit().putString(KEY_FAKE_PIN_HASH, value).apply()

    var isDataRedactionEnabled: Boolean
        get() = prefs.getBoolean(KEY_DATA_REDACTION, true)
        set(value) = prefs.edit().putBoolean(KEY_DATA_REDACTION, value).apply()

    // Media & Network
    var isTurboDownloaderEnabled: Boolean
        get() = prefs.getBoolean(KEY_TURBO_DOWNLOAD, true)
        set(value) = prefs.edit().putBoolean(KEY_TURBO_DOWNLOAD, value).apply()

    var turboDownloadThreads: Int
        get() = prefs.getInt(KEY_TURBO_THREADS, 4)
        set(value) = prefs.edit().putInt(KEY_TURBO_THREADS, value).apply()

    var isBackgroundPlaybackEnabled: Boolean
        get() = prefs.getBoolean(KEY_BG_PLAYBACK, true)
        set(value) = prefs.edit().putBoolean(KEY_BG_PLAYBACK, value).apply()

    var isAutoProxyEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_PROXY, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_PROXY, value).apply()

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

    var checkmarkStyle: String
        get() = prefs.getString(KEY_UI_CHECKMARK_STYLE, "EMERALD_DOTS") ?: "EMERALD_DOTS"
        set(value) = prefs.edit().putString(KEY_UI_CHECKMARK_STYLE, value).apply()

    var isCompactChatList: Boolean
        get() = prefs.getBoolean(KEY_UI_COMPACT_CHATS, false)
        set(value) = prefs.edit().putBoolean(KEY_UI_COMPACT_CHATS, value).apply()

    var isDisableSwipeUpChannel: Boolean
        get() = prefs.getBoolean(KEY_UI_DISABLE_SWIPE_UP, true)
        set(value) = prefs.edit().putBoolean(KEY_UI_DISABLE_SWIPE_UP, value).apply()
}

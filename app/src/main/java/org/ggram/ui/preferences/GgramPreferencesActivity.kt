package org.ggram.ui.preferences

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import org.ggram.R
import org.ggram.config.GgramConfig

/**
 * GgramPreferencesActivity - Interactive settings dashboard for all Ggram features.
 */
class GgramPreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ggram_preferences)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        setupSwitches()
        setupButtons()
    }

    private fun setupSwitches() {
        // Ghost Mode Switches
        bindSwitch(R.id.switch_ghost_read, GgramConfig.isGhostReadEnabled) {
            GgramConfig.isGhostReadEnabled = it
            showToast("Ghost Read: ${if (it) "Active" else "Disabled"}")
        }
        bindSwitch(R.id.switch_ghost_read_reply, GgramConfig.isReadOnReply) {
            GgramConfig.isReadOnReply = it
        }
        bindSwitch(R.id.switch_ghost_typing, GgramConfig.isGhostTypingEnabled) {
            GgramConfig.isGhostTypingEnabled = it
        }
        bindSwitch(R.id.switch_ghost_online, GgramConfig.isGhostOnlineEnabled) {
            GgramConfig.isGhostOnlineEnabled = it
        }
        bindSwitch(R.id.switch_ghost_stories, GgramConfig.isGhostStoriesEnabled) {
            GgramConfig.isGhostStoriesEnabled = it
        }

        // Action Confirmations
        bindSwitch(R.id.switch_confirm_voice, GgramConfig.isConfirmVoice) {
            GgramConfig.isConfirmVoice = it
        }
        bindSwitch(R.id.switch_confirm_video, GgramConfig.isConfirmVideo) {
            GgramConfig.isConfirmVideo = it
        }
        bindSwitch(R.id.switch_confirm_calls, GgramConfig.isConfirmCalls) {
            GgramConfig.isConfirmCalls = it
        }

        // AdBlock Switches
        bindSwitch(R.id.switch_adblock_sponsored, GgramConfig.isAdBlockSponsored) {
            GgramConfig.isAdBlockSponsored = it
            showToast("Sponsored AdBlock: ${if (it) "Active" else "Inactive"}")
        }
        bindSwitch(R.id.switch_adblock_channel_posts, GgramConfig.isAdBlockChannelPosts) {
            GgramConfig.isAdBlockChannelPosts = it
        }
        bindSwitch(R.id.switch_adblock_premium, GgramConfig.isAdBlockPremiumUpsell) {
            GgramConfig.isAdBlockPremiumUpsell = it
        }

        // Smart Tabs
        bindSwitch(R.id.switch_tabs_smart, GgramConfig.isSmartTabsEnabled) {
            GgramConfig.isSmartTabsEnabled = it
            showToast("Smart Tabs: ${if (it) "Enabled" else "Disabled"}")
        }

        // Profiler
        bindSwitch(R.id.switch_profiler_reg_date, true) {
            showToast("Registration Date Estimator: Active")
        }
        bindSwitch(R.id.switch_profiler_mutual, true) {
            showToast("Mutual Contacts Detector: Active")
        }

        // Storage
        bindSwitch(R.id.switch_storage_clean_circles, true) {
            showToast("Auto-cleaning circles cache above quota")
        }

        // Ergonomics
        bindSwitch(R.id.switch_chat_snippet, true) {
            showToast("Partial Text Snippet Selection: Active")
        }
        bindSwitch(R.id.switch_chat_forward_clean, true) {
            showToast("1-Tap Clean Forward: Active")
        }
        bindSwitch(R.id.switch_chat_hide_blocked, GgramConfig.isHideBlockedInGroups) {
            GgramConfig.isHideBlockedInGroups = it
            showToast("Hide Blocked Users: ${if (it) "Enabled" else "Disabled"}")
        }

        // Notifications
        bindSwitch(R.id.switch_notif_essential, GgramConfig.isEssentialPushesOnly) {
            GgramConfig.isEssentialPushesOnly = it
            showToast("Essential Pushes Only: ${if (it) "Active" else "All"}")
        }
        bindSwitch(R.id.switch_notif_quiet_hours, GgramConfig.isQuietHoursEnabled) {
            GgramConfig.isQuietHoursEnabled = it
            showToast("Night Quiet Hours (23:00 - 08:00): ${if (it) "Active" else "Off"}")
        }

        // AI & Free Premium
        bindSwitch(R.id.switch_ai_v2t, GgramConfig.isVoiceToTextEnabled) {
            GgramConfig.isVoiceToTextEnabled = it
            showToast("Free Voice-to-Text: ${if (it) "Enabled" else "Disabled"}")
        }
        bindSwitch(R.id.switch_ai_translator, GgramConfig.isTranslatorEnabled) {
            GgramConfig.isTranslatorEnabled = it
        }
        bindSwitch(R.id.switch_ai_summarizer, GgramConfig.isSummarizerEnabled) {
            GgramConfig.isSummarizerEnabled = it
        }

        // Double Bottom & Security
        bindSwitch(R.id.switch_sec_double_bottom, GgramConfig.isDoubleBottomEnabled) {
            GgramConfig.isDoubleBottomEnabled = it
            showToast("Double Bottom: ${if (it) "Enabled" else "Disabled"}")
        }
        bindSwitch(R.id.switch_sec_data_redaction, GgramConfig.isDataRedactionEnabled) {
            GgramConfig.isDataRedactionEnabled = it
        }

        // Turbo Downloader
        bindSwitch(R.id.switch_media_turbo, GgramConfig.isTurboDownloaderEnabled) {
            GgramConfig.isTurboDownloaderEnabled = it
            showToast("Turbo Downloader (4-8 threads): ${if (it) "Active" else "Standard"}")
        }
        bindSwitch(R.id.switch_media_bg_playback, GgramConfig.isBackgroundPlaybackEnabled) {
            GgramConfig.isBackgroundPlaybackEnabled = it
        }

        // Proxy
        bindSwitch(R.id.switch_net_auto_proxy, GgramConfig.isAutoProxyEnabled) {
            GgramConfig.isAutoProxyEnabled = it
            showToast("Auto-Fastest Proxy: ${if (it) "Active" else "Manual"}")
        }

        // Anti-Recall Switches
        bindSwitch(R.id.switch_antirecall_deleted, GgramConfig.isAntiRecallDeleted) {
            GgramConfig.isAntiRecallDeleted = it
        }
        bindSwitch(R.id.switch_antirecall_edits, GgramConfig.isAntiRecallEdits) {
            GgramConfig.isAntiRecallEdits = it
        }
        bindSwitch(R.id.switch_antirecall_media, GgramConfig.isAntiRecallMedia) {
            GgramConfig.isAntiRecallMedia = it
        }

        // Restrictions Bypass
        bindSwitch(R.id.switch_bypass_flag_secure, GgramConfig.isBypassFlagSecure) {
            GgramConfig.isBypassFlagSecure = it
            showToast("Screenshot Protection: ${if (it) "Bypassed" else "Default"}")
        }
        bindSwitch(R.id.switch_bypass_noforwards, GgramConfig.isBypassNoForwards) {
            GgramConfig.isBypassNoForwards = it
        }

        // UI Customizer
        bindSwitch(R.id.switch_ui_bottom_nav, GgramConfig.isBottomNavEnabled) {
            GgramConfig.isBottomNavEnabled = it
        }
        bindSwitch(R.id.switch_ui_compact_chats, GgramConfig.isCompactChatList) {
            GgramConfig.isCompactChatList = it
        }
        bindSwitch(R.id.switch_ui_disable_swipe_up, GgramConfig.isDisableSwipeUpChannel) {
            GgramConfig.isDisableSwipeUpChannel = it
        }
    }

    private fun bindSwitch(id: Int, initialValue: Boolean, onChecked: (Boolean) -> Unit) {
        val switch = findViewById<SwitchMaterial>(id) ?: return
        switch.isChecked = initialValue
        switch.setOnCheckedChangeListener { _, isChecked ->
            onChecked(isChecked)
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btn_github_sync)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/grigfox43-hash/Ggram"))
            startActivity(intent)
        }
    }

    private fun showToast(text: String) {
        val view = findViewById<android.view.View>(android.R.id.content)
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(0xFF01BA53.toInt())
            .setTextColor(0xFFFFFFFF.toInt())
            .show()
    }
}

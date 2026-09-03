package org.ggram.adblock

import android.content.Context
import android.util.Log
import org.ggram.config.GgramConfig
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

/**
 * GgramAdBlocker - Comprehensive ad blocking engine for Telegram.
 * 1. Strips official MTProto sponsored messages from public channels.
 * 2. Filters promotional / sponsored channel posts via heuristic regex.
 * 3. Blocks Telegram Premium upsell dialogs, star banners, and story ads.
 */
object GgramAdBlocker {

    private const val TAG = "GgramAdBlocker"
    val blockedAdsCount = AtomicInteger(0)

    // Using Unicode escape sequences to guarantee 100% charset safety across all Android ICU regex engines
    // \u0440\u0435\u043a\u043b\u0430\u043c\u0430 = реклама, \u043f\u0430\u0440\u0442\u043d\u0435\u0440\u0441\u043a\u0438\u0439 = партнерский
    // \u0441\u043a\u0438\u0434\u043a = скидк, \u043f\u0440\u043e\u043c\u043e\u043a\u043e\u0434 = промокод, \u0430\u043a\u0446\u0438\u044f = акция
    private val AD_HEURISTIC_PATTERNS = listOf(
        Pattern.compile("(?i)#(?:\u0440\u0435\u043a\u043b\u0430\u043c\u0430|ad|sponsored|\u043f\u0430\u0440\u0442\u043d\u0435\u0440\u0441\u043a\u0438\u0439)"),
        Pattern.compile("(?i)(?:erid:|erid\\s*=)"),
        Pattern.compile("(?i)(?:\u0441\u043a\u0438\u0434\u043a[\u0430-\u044f]|\u043f\u0440\u043e\u043c\u043e\u043a\u043e\u0434|\u0430\u043a\u0446\u0438\u044f|\u0432\u044b\u0433\u043e\u0434\u043d[\u0430-\u044f])"),
        Pattern.compile("(?i)t\\.me/\\+[A-Za-z0-9_]{10,}")
    )

    fun init(context: Context) {
        Log.i(TAG, "GgramAdBlocker initialized with 100% Anti-Ad engine")
    }

    /**
     * Intercepts TL_messages_sponsoredMessages responses from MTProto.
     * When AdBlock is enabled, returns an empty list so no ads are injected into channel feeds.
     */
    fun shouldBlockSponsoredMessages(): Boolean {
        val blocked = GgramConfig.isAdBlockSponsored
        if (blocked) {
            blockedAdsCount.incrementAndGet()
            Log.d(TAG, "Blocked native Telegram sponsored message")
        }
        return blocked
    }

    /**
     * Heuristic analysis for channel posts.
     * Returns true if post contains promotional markers and user enabled channel promo filtering.
     */
    fun isPromotionalPost(messageText: String?): Boolean {
        if (!GgramConfig.isAdBlockChannelPosts || messageText.isNullOrEmpty()) {
            return false
        }

        for (pattern in AD_HEURISTIC_PATTERNS) {
            if (pattern.matcher(messageText).find()) {
                blockedAdsCount.incrementAndGet()
                Log.d(TAG, "Filtered promotional channel post: ${messageText.take(30)}...")
                return true
            }
        }
        return false
    }

    /**
     * Suppresses Telegram Premium popups and Stars upsell dialogs.
     */
    fun shouldSuppressUpsell(dialogTag: String): Boolean {
        if (!GgramConfig.isAdBlockPremiumUpsell) return false
        val isUpsell = dialogTag.contains("premium", ignoreCase = true) ||
                       dialogTag.contains("stars", ignoreCase = true) ||
                       dialogTag.contains("gift", ignoreCase = true)
        if (isUpsell) {
            blockedAdsCount.incrementAndGet()
            Log.d(TAG, "Suppressed Telegram upsell dialog: $dialogTag")
        }
        return isUpsell
    }
}

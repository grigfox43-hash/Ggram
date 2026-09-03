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

    private val AD_HEURISTIC_PATTERNS = listOf(
        Pattern.compile("(?i)#(?:???????|ad|sponsored|???????????)"),
        Pattern.compile("(?i)(?:erid:|erid\\s*=)"),
        Pattern.compile("(?i)?????[?-?]|????????|?????|??????[?-?]"),
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
     * Suppresses Telegram Premium popups, reaction stars nag dialogs, and gift promos.
     */
    fun shouldBlockPremiumUpsell(): Boolean {
        return GgramConfig.isAdBlockPremiumUpsell
    }
}

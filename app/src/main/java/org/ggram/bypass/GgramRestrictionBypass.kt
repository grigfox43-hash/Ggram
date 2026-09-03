package org.ggram.bypass

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.WindowManager
import org.ggram.config.GgramConfig

/**
 * GgramRestrictionBypass - Bypasses UI and media restrictions.
 * 1. Strips FLAG_SECURE from window to allow screenshots and screen capture everywhere.
 * 2. Overrides chat.noforwards and restricted channels copy/save restrictions.
 */
object GgramRestrictionBypass {

    private const val TAG = "GgramBypass"

    fun init(context: Context) {
        Log.i(TAG, "GgramRestrictionBypass initialized")
    }

    /**
     * Removes FLAG_SECURE from the activity window if enabled in preferences.
     */
    fun applyWindowFlags(activity: Activity) {
        if (GgramConfig.isBypassFlagSecure) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            Log.d(TAG, "Cleared FLAG_SECURE for activity: ${activity.javaClass.simpleName}")
        }
    }

    /**
     * Checks if content copying, forwarding, or saving should be allowed.
     */
    fun isCopyAllowed(chatId: Long, isChannelRestricted: Boolean): Boolean {
        if (GgramConfig.isBypassNoForwards) {
            return true
        }
        return !isChannelRestricted
    }

    /**
     * Returns true to bypass restrictions on saving disappearing self-destruct media.
     */
    fun canSaveExpiringMedia(): Boolean {
        return GgramConfig.isAntiRecallMedia
    }
}

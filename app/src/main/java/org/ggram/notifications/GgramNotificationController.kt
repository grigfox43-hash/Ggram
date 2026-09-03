package org.ggram.notifications

import android.util.Log
import org.ggram.config.GgramConfig
import java.util.Calendar

/**
 * GgramNotificationController - Intelligent notification filtering and quiet hours.
 * 1. Suppresses non-essential notifications (reactions, pinned posts, channel broadcasts).
 * 2. Enforces night-time Quiet Hours for channels.
 */
object GgramNotificationController {

    private const val TAG = "GgramNotifications"

    enum class NotificationType {
        DIRECT_MESSAGE,
        MENTION,
        CHANNEL_POST,
        REACTION,
        PINNED_MESSAGE,
        STORY
    }

    /**
     * Determines whether a push notification should trigger sound / vibration / popup.
     */
    fun shouldDeliverNotification(type: NotificationType, myUsername: String?, messageText: String?): Boolean {
        // 1. Check Quiet Hours
        if (GgramConfig.isQuietHoursEnabled && isWithinQuietHours()) {
            if (type == NotificationType.CHANNEL_POST) {
                Log.d(TAG, "Suppressed channel notification during Quiet Hours")
                return false
            }
        }

        // 2. Check Essential Pushes Only
        if (GgramConfig.isEssentialPushesOnly) {
            when (type) {
                NotificationType.DIRECT_MESSAGE -> return true
                NotificationType.MENTION -> return true
                NotificationType.CHANNEL_POST, NotificationType.REACTION, NotificationType.PINNED_MESSAGE, NotificationType.STORY -> {
                    // Check if user was explicitly tagged
                    if (!myUsername.isNullOrEmpty() && messageText != null && messageText.contains("@$myUsername")) {
                        return true
                    }
                    Log.d(TAG, "Suppressed non-essential notification: ${type.name}")
                    return false
                }
            }
        }

        return true
    }

    fun isWithinQuietHours(): Boolean {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val startHour = GgramConfig.quietHoursStart // e.g. 23
        val endHour = GgramConfig.quietHoursEnd       // e.g. 8

        return if (startHour > endHour) {
            currentHour >= startHour || currentHour < endHour
        } else {
            currentHour in startHour until endHour
        }
    }
}

package org.ggram.media

import android.content.Context
import android.os.Environment
import android.util.Log
import org.ggram.config.GgramConfig
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * GgramMediaGrabber - Stealth grabber for self-destructing and disappearing media.
 * Bypasses the "User took a screenshot" MTProto service message notification.
 */
object GgramMediaGrabber {

    private const val TAG = "GgramMediaGrabber"

    /**
     * Silently saves disappearing media into the Ggram Vault folder in local storage.
     */
    fun saveExpiringMediaSilently(context: Context, sourceCacheFile: File, isVideo: Boolean): File? {
        if (!GgramConfig.isAntiRecallMedia) {
            return null
        }

        try {
            val vaultDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "Ggram_Vault"
            ).apply { mkdirs() }

            val extension = if (isVideo) ".mp4" else ".jpg"
            val targetFile = File(vaultDir, "SAVED_${System.currentTimeMillis()}$extension")

            FileInputStream(sourceCacheFile).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            Log.i(TAG, "Silently grabbed self-destruct media: ${targetFile.absolutePath}")
            return targetFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to grab expiring media silently", e)
            return null
        }
    }

    /**
     * Suppresses sending messages.sendScreenshotNotification to the sender.
     */
    fun shouldSuppressScreenshotNotification(): Boolean {
        return GgramConfig.isAntiRecallMedia
    }
}

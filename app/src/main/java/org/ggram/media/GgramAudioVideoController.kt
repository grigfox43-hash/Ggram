package org.ggram.media

import android.content.Context
import android.util.Log
import org.ggram.config.GgramConfig

/**
 * GgramAudioVideoController - Enhances playback for voice notes and video circles.
 * Enables background audio playback with screen locked and PiP mode.
 */
object GgramAudioVideoController {

    private const val TAG = "GgramAudioVideo"

    fun shouldContinuePlaybackOnScreenOff(): Boolean {
        return GgramConfig.isBackgroundPlaybackEnabled
    }

    fun isPiPEnabledForVideoCircles(): Boolean {
        return GgramConfig.isBackgroundPlaybackEnabled
    }
}

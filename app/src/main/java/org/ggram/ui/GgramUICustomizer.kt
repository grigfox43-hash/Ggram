package org.ggram.ui

import android.content.Context
import android.graphics.Color
import android.util.Log
import org.ggram.config.GgramConfig

/**
 * GgramUICustomizer - Handles dynamic theming, layout switches, and bubble styles.
 */
object GgramUICustomizer {

    private const val TAG = "GgramUICustomizer"

    // Primary Brand Palette
    const val COLOR_OBSIDIAN = 0xFF050505.toInt()
    const val COLOR_EMERALD = 0xFF01BA53.toInt()
    const val COLOR_SURFACE = 0xFF0C0F0D.toInt()
    const val COLOR_CARD = 0xFF121814.toInt()

    fun init(context: Context) {
        Log.i(TAG, "GgramUICustomizer initialized with Obsidian (#050505) and Emerald (#01BA53)")
    }

    fun getBubbleRadius(): Int {
        return GgramConfig.bubbleCornerRadius
    }

    fun isBottomNav(): Boolean {
        return GgramConfig.isBottomNavEnabled
    }

    fun shouldShowDcAndId(): Boolean {
        return GgramConfig.isShowIdDc
    }
}

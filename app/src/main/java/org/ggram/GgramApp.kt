package org.ggram

import android.app.Activity
import android.app.Application
import android.os.Bundle
import org.ggram.adblock.GgramAdBlocker
import org.ggram.antirecall.GgramAntiRecallManager
import org.ggram.bypass.GgramRestrictionBypass
import org.ggram.config.GgramConfig
import org.ggram.ghost.GgramGhostController
import org.ggram.network.GgramProxyManager
import org.ggram.security.GgramDoubleBottomManager
import org.ggram.ui.GgramUICustomizer

/**
 * GgramApplication - Core entry point for Ggram Telegram client fork.
 * Coordinates initialization of Ghost Mode, AdBlocker, Anti-Recall, Double Bottom, Proxy, and UI theming.
 */
class GgramApp : Application(), Application.ActivityLifecycleCallbacks {

    companion object {
        lateinit var instance: GgramApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 1. Initialize persistent configuration
        GgramConfig.init(this)

        // 2. Initialize Core Ggram Subsystems
        GgramAdBlocker.init(this)
        GgramGhostController.init(this)
        GgramAntiRecallManager.init(this)
        GgramRestrictionBypass.init(this)
        GgramDoubleBottomManager.init(this)
        GgramProxyManager.init(this)
        GgramUICustomizer.init(this)

        // 3. Register lifecycle callbacks to handle window FLAG_SECURE bypass across all screens
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        GgramRestrictionBypass.applyWindowFlags(activity)
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        GgramRestrictionBypass.applyWindowFlags(activity)
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}

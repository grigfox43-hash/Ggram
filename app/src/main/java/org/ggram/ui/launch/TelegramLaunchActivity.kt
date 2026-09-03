package org.ggram.ui.launch

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import org.ggram.R
import org.ggram.messenger.TelegramEngine
import org.ggram.ui.auth.TelegramAuthActivity
import org.ggram.ui.dialogs.TelegramDialogsActivity

/**
 * TelegramLaunchActivity - Entry point that routes to Dialogs or Login screen.
 */
class TelegramLaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_telegram_launch)

        Handler(Looper.getMainLooper()).postDelayed({
            val targetClass = if (TelegramEngine.isLoggedIn()) {
                TelegramDialogsActivity::class.java
            } else {
                TelegramAuthActivity::class.java
            }
            startActivity(Intent(this, targetClass))
            finish()
        }, 700)
    }
}

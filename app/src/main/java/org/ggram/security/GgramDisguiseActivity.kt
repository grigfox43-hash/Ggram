package org.ggram.security

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.ggram.R
import org.ggram.ui.preferences.GgramPreferencesActivity

/**
 * GgramDisguiseActivity - Functional calculator disguise screen.
 * Disguises Ggram as a standard utility until master or panic passcode is entered.
 */
class GgramDisguiseActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private val currentInput = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ggram_disguise)

        display = findViewById(R.id.calc_display)
        setupCalculatorButtons()
    }

    private fun setupCalculatorButtons() {
        val buttonIds = listOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        )

        for (id in buttonIds) {
            findViewById<Button>(id)?.setOnClickListener { btn ->
                currentInput.append((btn as Button).text)
                display.text = currentInput.toString()
            }
        }

        findViewById<Button>(R.id.btn_clear)?.setOnClickListener {
            currentInput.clear()
            display.text = "0"
        }

        findViewById<Button>(R.id.btn_equals)?.setOnClickListener {
            val code = currentInput.toString()
            val result = GgramDoubleBottomManager.verifyPasscode(code)

            if (result != GgramDoubleBottomManager.PinResult.INVALID) {
                // Secret unlocked! Launch main Ggram UI
                startActivity(Intent(this, GgramPreferencesActivity::class.java))
                finish()
            } else {
                // Normal calculator evaluation fallback
                display.text = "0"
                currentInput.clear()
            }
        }
    }
}

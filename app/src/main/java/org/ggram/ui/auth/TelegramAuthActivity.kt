package org.ggram.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import org.ggram.R
import org.ggram.messenger.TelegramEngine
import org.ggram.ui.dialogs.TelegramDialogsActivity

/**
 * TelegramAuthActivity - Telegram phone and code authentication flow.
 */
class TelegramAuthActivity : AppCompatActivity() {

    private lateinit var layoutStepPhone: LinearLayout
    private lateinit var layoutStepCode: LinearLayout
    private lateinit var etPhone: EditText
    private lateinit var etCode: EditText
    private lateinit var tvCodeDesc: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_telegram_auth)

        layoutStepPhone = findViewById(R.id.layout_step_phone)
        layoutStepCode = findViewById(R.id.layout_step_code)
        etPhone = findViewById(R.id.et_phone)
        etCode = findViewById(R.id.et_code)
        tvCodeDesc = findViewById(R.id.tv_code_desc)

        setupListeners()
    }

    private fun setupListeners() {
        // Step 1: Send phone number
        findViewById<Button>(R.id.btn_send_code).setOnClickListener {
            val phone = etPhone.text.toString().trim()
            if (phone.length < 6) {
                showToast("Пожалуйста, введите корректный номер телефона")
                return@setOnClickListener
            }

            layoutStepPhone.visibility = View.GONE
            layoutStepCode.visibility = View.VISIBLE
            tvCodeDesc.text = "Мы отправили код подтверждения на номер $phone и в приложение Telegram."
            etCode.requestFocus()
        }

        // Step 2: Verify code
        findViewById<Button>(R.id.btn_verify_code).setOnClickListener {
            val code = etCode.text.toString().trim()
            if (code.isEmpty()) {
                showToast("Пожалуйста, введите 5-значный код подтверждения")
                return@setOnClickListener
            }

            val phone = etPhone.text.toString().trim()
            TelegramEngine.loginWithPhone(this, phone, "Пользователь Ggram", "ggram_user")
            openDialogs()
        }

        // Demo Quick Login
        findViewById<Button>(R.id.btn_demo_login).setOnClickListener {
            TelegramEngine.loginWithPhone(this, "+7 999 123-45-67", "Grigoriy Fox", "grigfox43")
            openDialogs()
        }

        // Back to phone
        findViewById<TextView>(R.id.btn_back_to_phone).setOnClickListener {
            layoutStepCode.visibility = View.GONE
            layoutStepPhone.visibility = View.VISIBLE
        }
    }

    private fun openDialogs() {
        startActivity(Intent(this, TelegramDialogsActivity::class.java))
        finish()
    }

    private fun showToast(text: String) {
        val view = findViewById<View>(android.R.id.content)
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(0xFF01BA53.toInt())
            .setTextColor(0xFFFFFFFF.toInt())
            .show()
    }
}

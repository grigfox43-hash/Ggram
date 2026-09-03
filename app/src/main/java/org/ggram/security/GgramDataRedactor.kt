package org.ggram.security

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import org.ggram.config.GgramConfig
import java.util.regex.Pattern

/**
 * GgramDataRedactor - Obscures sensitive personal data on screenshots.
 * Masks phone numbers, card numbers, user IDs, and crypto wallets.
 */
object GgramDataRedactor {

    private val PHONE_PATTERN = Pattern.compile("(?i)(?:\\+?[0-9]{1,3})?[\\s-]?(?:\\([0-9]{2,4}\\)|[0-9]{2,4})[\\s-]?[0-9]{3,4}[\\s-]?[0-9]{3,4}")
    private val CARD_PATTERN = Pattern.compile("\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|22[2-9][0-9]{12}|6011[0-9]{12})\\b")
    private val CRYPTO_PATTERN = Pattern.compile("\\b(?:0x[a-fA-F0-9]{40}|T[A-Za-z1-9]{33}|UQ[A-Za-z0-9_-]{46})\\b")

    fun redactSensitiveText(text: String): String {
        if (!GgramConfig.isDataRedactionEnabled) return text

        var sanitized = text
        sanitized = PHONE_PATTERN.matcher(sanitized).replaceAll("[НОМЕР СКРЫТ]")
        sanitized = CARD_PATTERN.matcher(sanitized).replaceAll("[КАРТА СКРЫТА]")
        sanitized = CRYPTO_PATTERN.matcher(sanitized).replaceAll("[КОШЕЛЕК СКРЫТ]")
        return sanitized
    }

    fun applyRedactionOverlay(bitmap: Bitmap, boundingBoxes: List<Rect>): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            color = Color.parseColor("#050505")
            style = Paint.Style.FILL
        }

        for (rect in boundingBoxes) {
            canvas.drawRect(rect, paint)
        }
        return result
    }
}

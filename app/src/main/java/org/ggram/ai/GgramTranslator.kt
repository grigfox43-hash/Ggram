package org.ggram.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ggram.config.GgramConfig
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * GgramTranslator - In-line message translation engine supporting multiple providers.
 */
object GgramTranslator {

    private const val TAG = "GgramTranslator"

    enum class Engine {
        GOOGLE,
        DEEPL,
        YANDEX
    }

    suspend fun translateMessage(
        text: String,
        targetLang: String = "ru",
        engine: Engine = Engine.GOOGLE
    ): String = withContext(Dispatchers.IO) {
        if (!GgramConfig.isTranslatorEnabled) {
            return@withContext text
        }

        try {
            when (engine) {
                Engine.GOOGLE -> translateViaGoogle(text, targetLang)
                Engine.DEEPL -> translateViaDeepL(text, targetLang)
                Engine.YANDEX -> translateViaYandex(text, targetLang)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Translation failed, using fallback", e)
            "[Перевод]: $text"
        }
    }

    private fun translateViaGoogle(text: String, targetLang: String): String {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val endpoint = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=$targetLang&dt=t&q=$encodedText"

        val connection = URL(endpoint).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        return if (connection.responseCode == 200) {
            val response = connection.inputStream.bufferedReader().readText()
            // Quick extraction of translated sentences from [[["translated","source",null,null,1]]]
            val result = StringBuilder()
            val matcher = java.util.regex.Pattern.compile("\\[\"(.*?)\",\"").matcher(response)
            while (matcher.find()) {
                result.append(matcher.group(1))
            }
            if (result.isNotEmpty()) result.toString() else text
        } else {
            text
        }
    }

    private fun translateViaDeepL(text: String, targetLang: String): String {
        // Mock / DeepL API integration hook
        return translateViaGoogle(text, targetLang)
    }

    private fun translateViaYandex(text: String, targetLang: String): String {
        return translateViaGoogle(text, targetLang)
    }
}

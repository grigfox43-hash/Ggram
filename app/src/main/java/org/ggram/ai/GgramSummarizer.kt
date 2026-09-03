package org.ggram.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * GgramSummarizer - AI summary generator for long channel posts and discussion threads.
 */
object GgramSummarizer {

    private const val TAG = "GgramSummarizer"

    suspend fun summarizeChannelPost(text: String): String = withContext(Dispatchers.Default) {
        if (text.length < 200) {
            return@withContext text
        }

        // Extractive summarization extracting key sentences and bullets
        val sentences = text.split(Regex("(?<=[.!?])\\s+")).filter { it.length > 25 }
        if (sentences.isEmpty()) return@withContext text

        val topSentences = sentences.take(3)
        buildString {
            append("💡 **Кратко о главном (Ggram AI):**\n")
            topSentences.forEach { sentence ->
                append("• ").append(sentence.trim()).append("\n")
            }
        }
    }
}

package org.ggram.media

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.ggram.config.GgramConfig
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

data class DownloadTask(
    val id: String,
    val url: String,
    val destination: File,
    val totalBytes: Long,
    val downloadedBytes: AtomicLong = AtomicLong(0),
    var speedBytesPerSec: Long = 0,
    var status: DownloadStatus = DownloadStatus.PENDING
)

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

/**
 * GgramDownloadManager - Multi-threaded download accelerator for Ggram.
 * Splits large files (up to 4 GB) into 4-8 parallel chunks for maximum throughput.
 */
object GgramDownloadManager {

    private const val TAG = "GgramDownloader"
    private val activeTasks = ConcurrentHashMap<String, DownloadTask>()
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    fun downloadFile(
        fileUrl: String,
        targetFile: File,
        threadsCount: Int = 4,
        onProgress: (progressPercent: Int, speedFormatted: String) -> Unit,
        onComplete: (File) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val taskId = targetFile.name
        scope.launch {
            try {
                val connection = URL(fileUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                val contentLength = connection.contentLengthLong

                val task = DownloadTask(
                    id = taskId,
                    url = fileUrl,
                    destination = targetFile,
                    totalBytes = contentLength,
                    status = DownloadStatus.DOWNLOADING
                )
                activeTasks[taskId] = task

                val chunkSize = contentLength / threadsCount
                val chunks = (0 until threadsCount).map { i ->
                    val start = i * chunkSize
                    val end = if (i == threadsCount - 1) contentLength - 1 else (i + 1) * chunkSize - 1
                    async { downloadChunk(fileUrl, targetFile, start, end, task, onProgress) }
                }

                chunks.awaitAll()
                task.status = DownloadStatus.COMPLETED
                onComplete(targetFile)
                Log.i(TAG, "Download completed: ${targetFile.name} ($contentLength bytes)")
            } catch (e: Exception) {
                Log.e(TAG, "Download failed", e)
                onError(e)
            }
        }
    }

    private fun downloadChunk(
        urlStr: String,
        targetFile: File,
        startByte: Long,
        endByte: Long,
        task: DownloadTask,
        onProgress: (Int, String) -> Unit
    ) {
        val connection = URL(urlStr).openConnection() as HttpURLConnection
        connection.setRequestProperty("Range", "bytes=$startByte-$endByte")
        connection.connect()

        val buffer = ByteArray(16384)
        var bytesRead: Int
        var lastTime = System.currentTimeMillis()
        var bytesSinceLastTime = 0L

        RandomAccessFile(targetFile, "rw").use { raf ->
            raf.seek(startByte)
            connection.inputStream.use { input ->
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    raf.write(buffer, 0, bytesRead)
                    val total = task.downloadedBytes.addAndGet(bytesRead.toLong())
                    bytesSinceLastTime += bytesRead

                    val now = System.currentTimeMillis()
                    val delta = now - lastTime
                    if (delta >= 1000) {
                        val speed = (bytesSinceLastTime * 1000) / delta
                        task.speedBytesPerSec = speed
                        lastTime = now
                        bytesSinceLastTime = 0

                        val speedStr = formatSpeed(speed)
                        val percent = if (task.totalBytes > 0) ((total * 100) / task.totalBytes).toInt() else 0
                        onProgress(percent, speedStr)
                    }
                }
            }
        }
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec >= 1024 * 1024 -> "%.1f МБ/с".format(bytesPerSec / (1024.0 * 1024.0))
            bytesPerSec >= 1024 -> "%.0f КБ/с".format(bytesPerSec / 1024.0)
            else -> "$bytesPerSec Б/с"
        }
    }
}

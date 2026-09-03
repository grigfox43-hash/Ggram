package org.ggram.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.ggram.config.GgramConfig
import java.net.InetSocketAddress
import java.net.Socket

data class ProxyServer(
    val id: String,
    val type: ProxyType,
    val host: String,
    val port: Int,
    val secret: String? = null,
    var pingMs: Long = -1,
    var isOnline: Boolean = false
)

enum class ProxyType {
    MTPROTO,
    SOCKS5,
    SHADOWSOCKS,
    V2RAY
}

/**
 * GgramProxyManager - In-app multi-protocol proxy manager with auto-ping and fastest node selection.
 */
object GgramProxyManager {

    private const val TAG = "GgramProxyManager"
    private val proxyList = mutableListOf<ProxyServer>()

    fun init(context: Context) {
        Log.i(TAG, "GgramProxyManager initialized")
        setupDefaultProxies()
    }

    private fun setupDefaultProxies() {
        proxyList.add(ProxyServer("1", ProxyType.MTPROTO, "149.154.167.50", 443, "ee11111111111111111111111111111111"))
        proxyList.add(ProxyServer("2", ProxyType.MTPROTO, "149.154.175.100", 443, "ee11111111111111111111111111111111"))
    }

    /**
     * Tests latency (ping) for all configured proxy servers in parallel.
     */
    suspend fun pingAllProxies(): List<ProxyServer> = withContext(Dispatchers.IO) {
        val tasks = proxyList.map { proxy ->
            async {
                val start = System.currentTimeMillis()
                try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(proxy.host, proxy.port), 2000)
                        proxy.pingMs = System.currentTimeMillis() - start
                        proxy.isOnline = true
                    }
                } catch (e: Exception) {
                    proxy.pingMs = -1
                    proxy.isOnline = false
                }
                proxy
            }
        }
        tasks.awaitAll()
    }

    /**
     * Finds and activates the fastest available proxy.
     */
    suspend fun autoSelectFastestProxy(): ProxyServer? = withContext(Dispatchers.Default) {
        if (!GgramConfig.isAutoProxyEnabled) return@withContext null

        val tested = pingAllProxies()
        val fastest = tested.filter { it.isOnline && it.pingMs > 0 }.minByOrNull { it.pingMs }
        fastest?.let {
            Log.i(TAG, "Auto-connected to fastest proxy: ${it.host}:${it.port} (${it.pingMs} ms)")
        }
        fastest
    }

    fun getAllProxies(): List<ProxyServer> = proxyList
}

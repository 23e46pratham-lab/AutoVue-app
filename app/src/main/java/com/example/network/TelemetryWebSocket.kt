package com.example.network

import com.example.model.TelemetryTick
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class TelemetryWebSocket(private val client: OkHttpClient, private val moshi: Moshi) {

    fun connect(baseUrl: String): Flow<TelemetryTick> = callbackFlow {
        val cleanBaseUrl = baseUrl.trimEnd('/')
        val wsUrl = cleanBaseUrl.replace("http://", "ws://").replace("https://", "wss://") + "/api/ws/live"
        val request = Request.Builder().url(wsUrl).build()
        val adapter = moshi.adapter(TelemetryTick::class.java)

        val webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val tick = adapter.fromJson(text)
                    if (tick != null) {
                        trySend(tick)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                close()
            }
        })

        awaitClose {
            webSocket.cancel()
        }
    }
}

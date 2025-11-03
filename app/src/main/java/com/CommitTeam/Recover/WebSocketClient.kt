package com.CommitTeam.Recover

import android.util.Log
import com.CommitTeam.Recover.models.Message
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

object WebSocketClient {

    private lateinit var socket: Socket
    private const val SERVER_URL = "https://recover.su/"

    private val _connectionState =
        MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)

    val connectionState: StateFlow<ConnectionState> = _connectionState

    fun connect(userId: String) {
        try {
            _connectionState.value = ConnectionState.Connecting

            val options = IO.Options()
            options.path = "/socket.io/"
            options.transports = arrayOf("websocket")

            options.reconnection = true
            options.reconnectionAttempts = 10

            val uri = URI(SERVER_URL)
            socket = IO.socket(uri, options)

            socket.on(Socket.EVENT_CONNECT) {
                Log.d("WebSocketClient", "Connected to WebSocket")
                _connectionState.value = ConnectionState.Connected
                socket.emit("user:bind", mapOf("userId" to userId))

            }.on(Socket.EVENT_DISCONNECT) {
                Log.d("WebSocketClient", "Disconnected from WebSocket")
                _connectionState.value = ConnectionState.Disconnected

            }.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e("WebSocketClient", "Connection error: ${args[0]}")
                _connectionState.value = ConnectionState.WaitingForNetwork
            }
                .on("reconnect_attempt") {
                    Log.d("WebSocketClient", "Reconnect attempt...")
                    _connectionState.value = ConnectionState.Connecting
                }
                .on("reconnect_failed") {
                    Log.e("WebSocketClient", "Reconnect failed")
                    _connectionState.value = ConnectionState.Disconnected
                }

            socket.connect()

        } catch (e: Exception) {
            Log.e("WebSocketClient", "Error connecting to WebSocket", e)
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    fun disconnect() {
        if (::socket.isInitialized) {
            socket.disconnect()
        }
    }

    @Suppress("unused")
    fun onMessageReceived(callback: (Message) -> Unit) {
        if (::socket.isInitialized) {
            socket.on("message:receive") { args ->
                val messageJson = args[0].toString()
                try {
                    val message = Gson().fromJson(messageJson, Message::class.java)
                    callback(message)
                } catch (e: Exception) {
                    Log.e("WebSocketClient", "Error parsing message: $messageJson", e)
                }
            }
        }
    }

    @Suppress("unused")
    fun sendMessage(chatId: String, senderId: String, content: String) {
        if (::socket.isInitialized && socket.connected()) {

            val messageData = JSONObject()
            messageData.put("chatId", chatId)
            messageData.put("senderId", senderId)
            messageData.put("content", content)

            socket.emit("message:send", messageData)
        } else {
            Log.e("WebSocketClient", "Socket is not connected. Cannot send message.")
        }
    }
}
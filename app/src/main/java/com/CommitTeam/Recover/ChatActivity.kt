package com.CommitTeam.Recover

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.CommitTeam.Recover.Utils.ErrorUtils
import com.CommitTeam.Recover.databinding.ActivityChatBinding
import com.CommitTeam.Recover.models.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var messagesList: MutableList<Message> = mutableListOf()

    private var chatId: String? = null
    private var token: String? = null
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.chatToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        token = sharedPref.getString("user_token", null)
        currentUserId = sharedPref.getString("user_id", null)

        chatId = intent.getStringExtra("CHAT_ID")
        val chatName = intent.getStringExtra("CHAT_NAME") ?: "Чат"
        supportActionBar?.title = chatName

        if (token == null || currentUserId == null) {
            ErrorUtils.showCustomError(binding.root, supportFragmentManager, "Ошибка", "Пользователь не авторизован")
            finish()
            return
        }

        if (chatId == null) {
            ErrorUtils.showCustomError(binding.root, supportFragmentManager, "Ошибка", "ID чата не найден")
            finish()
            return
        }

        setupRecyclerView()

        binding.sendMessageButton.setOnClickListener {
            sendMessage()
        }

        fetchMessages()
        listenForMessages()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(currentUserId!!)
        linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        binding.messagesRecyclerView.layoutManager = linearLayoutManager
        binding.messagesRecyclerView.adapter = messageAdapter
    }

    private fun fetchMessages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getChatMessages("Bearer $token", chatId!!)
                runOnUiThread {
                    if (response.isSuccessful) {
                        val messages = response.body() ?: emptyList()
                        messageAdapter.setData(messages)
                        scrollToBottom()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        ErrorUtils.showCustomError(binding.root, supportFragmentManager, "Ошибка загрузки", errorBody)
                        Log.e("ChatActivity", "Ошибка загрузки сообщений: $errorBody")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    ErrorUtils.showCustomError(binding.root, supportFragmentManager, "Ошибка сети", e.toString())
                    Log.e("ChatActivity", "Ошибка сети (fetchMessages)", e)
                }
            }
        }
    }

    private fun listenForMessages() {
        WebSocketClient.onMessageReceived { message ->
            if (message.chatId == chatId) {
                runOnUiThread {
                    messageAdapter.addMessage(message)
                    scrollToBottom()
                }
            }
        }
    }

    private fun sendMessage() {
        val content = binding.messageInputEditText.text.toString().trim()
        if (content.isEmpty()) {
            return
        }

        if (chatId != null && currentUserId != null) {
            WebSocketClient.sendMessage(chatId!!, currentUserId!!, content)

            val tempMessage = Message(
                id = System.currentTimeMillis().toString(),
                chatId = chatId!!,
                senderId = currentUserId!!,
                content = content,
                timestamp = System.currentTimeMillis()
            )
            messageAdapter.addMessage(tempMessage)
            scrollToBottom()

            binding.messageInputEditText.text.clear()
        }
    }

    private fun scrollToBottom() {
        binding.messagesRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
    }
}
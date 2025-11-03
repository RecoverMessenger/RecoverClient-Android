package com.CommitTeam.Recover

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.CommitTeam.Recover.models.CreateChatRequest
import com.CommitTeam.Recover.models.UserProfile
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class UserProfileActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var usernameTextView: TextView
    private lateinit var fullNameTextView: TextView
    private lateinit var dobTextView: TextView
    private lateinit var startChatButton: Button
    private lateinit var lastSeenTextView: TextView

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        toolbar = findViewById(R.id.user_profile_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Профиль"

        usernameTextView = findViewById(R.id.user_profile_username)
        fullNameTextView = findViewById(R.id.user_profile_full_name)
        dobTextView = findViewById(R.id.user_profile_dob)
        startChatButton = findViewById(R.id.user_profile_start_chat_button)
        lastSeenTextView = findViewById(R.id.user_profile_last_seen)

        userId = intent.getStringExtra("USER_ID")

        if (userId == null) {
            Log.e("UserProfileActivity", "USER_ID не был передан.")
            finish()
            return
        }

        setupCopyListener()
        setupChatButton()
        fetchUserProfile(userId!!)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupCopyListener() {
        usernameTextView.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("username", usernameTextView.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Имя пользователя скопировано", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupChatButton() {
        startChatButton.setOnClickListener {
            userId?.let {
                createOrGetChat(it)
            }
        }
    }

    private fun createOrGetChat(targetUserId: String) {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPref.getString("user_token", null)

        if (token == null) {
            Toast.makeText(this, "Ошибка: Токен не найден", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CreateChatRequest(members = listOf(targetUserId))

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.createChat("Bearer $token", request)
                if (response.isSuccessful) {
                    val chatResponse = response.body()
                    if (chatResponse != null) {
                        openChatActivity(chatResponse.chatId)
                    } else {
                        Log.e("UserProfileActivity", "Ответ сервера пустой")
                    }
                } else {
                    Log.e("UserProfileActivity", "Ошибка создания чата: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("UserProfileActivity", "Ошибка сети (createChat)", e)
            }
        }
    }

    private fun openChatActivity(chatId: String) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("CHAT_ID", chatId)
        }
        startActivity(intent)
        finish()
    }

    private fun fetchUserProfile(userId: String) {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPref.getString("user_token", null)

        if (token == null) {
            Log.e("UserProfileActivity", "Токен не найден")
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getUserById("Bearer $token", userId)
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    userProfile?.let {
                        updateUI(it)
                    }
                } else {
                    Log.e("UserProfileActivity", "Ошибка загрузки профиля: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("UserProfileActivity", "Ошибка сети", e)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(user: UserProfile) {
        usernameTextView.text = user.username ?: "N/A"

        val firstName = user.firstName ?: ""
        val lastName = user.lastName ?: ""
        fullNameTextView.text = "$firstName $lastName".trim()

        if (user.dob != null) {
            try {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                dobTextView.text = "Дата рождения: ${dateFormat.format(Date(user.dob))}"
                dobTextView.visibility = View.VISIBLE
            } catch (_: Exception) {
                dobTextView.visibility = View.GONE
            }
        } else {
            dobTextView.visibility = View.GONE
        }

        lastSeenTextView.text = formatLastSeen(user.lastSeen)
    }

    private fun formatLastSeen(lastSeen: Long?): String {
        if (lastSeen == null) {
            return "был(а) давно"
        }

        val now = System.currentTimeMillis()
        val diff = now - lastSeen

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "в сети"
            minutes < 60 -> "был(а) $minutes минут назад"
            hours < 24 -> "был(а) $hours часов назад"
            days == 1L -> "был(а) вчера"
            days < 7 -> "был(а) $days дней назад"
            else -> {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                "был(а) ${dateFormat.format(Date(lastSeen))}"
            }
        }
    }
}
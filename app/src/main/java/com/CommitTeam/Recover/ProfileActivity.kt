package com.CommitTeam.Recover

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit // 1. ИМПОРТИРУЕМ KTX
import androidx.lifecycle.lifecycleScope
import com.CommitTeam.Recover.models.UserProfile
import com.CommitTeam.Recover.Utils.ErrorUtils
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var firstNameTextView: TextView
    private lateinit var lastNameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        usernameTextView = findViewById(R.id.profile_username)
        emailTextView = findViewById(R.id.profile_email)
        firstNameTextView = findViewById(R.id.profile_first_name)
        lastNameTextView = findViewById(R.id.profile_last_name)

        loadCachedProfile()

        fetchUserProfileFromServer()
    }


    @SuppressLint("SetTextI18n")
    private fun loadCachedProfile() {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // 'email' у нас есть 100%
        val email = sharedPref.getString("user_email", "...")
        // 'username', 'firstName', 'lastName' могут быть null при первом заходе
        val username = sharedPref.getString("user_username", null)
        val firstName = sharedPref.getString("user_firstName", null)
        val lastName = sharedPref.getString("user_lastName", null)

        usernameTextView.text = "Имя пользователя: ${username ?: email ?: "..."}"
        emailTextView.text = "Email: ${email ?: "..."}"
        firstNameTextView.text = "Имя: ${firstName ?: "Не указано"}"
        lastNameTextView.text = "Фамилия: ${lastName ?: "Не указано"}"
    }


    private fun fetchUserProfileFromServer() {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPref.getString("user_token", null)

        if (token == null) {
            ErrorUtils.showCustomError(
                usernameTextView,
                supportFragmentManager,
                "Токен не найден. Пожалуйста, авторизуйтесь заново."
            )
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getUserProfile("Bearer $token")
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    userProfile?.let {

                        updateUI(it)


                        sharedPref.edit {
                            putString("user_email", it.email)
                            putString("user_username", it.username)
                            putString("user_firstName", it.firstName)
                            putString("user_lastName", it.lastName)
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    ErrorUtils.showCustomError(
                        usernameTextView,
                        supportFragmentManager,
                        "Ошибка обновления профиля",
                        errorBody
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Ошибка сети", e)
                ErrorUtils.showCustomError(
                    usernameTextView,
                    supportFragmentManager,
                    "Ошибка сети. Проверьте подключение.",
                    e.toString()
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(user: UserProfile) {
        usernameTextView.text = "Имя пользователя: ${user.username ?: "Не указано"}"
        emailTextView.text = "Email: ${user.email}"
        firstNameTextView.text = "Имя: ${user.firstName ?: "Не указано"}"
        lastNameTextView.text = "Фамилия: ${user.lastName ?: "Не указано"}"
    }
}
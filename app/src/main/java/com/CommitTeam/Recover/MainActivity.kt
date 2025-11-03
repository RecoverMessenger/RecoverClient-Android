package com.CommitTeam.Recover

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.CommitTeam.Recover.models.ErrorResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch
import com.CommitTeam.Recover.Utils.ErrorUtils
import com.CommitTeam.Recover.Utils.SuccessUtils

class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPref.getString("user_token", null)

        if (!token.isNullOrEmpty()) {
            Log.d("MainActivity", "Найден токен, автоматический вход...")
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val btnRegister = findViewById<Button>(R.id.btn_register)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        progressBar = findViewById(R.id.progress_bar)

        btnRegister.setOnClickListener {
            showRegisterDialog()
        }

        btnLogin.setOnClickListener {
            showLoginDialog()
        }
    }

    private fun showRegisterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_register, null)
        val emailInput = dialogView.findViewById<EditText>(R.id.input_email)
        val passwordInput = dialogView.findViewById<EditText>(R.id.input_password)
        val usernameInput = dialogView.findViewById<EditText>(R.id.input_username)
        val agreeCheckbox = dialogView.findViewById<CheckBox>(R.id.checkbox_agree)
        val termsTextView = dialogView.findViewById<TextView>(R.id.text_view_terms)

        termsTextView.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, "https://recovermsg.com/terms".toUri())
            startActivity(browserIntent)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Регистрация")
            .setView(dialogView)
            .setPositiveButton("Зарегистрироваться", null)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                if (!agreeCheckbox.isChecked) {
                    showError("Вы должны принять пользовательское соглашение и правила.")
                    return@setOnClickListener
                }

                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()
                val username = usernameInput.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    showError("Email и пароль обязательны.")
                } else {
                    showLoading(true)
                    lifecycleScope.launch {
                        try {
                            val request = RegisterRequest(email, username, password)
                            val response = RetrofitClient.instance.register(request)

                            if (response.isSuccessful) {
                                dialog.dismiss()
                                showUserInfoDialog()
                            } else {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                                val errorMessage = errorResponse?.error ?: errorResponse?.message ?: "Неизвестная ошибка."
                                showError(errorMessage, errorBody)
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Registration error", e)
                            showError("Не удалось подключиться к серверу. Проверьте подключение.", e.toString())
                        } finally {
                            showLoading(false)
                        }
                    }
                }
            }
        }
        dialog.show()
    }

    private fun showLoginDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_login, null)
        val emailInput = dialogView.findViewById<EditText>(R.id.input_email)
        val passwordInput = dialogView.findViewById<EditText>(R.id.input_password)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Авторизация")
            .setView(dialogView)
            .setPositiveButton("Войти", null)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {

                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    showError("Email и пароль обязательны.")
                } else {
                    showLoading(true)
                    lifecycleScope.launch {
                        try {
                            val request = LoginRequest(email, password)
                            val response = RetrofitClient.instance.login(request)

                            if (response.isSuccessful) {
                                val body = response.body()
                                val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                                sharedPref.edit {
                                    putString("user_token", body?.token)
                                    putString("user_id", body?.userId)
                                    putString("user_email", email)
                                }
                                dialog.dismiss()
                                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                                val errorMessage = errorResponse?.error ?: errorResponse?.message ?: "Неизвестная ошибка."

                                if (response.code() == 403 && errorResponse?.banInfo != null) {
                                    val banInfo = errorResponse.banInfo

                                    val reason = banInfo.reason ?: "Причина не указана."
                                    val banUntil = banInfo.banUntil?.toLong() ?: 0L

                                    dialog.dismiss()

                                    ErrorUtils.showBanSnackbar(
                                        progressBar,
                                        supportFragmentManager,
                                        reason,
                                        banUntil
                                    )
                                } else {
                                    showError(errorMessage, errorBody)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Login error", e)
                            showError("Не удалось подключиться к серверу. Проверьте подключение.", e.toString())
                        } finally {
                            showLoading(false)
                        }
                    }
                }
            }
        }
        dialog.show()
    }

    private fun showUserInfoDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user_info, null)

        AlertDialog.Builder(this)
            .setTitle("Ваши данные")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialog, _ ->

                showSuccess()

                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
                dialog.dismiss()
            }
            .setNegativeButton("Пропустить") { dialog, _ ->
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String, details: String? = null) {
        ErrorUtils.showCustomError(
            progressBar,
            supportFragmentManager,
            message,
            details
        )
    }

    private fun showSuccess() {
        SuccessUtils.showSuccessSnackbar(progressBar, "Данные сохранены!")
    }
}
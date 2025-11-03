package com.CommitTeam.Recover.settings

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.CommitTeam.Recover.R
import com.CommitTeam.Recover.RetrofitClient
import com.CommitTeam.Recover.Utils.ErrorUtils
import com.CommitTeam.Recover.Utils.SuccessUtils
import com.CommitTeam.Recover.models.UpdateProfileRequest
import com.CommitTeam.Recover.models.UserProfile
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditProfileActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var usernameInput: TextInputEditText
    private lateinit var firstNameInput: TextInputEditText
    private lateinit var lastNameInput: TextInputEditText
    private lateinit var dobTextView: TextView
    private lateinit var saveButton: Button

    private val calendar = Calendar.getInstance()
    private var dobTimestamp: Long? = null

    private val usernameRegex = Regex("^[a-zA-Z0-9_]{4,50}$")
    private val usernameErrorMessage = "Имя пользователя должно быть от 4 до 50 символов и содержать только буквы, цифры и _"
    private val usernameErrorCode = "ERR-LOCAL_CLIENT::CHANGE_USERNAME"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        toolbar = findViewById(R.id.edit_profile_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Редактирование профиля"

        usernameInput = findViewById(R.id.settings_username_input)
        firstNameInput = findViewById(R.id.settings_firstname_input)
        lastNameInput = findViewById(R.id.settings_lastname_input)
        dobTextView = findViewById(R.id.settings_dob_text)
        saveButton = findViewById(R.id.settings_save_button)

        loadCachedProfile()
        fetchProfileFromServer()

        setupDatePicker()

        saveButton.setOnClickListener {
            saveChanges()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDobLabel()
        }

        dobTextView.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDobLabel() {
        val format = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        dobTextView.text = sdf.format(calendar.time)
        dobTimestamp = calendar.timeInMillis
    }

    private fun loadCachedProfile() {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        usernameInput.setText(sharedPref.getString("user_username", ""))
        firstNameInput.setText(sharedPref.getString("user_firstName", ""))
        lastNameInput.setText(sharedPref.getString("user_lastName", ""))
    }

    private fun fetchProfileFromServer() {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPref.getString("user_token", null)

        if (token == null) {
            showError("Токен не найден")
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getUserProfile("Bearer $token")
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    userProfile?.let {
                        updateUiWithProfile(it)
                    }
                } else {
                    showError("Не удалось загрузить профиль", response.errorBody()?.string())
                }
            } catch (e: Exception) {
                Log.e("EditProfileActivity", "Ошибка загрузки профиля", e)
                showError("Ошибка сети", e.toString())
            }
        }
    }

    private fun updateUiWithProfile(user: UserProfile) {
        usernameInput.setText(user.username)
        firstNameInput.setText(user.firstName)
        lastNameInput.setText(user.lastName)
    }

    private fun saveChanges() {
        val newUsername = usernameInput.text.toString().trim()
        val newFirstName = firstNameInput.text.toString().trim()
        val newLastName = lastNameInput.text.toString().trim()

        if (!usernameRegex.matches(newUsername)) {
            showError(usernameErrorMessage, usernameErrorCode)
            return
        }

        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPref.getString("user_token", null)

        if (token == null) {
            showError("Токен не найден. Пожалуйста, авторизуйтесь заново.")
            return
        }

        val request = UpdateProfileRequest(
            username = newUsername,
            firstName = newFirstName.ifEmpty { null },
            lastName = newLastName.ifEmpty { null },
            dob = dobTimestamp
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.updateProfile("Bearer $token", request)
                if (response.isSuccessful) {
                    showSuccess()

                    sharedPref.edit {
                        putString("user_username", newUsername)
                        putString("user_firstName", newFirstName.ifEmpty { null })
                        putString("user_lastName", newLastName.ifEmpty { null })
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    showError("Ошибка обновления профиля", errorBody)
                }
            } catch (e: Exception) {
                Log.e("EditProfileActivity", "Ошибка сохранения", e)
                showError("Ошибка сети", e.toString())
            }
        }
    }

    private fun showError(message: String, details: String? = null) {
        ErrorUtils.showCustomError(
            saveButton,
            supportFragmentManager,
            message,
            details
        )
    }

    private fun showSuccess() {
        SuccessUtils.showSuccessSnackbar(saveButton, "Профиль успешно обновлён!")
    }
}
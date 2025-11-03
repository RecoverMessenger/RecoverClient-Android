package com.CommitTeam.Recover

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.CommitTeam.Recover.databinding.ActivityHomeBinding
import com.CommitTeam.Recover.Utils.ErrorUtils
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var binding: ActivityHomeBinding
    private lateinit var chatListAdapter: ChatListAdapter

    private val originalTitle = "Recover Messenger"
    private var titleAnimationJob: Job? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("HomeActivity", "Разрешение на уведомления получено")
        } else {
            Log.w("HomeActivity", "Разрешение на уведомления отклонено")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = originalTitle

        drawerLayout = binding.drawerLayout
        val navigationView = binding.navView

        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val headerView = navigationView.getHeaderView(0)
        val navHeaderEmail = headerView.findViewById<TextView>(R.id.nav_header_email)
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val userEmail = sharedPref.getString("user_email", "user@example.com")
        val userId = sharedPref.getString("user_id", null)
        navHeaderEmail.text = userEmail

        if (userId != null) {
            WebSocketClient.connect(userId)
        } else {
            logout()
            return
        }

        setupRecyclerView()
        collectConnectionStatus()

        // --- ИЗМЕНЕНИЕ: Вызываем функцию запроса разрешений ---
        askNotificationPermission()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        fetchChats()
    }

    private fun setupRecyclerView() {
        chatListAdapter = ChatListAdapter(emptyList())
        binding.chatsRecyclerView.apply {
            adapter = chatListAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }
    }

    private fun fetchChats() {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPref.getString("user_token", null)
        if (token == null) {
            logout()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getChats("Bearer $token")
                if (response.isSuccessful) {
                    val chats = response.body() ?: emptyList()
                    runOnUiThread {
                        chatListAdapter.updateData(chats)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("HomeActivity", "Ошибка загрузки чатов: $errorBody")
                    ErrorUtils.showCustomError(
                        binding.root,
                        supportFragmentManager,
                        "Не удалось загрузить чаты",
                        errorBody
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Сетевая ошибка при загрузке чатов", e)
                ErrorUtils.showCustomError(
                    binding.root,
                    supportFragmentManager,
                    "Ошибка сети. Проверьте подключение.",
                    e.toString()
                )
            }
        }
    }

    // --- ИЗМЕНЕНИЕ: Реализована логика запроса разрешений ---
    private fun askNotificationPermission() {
        // Это требуется только для Android 13 (API 33) и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Разрешение уже есть
                Log.d("HomeActivity", "Разрешение на уведомления уже предоставлено")
            } else {
                // Разрешения нет, запрашиваем его
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun collectConnectionStatus() {
        lifecycleScope.launch {
            WebSocketClient.connectionState.collect { state ->
                when (state) {
                    is ConnectionState.Connecting -> {
                        startTitleAnimation("Соединение")
                    }
                    is ConnectionState.Connected -> {
                        stopTitleAnimation()
                        supportActionBar?.title = originalTitle
                    }
                    is ConnectionState.Disconnected -> {
                        stopTitleAnimation()
                        supportActionBar?.title = "Нет соединения"
                    }
                    is ConnectionState.WaitingForNetwork -> {
                        startTitleAnimation("Ожидание сети")
                    }
                    else -> {
                        stopTitleAnimation()
                        supportActionBar?.title = originalTitle
                    }
                }
            }
        }
    }

    private fun startTitleAnimation(baseText: String) {
        stopTitleAnimation()

        titleAnimationJob = lifecycleScope.launch {
            var dotCount = 0
            while (isActive) {
                dotCount = (dotCount + 1) % 4
                val dots = ".".repeat(dotCount)

                supportActionBar?.title = "$baseText$dots"

                delay(500)
            }
        }
    }

    private fun stopTitleAnimation() {
        titleAnimationJob?.cancel()
        titleAnimationJob = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTitleAnimation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                logout()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logout() {
        WebSocketClient.disconnect()

        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit {
            clear()
        }
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
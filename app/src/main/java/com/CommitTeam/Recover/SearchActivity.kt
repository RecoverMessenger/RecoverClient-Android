package com.CommitTeam.Recover

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.CommitTeam.Recover.databinding.ActivitySearchBinding
import com.CommitTeam.Recover.models.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var userSearchAdapter: UserSearchAdapter
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Подключаем ViewBinding
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecycler()
        setupSearchView()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.searchToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupRecycler() {
        userSearchAdapter = UserSearchAdapter(this, emptyList())
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchResultsRecyclerView.adapter = userSearchAdapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchJob?.cancel()
                performSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                if (newText.isNullOrBlank()) {
                    showSearchState(null)
                } else {
                    searchJob = lifecycleScope.launch {
                        delay(300)
                        performSearch(newText)
                    }
                }
                return true
            }
        })
    }

    private fun showLoading() {
        binding.searchResultsRecyclerView.visibility = View.GONE
        binding.searchStatusTextView.visibility = View.GONE
        binding.searchProgressBar.visibility = View.VISIBLE
    }

    private fun showSearchState(message: String?) {
        binding.searchProgressBar.visibility = View.GONE
        if (message == null) {
            binding.searchResultsRecyclerView.visibility = View.GONE
            binding.searchStatusTextView.visibility = View.GONE
        } else {
            binding.searchResultsRecyclerView.visibility = View.GONE
            binding.searchStatusTextView.visibility = View.VISIBLE
            binding.searchStatusTextView.text = message
        }
    }

    private fun showSearchResults(users: List<UserProfile>) {
        binding.searchProgressBar.visibility = View.GONE
        binding.searchStatusTextView.visibility = View.GONE
        binding.searchResultsRecyclerView.visibility = View.VISIBLE
        userSearchAdapter.updateData(users)
    }

    private fun performSearch(query: String?) {
        if (query.isNullOrBlank()) {
            showSearchState(null)
            return
        }

        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPref.getString("user_token", null)

        if (token == null) {
            showSearchState("Ошибка: Вы не авторизованы")
            return
        }

        showLoading()

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.searchUsers("Bearer $token", query)
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    if (users.isNotEmpty()) {
                        showSearchResults(users)
                    } else {
                        showSearchState("Ничего не найдено")
                    }
                } else {
                    Log.e("SearchActivity", "Search error: ${response.errorBody()?.string()}")
                    showSearchState("Ошибка сервера")
                }
            } catch (e: Exception) {
                Log.e("SearchActivity", "Network error during search", e)
                showSearchState("Отсутствует соединение")
            }
        }
    }
}

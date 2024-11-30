package com.nabila.storyappdicoding.ui.story

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.nabila.storyappdicoding.R
import com.nabila.storyappdicoding.data.pref.UserPreference
import com.nabila.storyappdicoding.data.pref.dataStore
import com.nabila.storyappdicoding.data.repository.UserRepository
import com.nabila.storyappdicoding.ui.login.Result
import com.nabila.storyappdicoding.databinding.ActivityStoryListBinding
import com.nabila.storyappdicoding.ui.welcome.WelcomeActivity
import com.nabila.storyappdicoding.data.remote.ApiConfig
import com.nabila.storyappdicoding.data.remote.ApiService
import com.nabila.storyappdicoding.ui.addstory.AddStoryActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class StoryListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryListBinding
    private val userPreference: UserPreference by lazy {
        UserPreference.getInstance(dataStore)
    }
    private val apiService: ApiService by lazy {
        val token = runBlocking { userPreference.getSession().first().token } // Mengambil token dari UserPreference
        Log.d(TAG, "Token: $token")
        ApiConfig.getApiService(token) // Memberikan token sebagai parameter
    }
    private val userRepository: UserRepository by lazy {
        UserRepository.getInstance(apiService, userPreference) // Memberikan apiService dan userPreference sebagai parameter
    }
    private val viewModel: StoryListViewModel by viewModels {
        StoryListViewModelFactory(userRepository)
    }
    private lateinit var adapter: StoryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupNavigationDrawer()

        viewModel.getSession().observe(this) { user ->
            if (user.isLogin) {
                val token = user.token
                viewModel.getStories(token)
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        adapter = StoryListAdapter()
        binding.rvStories.layoutManager = LinearLayoutManager(this)
        binding.rvStories.adapter = adapter

        viewModel.stories.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Success -> {
                    showLoading(false)
                    adapter.submitList(result.data)
                }
                is Result.Error -> {
                    showLoading(false)
                    Log.e(TAG, "Error getting stories: ${result.error}")
                }
            }
        }

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        binding.rvStories.layoutManager = LinearLayoutManager(this)
        adapter = StoryListAdapter()
        binding.rvStories.adapter = adapter
    }

    private fun setupNavigationDrawer() {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        binding.menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    viewModel.logout() // Memanggil logout() dari instance viewModel
                    val intent = Intent(this, WelcomeActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TAG = "StoryListActivity"
    }
}
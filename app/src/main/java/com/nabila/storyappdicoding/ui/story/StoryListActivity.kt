package com.nabila.storyappdicoding.ui.story

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.navigation.NavigationView
import com.nabila.storyappdicoding.R
import com.nabila.storyappdicoding.data.pref.UserModel
import com.nabila.storyappdicoding.data.pref.UserPreference
import com.nabila.storyappdicoding.data.pref.dataStore
import com.nabila.storyappdicoding.data.repository.UserRepository
import com.nabila.storyappdicoding.ui.login.Result
import com.nabila.storyappdicoding.databinding.ActivityStoryListBinding
import com.nabila.storyappdicoding.ui.welcome.WelcomeActivity
import com.nabila.storyappdicoding.data.remote.ApiConfig
import com.nabila.storyappdicoding.data.remote.ApiService
import com.nabila.storyappdicoding.di.Injection
import com.nabila.storyappdicoding.ui.addstory.AddStoryActivity
import com.nabila.storyappdicoding.ui.worker.SaveSessionWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class StoryListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryListBinding
    // hapus nanti
    /*private val userPreference: UserPreference by lazy {
        UserPreference.getInstance(dataStore)
    }*/
    private val userRepository: UserRepository by lazy {
        Injection.provideRepository(this)
    }
    // hapus nanti
    /*private val apiService: ApiService by lazy {
        userRepository.apiService
    }*/
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

        /*viewModel.getSession().observe(this) { user ->
            if (user.isLogin) {
                viewModel.getStories("Bearer ${user.token}")
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }*/
        userRepository.getSession().asLiveData().observe(this) { user ->
            if (user.isLogin) {
                viewModel.getStories("Bearer ${user.token}")
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        // Jadwalkan WorkRequest
        val saveSessionRequest = PeriodicWorkRequestBuilder<SaveSessionWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "saveSession",
            ExistingPeriodicWorkPolicy.REPLACE, // Gunakan REPLACE
            saveSessionRequest
        )

        // Pantau status WorkRequest
        val workInfoLiveData = WorkManager.getInstance(this).getWorkInfoByIdLiveData(saveSessionRequest.id)
        workInfoLiveData.observe(this) { workInfo ->
            if (workInfo != null) {
                Log.d("StoryListActivity", "WorkInfo state: ${workInfo.state}")
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    Log.d("StoryListActivity", "SaveSessionWorker has successfully saved the session.")
                } else if (workInfo.state == WorkInfo.State.FAILED) {
                    Log.e("StoryListActivity", "SaveSessionWorker failed to save the session.")
                }
            }
        }

        //Log.d("StoryListActivity", "WorkRequest 'saveSession' enqueued.")

        viewModel.stories.observe(this) { result ->
            if (result is Result.Success) {
                adapter.submitList(result.data)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        /*adapter = StoryListAdapter()
        binding.rvStories.layoutManager = LinearLayoutManager(this)
        binding.rvStories.adapter = adapter*/

        /*viewModel.stories.observe(this) { result ->
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
        }*/

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        lifecycleScope.launch {
            val user = userRepository.getSession().first()
            outState.putString("email", user.email)
            outState.putString("token", user.token)
            outState.putBoolean("isLogin", user.isLogin)
            Log.d("StoryListActivity", "onSaveInstanceState: Saving session: $user")
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val email = savedInstanceState.getString("email", "")
        val token = savedInstanceState.getString("token", "")
        val isLogin = savedInstanceState.getBoolean("isLogin", false)
        val user = UserModel(email, token, isLogin)
        lifecycleScope.launch {
            userRepository.saveSession(user)
            Log.d("StoryListActivity", "onRestoreInstanceState: Restoring session: $user")
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

        // cek ini
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    lifecycleScope.launch { // Gunakan lifecycleScope.launch
                        Log.d("StoryListActivity", "User logging out")
                        userRepository.logout() // Panggil userRepository.logout() di dalam coroutine
                        Log.d("StoryListActivity", "User logged out")
                        val intent = Intent(this@StoryListActivity, WelcomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    true // Menandakan item telah diproses
                }
                else -> false // Menandakan item tidak diproses
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
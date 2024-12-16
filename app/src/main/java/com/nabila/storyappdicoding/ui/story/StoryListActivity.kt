package com.nabila.storyappdicoding.ui.story

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.navigation.NavigationView
import com.nabila.storyappdicoding.R
import com.nabila.storyappdicoding.data.pref.UserModel
import com.nabila.storyappdicoding.data.repository.UserRepository
import com.nabila.storyappdicoding.databinding.ActivityStoryListBinding
import com.nabila.storyappdicoding.ui.welcome.WelcomeActivity
import com.nabila.storyappdicoding.di.Injection
import com.nabila.storyappdicoding.ui.addstory.AddStoryActivity
import com.nabila.storyappdicoding.ui.maps.MapsActivity
import com.nabila.storyappdicoding.ui.worker.SaveSessionWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class StoryListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryListBinding
    private val userRepository: UserRepository by lazy {
        Injection.provideRepository(this)
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
        adapter = StoryListAdapter()
        binding.rvStories.adapter = adapter

        userRepository.getSession().asLiveData().observe(this) { user ->
            if (user.isLogin) {
                viewModel.getStories("Bearer ${user.token}")
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        val saveSessionRequest = PeriodicWorkRequestBuilder<SaveSessionWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "saveSession",
            ExistingPeriodicWorkPolicy.UPDATE,
            saveSessionRequest
        )

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

        viewModel.storyPagingData.observe(this) { pagingData ->
            lifecycleScope.launch {
                adapter.submitData(pagingData)
            }
        }

        adapter.addLoadStateListener { loadState ->
            binding.progressBar.visibility = if (loadState.refresh is LoadState.Loading) View.VISIBLE else View.GONE

            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
            errorState?.let {
                Toast.makeText(
                    this@StoryListActivity, "Error: ${it.error.message}", Toast.LENGTH_LONG // Durasi Toast
                ).show()
            }
            Log.d("StoryListActivity", "LoadState: $loadState")
        }

        lifecycleScope.launch {
            val user = userRepository.getSession().first()
            if (!user.isLogin) {
                startActivity(Intent(this@StoryListActivity, WelcomeActivity::class.java))
                finish()
            }
        }

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
                    lifecycleScope.launch {
                        Log.d("StoryListActivity", "User logging out")
                        userRepository.logout()
                        Log.d("StoryListActivity", "User logged out")
                        withContext(Dispatchers.Main) {
                            val intent = Intent(this@StoryListActivity, WelcomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                    true
                }
                R.id.nav_map -> {
                    startActivity(Intent(this, MapsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    companion object {
        private const val TAG = "StoryListActivity"
    }
}
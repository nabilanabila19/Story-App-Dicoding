package com.nabila.storyappdicoding.ui.story

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.nabila.storyappdicoding.MainActivity
import com.nabila.storyappdicoding.R
import com.nabila.storyappdicoding.ViewModelFactory
import com.nabila.storyappdicoding.data.pref.UserModel
import com.nabila.storyappdicoding.databinding.ActivityStoryListBinding
import com.nabila.storyappdicoding.ui.welcome.WelcomeActivity

class StoryListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryListBinding
    private val storyListViewModel by viewModels<StoryListViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var adapter: StoryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        storyListViewModel.story.observe(this) {
            adapter.submitList(it)
        }

        storyListViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu1 -> {
                storyListViewModel.getSession().observe(this) { user ->
                    if (user.isLogin) {
                        val intent = Intent(this, MapsStoryActivity::class.java)
                        startActivity(intent)
                    } else {
                        startActivity(Intent(this, WelcomeActivity::class.java))
                        finish()
                    }
                }
                return true
            }
            R.id.menu2 -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                return true
            }
            R.id.menu3 -> {
                storyListViewModel.logout()
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            else -> return true
        }
    }

    private fun setupRecyclerView() {
        binding.rvStories.layoutManager = LinearLayoutManager(this)
        adapter = StoryListAdapter()
        binding.rvStories.adapter = adapter
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
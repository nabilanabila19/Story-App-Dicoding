package com.nabila.storyappdicoding.ui.welcome

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nabila.storyappdicoding.data.pref.UserModel
import com.nabila.storyappdicoding.databinding.ActivityWelcomeBinding
import com.nabila.storyappdicoding.di.Injection
import com.nabila.storyappdicoding.ui.login.LoginActivity
import com.nabila.storyappdicoding.ui.signup.SignupActivity
import com.nabila.storyappdicoding.ui.story.StoryListActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
        playAnimation()
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            val userRepository = Injection.provideRepository(this@WelcomeActivity)
            val user: UserModel? = userRepository.getUser().first()

            if (user?.isLogin == true && user.token.isNotEmpty()) {
                startActivity(Intent(this@WelcomeActivity, StoryListActivity::class.java))
                finish()
            } else {
                Log.d("WelcomeActivity", "Pengguna belum login atau token kosong.")
            }
        }
    }


    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {

        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        try {
            binding.loginButton.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        } catch (e: Exception) {
            Log.e("WelcomeActivity", "Error: ${e.message}")
        }
    }

    private fun playAnimation() {
        println("Animasi dimulai")
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val login = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(100)
        val signup = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(100)
        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val desc = ObjectAnimator.ofFloat(binding.descTextView, View.ALPHA, 1f).setDuration(100)

        val together = AnimatorSet().apply {
            playTogether(login, signup)
        }

        AnimatorSet().apply {
            playSequentially(title, desc, together)
            start()
        }
    }
}
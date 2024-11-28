package com.nabila.storyappdicoding.ui.signup

import androidx.lifecycle.ViewModel
import com.nabila.storyappdicoding.data.repository.UserRepository
import javax.inject.Inject

class SignupViewModel @Inject constructor(private val userRepository: UserRepository) :
    ViewModel() {
    suspend fun register(name: String, email: String, password: String) =
        userRepository.register(name, email, password)
}
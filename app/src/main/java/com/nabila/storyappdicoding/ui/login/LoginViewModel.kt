package com.nabila.storyappdicoding.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nabila.storyappdicoding.data.pref.UserModel
import com.nabila.storyappdicoding.data.repository.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {
    suspend fun login(email: String, password: String) = userRepository.login(email, password)

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            userRepository.saveSession(user)
        }
    }
}
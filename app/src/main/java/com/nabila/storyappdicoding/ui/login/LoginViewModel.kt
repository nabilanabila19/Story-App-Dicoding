package com.nabila.storyappdicoding.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nabila.storyappdicoding.data.pref.UserModel
import com.nabila.storyappdicoding.data.repository.UserRepository
import com.nabila.storyappdicoding.utils.Result
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {
    private val _loginResult = MutableLiveData<Result<UserModel>>()
    val loginResult: LiveData<Result<UserModel>> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = Result.Loading
            try {
                val response = userRepository.login(email, password)
                if (!response.error) {
                    val user = UserModel(
                        email = email,
                        token = response.loginResult.token,
                        isLogin = true
                    )
                    userRepository.saveSession(user)
                    _loginResult.value = Result.Success(user)
                } else {
                    _loginResult.value = Result.Error(response.message)
                }
            } catch (e: Exception) {
                _loginResult.value = Result.Error(e.message.toString())
            }
        }
    }
}
package com.nabila.storyappdicoding.data.repository

import com.nabila.storyappdicoding.data.pref.UserModel
import com.nabila.storyappdicoding.data.pref.UserPreference
import com.nabila.storyappdicoding.data.remote.ApiService
import com.nabila.storyappdicoding.data.response.LoginResponse
import com.nabila.storyappdicoding.data.response.RegisterResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {
    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return apiService.register(name, email, password)
    }

    suspend fun login(email: String, password: String): LoginResponse {
        return apiService.login(email, password)
    }

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }
}
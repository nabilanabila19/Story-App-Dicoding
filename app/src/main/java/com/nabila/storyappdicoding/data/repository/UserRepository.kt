package com.nabila.storyappdicoding.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.nabila.storyappdicoding.data.pref.UserModel
import com.nabila.storyappdicoding.data.pref.UserPreference
import com.nabila.storyappdicoding.data.remote.ApiService
import com.nabila.storyappdicoding.data.response.DetailStoryResponse
import com.nabila.storyappdicoding.data.response.LoginResponse
import com.nabila.storyappdicoding.data.response.RegisterResponse
import com.nabila.storyappdicoding.data.response.StoryResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.await
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

    suspend fun getStories(token: String): StoryResponse {
        Log.d(TAG, "Get stories with token: $token")
        return apiService.getStories()
    }

    suspend fun getDetailStory(token: String, id: String): DetailStoryResponse {
        return apiService.getDetailStory(token, id).await()
    }

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(apiService: ApiService, userPreference: UserPreference): UserRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = UserRepository(apiService, userPreference)
                INSTANCE = instance
                instance
            }
        }
    }
}
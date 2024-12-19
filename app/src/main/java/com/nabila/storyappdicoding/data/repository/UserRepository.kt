package com.nabila.storyappdicoding.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.nabila.storyappdicoding.data.model.Story
import com.nabila.storyappdicoding.data.pref.UserModel
import com.nabila.storyappdicoding.data.pref.UserPreference
import com.nabila.storyappdicoding.data.remote.ApiService
import com.nabila.storyappdicoding.data.response.DetailStoryResponse
import com.nabila.storyappdicoding.data.response.LoginResponse
import com.nabila.storyappdicoding.data.response.RegisterResponse
import com.nabila.storyappdicoding.data.response.StoryResponse
import com.nabila.storyappdicoding.ui.story.StoryPagingSource
import kotlinx.coroutines.flow.Flow
import retrofit2.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val context: Context,
    val apiService: ApiService
) {
    private val userPreference: UserPreference by lazy {
        UserPreference.getInstance(context)
    }

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

    fun getUser(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    /*suspend fun getStories(token: String, page: Int, size: Int): StoryResponse {
        Log.d(TAG, "Get stories with token: $token, page: $page, size: $size")
        return apiService.getStories(page, size)
    }*/
    fun getStories(token: String): Flow<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StoryPagingSource(apiService, token) }
        ).flow
    }

    suspend fun getDetailStory(token: String, id: String): DetailStoryResponse {
        return apiService.getDetailStory(token, id).await()
    }

    suspend fun getStoriesWithLocation(token: String): StoryResponse {
        Log.d(TAG, "Get stories with location token: $token")
        return apiService.getStoriesWithLocation()
    }

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(context: Context, apiService: ApiService): UserRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = UserRepository(context, apiService)
                INSTANCE = instance
                instance
            }
        }
    }
}
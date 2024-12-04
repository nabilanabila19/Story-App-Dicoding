package com.nabila.storyappdicoding.di


import com.nabila.storyappdicoding.data.remote.ApiConfig
import com.nabila.storyappdicoding.data.repository.UserRepository
import android.content.Context

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val apiService = ApiConfig.getApiService(context)
        return UserRepository.getInstance(context, apiService)
    }
}
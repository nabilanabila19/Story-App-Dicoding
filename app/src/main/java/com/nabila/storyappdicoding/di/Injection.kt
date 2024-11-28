package com.nabila.storyappdicoding.di

import android.content.Context
import com.nabila.storyappdicoding.data.pref.UserPreference
import com.nabila.storyappdicoding.data.pref.dataStore
import com.nabila.storyappdicoding.data.remote.ApiConfig
import com.nabila.storyappdicoding.data.repository.UserRepository

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService() // Buat instance ApiService
        return UserRepository(apiService, pref) // Gunakan constructor yang menerima ApiService
    }
}
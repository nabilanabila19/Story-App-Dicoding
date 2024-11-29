package com.nabila.storyappdicoding.di

import android.content.Context
import com.nabila.storyappdicoding.data.pref.UserPreference
import com.nabila.storyappdicoding.data.pref.dataStore
import com.nabila.storyappdicoding.data.remote.ApiConfig
import com.nabila.storyappdicoding.data.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() } // Ambil data user dari DataStore
        val apiService = ApiConfig.getApiService(user.token) // Buat instance ApiService dengan token
        return UserRepository(apiService, pref) // Gunakan constructor yang menerima ApiService
    }
}
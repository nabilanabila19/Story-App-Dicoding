package com.nabila.storyappdicoding.di


import com.nabila.storyappdicoding.data.remote.ApiConfig
import com.nabila.storyappdicoding.data.repository.UserRepository
import com.nabila.storyappdicoding.ui.story.StoryListActivity
import android.content.Context

object Injection {
    fun provideRepository(context: Context): UserRepository { // Ubah parameter menjadi Context
        val apiService = ApiConfig.getApiService(context)
        return UserRepository.getInstance(context, apiService)
    }
}
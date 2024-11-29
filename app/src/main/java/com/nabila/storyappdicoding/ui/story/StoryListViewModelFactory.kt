package com.nabila.storyappdicoding.ui.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nabila.storyappdicoding.data.repository.UserRepository

class StoryListViewModelFactory(private val repository: UserRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoryListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.nabila.storyappdicoding.ui.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nabila.storyappdicoding.data.repository.UserRepository

class MapsViewModelFactory(private val userRepository: UserRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapsViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
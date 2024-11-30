package com.nabila.storyappdicoding.ui.addstory

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.get

class AddStoryViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var currentImageUri: Uri?
        get() = savedStateHandle.get<String>(CURRENT_IMAGE_URI_KEY)?.let { Uri.parse(it) }
        set(value) {
            savedStateHandle[CURRENT_IMAGE_URI_KEY] = value?.toString()
        }

    companion object {
        private const val CURRENT_IMAGE_URI_KEY = "currentImageUri"
    }
}
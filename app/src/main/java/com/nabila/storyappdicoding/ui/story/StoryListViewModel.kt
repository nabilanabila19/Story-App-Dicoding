package com.nabila.storyappdicoding.ui.story

import android.util.Log
import androidx.activity.result.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.nabila.storyappdicoding.data.pref.UserModel
import com.nabila.storyappdicoding.data.repository.UserRepository
import com.nabila.storyappdicoding.data.response.ListStoryItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StoryListViewModel(private val userRepository: UserRepository) :
    ViewModel() {
    private val _stories = MutableLiveData<List<ListStoryItem>>()
    val stories: LiveData<List<ListStoryItem>> = _stories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getSession(): LiveData<UserModel> {
        return userRepository.getSession().asLiveData()
    }

    fun getStories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = userRepository.getSession().first()
                val response = userRepository.getStories(user.token)
                _stories.value = response.listStory?.filterNotNull() ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting stories: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        private const val TAG = "StoryListViewModel"
    }
}
package com.nabila.storyappdicoding.ui.story

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.nabila.storyappdicoding.data.pref.UserModel
import com.nabila.storyappdicoding.ui.login.Result
import com.nabila.storyappdicoding.data.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StoryListViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _stories = MutableLiveData<Result<List<com.nabila.storyappdicoding.data.model.Story>>>()
    val stories: LiveData<Result<List<com.nabila.storyappdicoding.data.model.Story>>> = _stories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getSession(): LiveData<UserModel> {
        return userRepository.getSession().asLiveData()
    }

    fun getStories(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = userRepository.getSession().first()
                val response = userRepository.getStories(token)
                Log.d(TAG, "Story Response: $response")
                val stories = response.listStory?.map { storyItem ->
                    com.nabila.storyappdicoding.data.model.Story(
                        photoUrl = storyItem?.photoUrl ?: "",
                        createdAt = storyItem?.createdAt ?: "",
                        name = storyItem?.name ?: "",
                        description = storyItem?.description ?: "",
                        lon = storyItem?.lon,
                        id = storyItem?.id ?: "",
                        lat = storyItem?.lat
                    )
                } ?: emptyList()
                _stories.value = Result.Success(stories)
                Log.d(TAG, "Stories: $stories")
            } catch (e: Exception) {
                _stories.value = Result.Error(e.message.toString())
                Log.e(TAG, "Error getting stories: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    companion object {
        private const val TAG = "StoryListViewModel"
    }
}
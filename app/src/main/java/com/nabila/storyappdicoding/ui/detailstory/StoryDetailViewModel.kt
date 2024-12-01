package com.nabila.storyappdicoding.ui.detailstory

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nabila.storyappdicoding.data.pref.UserPreference
import com.nabila.storyappdicoding.data.repository.UserRepository
import com.nabila.storyappdicoding.data.response.DetailStoryResponse
import kotlinx.coroutines.launch

class StoryDetailViewModel(private val pref: UserPreference, private val userRepository: UserRepository) : ViewModel() {
    private val _storyDetail = MutableLiveData<DetailStoryResponse>()
    val storyDetail: LiveData<DetailStoryResponse> = _storyDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun getStoryDetail(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                pref.getSession().collect { user ->
                    val token = user.token
                    val detailStoryResponse = userRepository.getDetailStory("Bearer $token", id)
                    _storyDetail.value = detailStoryResponse
                }
            } catch (e: Exception) {
                _isError.value = true
                _errorMessage.value = e.message.toString()
                Log.e(TAG, "Error getting story detail: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
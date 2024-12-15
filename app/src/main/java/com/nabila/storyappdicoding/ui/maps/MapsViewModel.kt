package com.nabila.storyappdicoding.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nabila.storyappdicoding.data.model.Story
import com.nabila.storyappdicoding.data.repository.UserRepository
import com.nabila.storyappdicoding.utils.Result
import com.nabila.storyappdicoding.utils.Result.*
import kotlinx.coroutines.launch

class MapsViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _storiesWithLocation = MutableLiveData<Result<List<Story>>>()
    val storiesWithLocation: LiveData<Result<List<Story>>> = _storiesWithLocation

    fun getStoriesWithLocation(token: String) {
        viewModelScope.launch {
            _storiesWithLocation.value = Loading
            try {
                val response = userRepository.getStoriesWithLocation(token)
                val stories = response.listStory?.mapNotNull { storyItem ->
                    storyItem?.let {
                        Story(
                            it.id ?: "",
                            it.name ?: "",
                            it.description ?: "",
                            it.photoUrl ?: "",
                            it.createdAt ?: "",
                            it.lat,
                            it.lon
                        )
                    }
                } ?: emptyList()
                _storiesWithLocation.value = Success(stories)
            } catch (e: Exception) {
                _storiesWithLocation.value = Error(e.message.toString())
            }
        }
    }
}
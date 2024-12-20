package com.nabila.storyappdicoding.ui.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nabila.storyappdicoding.data.model.Story
import com.nabila.storyappdicoding.data.repository.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StoryListViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _storyPagingData = MutableLiveData<PagingData<Story>>()
    val storyPagingData: LiveData<PagingData<Story>> = _storyPagingData

    fun getStories(token: String) {
        /*viewModelScope.launch {
            val pager = Pager(
                config = PagingConfig(
                    pageSize = 20,
                    initialLoadSize = 20,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { StoryPagingSource(userRepository, token) }
            ).flow.cachedIn(viewModelScope)

            pager.collectLatest { pagingData ->
                _storyPagingData.value = pagingData
            }
        }*/
        viewModelScope.launch {
            viewModelScope.launch {
                userRepository.getStories(token)
                    .cachedIn(viewModelScope)
                    .collectLatest { pagingData ->
                        _storyPagingData.value = pagingData
                    }
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
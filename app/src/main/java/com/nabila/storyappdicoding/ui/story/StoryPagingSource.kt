package com.nabila.storyappdicoding.ui.story

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nabila.storyappdicoding.data.model.Story
import com.nabila.storyappdicoding.data.repository.UserRepository

class StoryPagingSource(private val userRepository: UserRepository, private val token: String) :
    PagingSource<Int, Story>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val responseData = userRepository.getStories(token, position, params.loadSize)

            LoadResult.Page(
                data = responseData.listStory?.map { storyItem ->
                    Story(
                        photoUrl = storyItem?.photoUrl ?: "",
                        createdAt = storyItem?.createdAt ?: "",
                        name = storyItem?.name ?: "",
                        description = storyItem?.description ?: "",
                        lon = storyItem?.lon,
                        id = storyItem?.id ?: "",
                        lat = storyItem?.lat
                    )
                } ?: emptyList(),
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (responseData.listStory.isNullOrEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
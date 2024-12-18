package com.nabila.storyappdicoding.ui.story

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.*
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.nabila.storyappdicoding.DataDummy
import com.nabila.storyappdicoding.MainDispatcherRule
import com.nabila.storyappdicoding.data.model.Story
import com.nabila.storyappdicoding.data.repository.UserRepository
import com.nabila.storyappdicoding.data.response.StoryResponse
import com.nabila.storyappdicoding.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

@ExperimentalCoroutinesApi
class StoryListViewModelTest {

    // Rule agar test berjalan dalam main thread dan coroutine
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var viewModel: StoryListViewModel

    private val dummyStories = DataDummy.generateDummyStories()
    private val token = "dummy_token"

    @Before
    fun setUp() {
        viewModel = StoryListViewModel(userRepository)
    }

    @Test
    fun `when getStories Should Return PagingData Successfully`() = runTest {
        `when`(userRepository.getStories(token, 1, 20)).thenReturn(StoryResponse(dummyStories, false, "Stories fetched successfully")) // Perbaikan di sini

        viewModel.getStories(token)
        val result = viewModel.storyPagingData.getOrAwaitValue()
        assertNotNull(result)
    }

    @Test
    fun `when getStories Return Empty Data`() = runTest {
        `when`(userRepository.getStories(token, 1, 20)).thenReturn(StoryResponse(emptyList(), false, "Stories fetched successfully")) // Perbaikan di sini

        viewModel.getStories(token)
        val result = viewModel.storyPagingData.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryListAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(result)

        assertEquals(0, differ.snapshot().size)
    }

    @Test
    fun `when logout Should Call Repository Logout`() = runTest {
        // Arrange: Tidak ada yang perlu dikembalikan

        // Act: Panggil fungsi logout di viewModel
        viewModel.logout()

        // Assert: Verifikasi jika fungsi logout di repository dipanggil
        verify(userRepository, times(1)).logout()
    }
}

private val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
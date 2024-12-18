package com.nabila.storyappdicoding.ui.story

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.*
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import com.nabila.storyappdicoding.DataDummy
import com.nabila.storyappdicoding.MainDispatcherRule
import com.nabila.storyappdicoding.data.model.Story
import com.nabila.storyappdicoding.data.repository.UserRepository
import com.nabila.storyappdicoding.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        // Arrange: Siapkan PagingData dengan dummyStories
        val data = PagingData.from(dummyStories)
        val liveDataPaging = MutableLiveData<PagingData<Story>>()
        liveDataPaging.value = data

        // Mock repository agar mengembalikan data
        `when`(userRepository.getStories(token, 1, 20)).thenReturn(null) // Mock API return paging

        // Act: Panggil method viewModel
        viewModel.getStories(token)

        // Assert: Pastikan LiveData berisi data yang diharapkan
        val result = viewModel.storyPagingData.getOrAwaitValue()
        assertNotNull(result)
    }

    @Test
    fun `when getStories Return Empty Data`() = runTest {
        // Arrange: Data kosong
        val emptyData = PagingData.from(emptyList<Story>())
        val liveDataPaging = MutableLiveData<PagingData<Story>>()
        liveDataPaging.value = emptyData

        `when`(userRepository.getStories(token, 1, 20)).thenReturn(null)

        // Act: Panggil method
        viewModel.getStories(token)

        // Assert: Data kosong
        val result = viewModel.storyPagingData.getOrAwaitValue()
        assertEquals(0, result.size)
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

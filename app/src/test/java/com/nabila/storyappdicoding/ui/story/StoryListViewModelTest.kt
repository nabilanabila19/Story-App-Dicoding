package com.nabila.storyappdicoding.ui.story

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import com.nabila.storyappdicoding.data.model.Story
import com.nabila.storyappdicoding.data.repository.UserRepository
import com.nabila.storyappdicoding.utils.MainDispatcherRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import com.nabila.storyappdicoding.utils.LiveDataTestUtil.getOrAwaitValue
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var userRepository: UserRepository
    private lateinit var storyListViewModel: StoryListViewModel
    private val dummyToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLXZOb25oc0xobU9pR1EiLCJpYXQiOjE2ODc5NTYwMTl9.flEMaQ7zfa_fMJSjIM-WeGohG_Zyk9bZMr_kAmj5K9k"


    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        storyListViewModel = StoryListViewModel(userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when Get Story Should Not Null and Return Data`() = runTest {
        val dummyStories = listOf(
            Story(
                id = "1",
                photoUrl = "photoUrl1",
                createdAt = "createdAt1",
                name = "name1",
                description = "description1",
                lon = 1.0,
                lat = 1.0
            ),
            Story(
                id = "2",
                photoUrl = "photoUrl2",
                createdAt = "createdAt2",
                name = "name2",
                description = "description2",
                lon = 2.0,
                lat = 2.0
            )
        )
        val data: PagingData<Story> = PagingData.from(dummyStories)
        val expectedStory = flowOf(data)
        Mockito.`when`(userRepository.getStories(dummyToken)).thenReturn(expectedStory)

        storyListViewModel.getStories(dummyToken)
        val actualStory: PagingData<Story> = storyListViewModel.storyPagingData.getOrAwaitValue()

        assertNotNull(actualStory)
        assertEquals(dummyStories.size, actualStory.toString().length) // Ganti dengan assertion yang sesuai
        assertEquals(dummyStories[0].id, actualStory) // Ganti dengan assertion yang sesuai
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val data: PagingData<Story> = PagingData.from(emptyList())
        val expectedStory = flowOf(data)
        Mockito.`when`(userRepository.getStories(dummyToken)).thenReturn(expectedStory)

        storyListViewModel.getStories(dummyToken)
        val actualStory: PagingData<Story> = storyListViewModel.storyPagingData.getOrAwaitValue()

        assertEquals(0, actualStory.toString().length) // Ganti dengan assertion yang sesuai
    }
}
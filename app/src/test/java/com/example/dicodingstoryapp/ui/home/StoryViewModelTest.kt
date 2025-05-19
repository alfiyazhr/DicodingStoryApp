package com.example.dicodingstoryapp.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.dicodingstoryapp.DataDummy
import com.example.dicodingstoryapp.LiveDataTestUtil
import com.example.dicodingstoryapp.MainDispatcherRule
import com.example.dicodingstoryapp.data.model.Story
import com.example.dicodingstoryapp.data.repository.StoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
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
class StoryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var storyViewModel: StoryViewModel

    @Before
    fun setUp() {
        storyViewModel = StoryViewModel(storyRepository)
    }

    @Test
    fun `when Get Stories Should Not Null and Return Success`() = runTest {
        val dummyStories = DataDummy.generateDummyStories()
        val data: PagingData<Story> = PagingData.from(dummyStories)
        val expectedStories = flow {
            emit(data)
        }

        Mockito.`when`(storyRepository.getStoriesWithPaging("token")).thenReturn(expectedStories)

        storyViewModel.getStoriesWithPaging("token")
        val actualStories = storyViewModel.pagingDataFlow.asLiveData()
        val actualStoriesData = LiveDataTestUtil.getOrAwaitValue(actualStories)

        val differ = AsyncPagingDataDiffer(
            diffCallback = DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStoriesData ?: PagingData.from(emptyList()))

        // Test Case 1: Memastikan data tidak null
        assertNotNull(differ.snapshot())

        // Test Case 2: Memastikan jumlah data sesuai yang diharapkan (11 item, karena loop 0..10)
        assertEquals(dummyStories.size, differ.snapshot().size)
        assertEquals(11, differ.snapshot().size)

        // Test Case 3: Memastikan data pertama yang dikembalikan sesuai
        val expectedFirstStory = Story(
            id = "story-0",
            name = "Story 0",
            description = "Description 0",
            photoUrl = "https://example.com/photo0.jpg",
            createdAt = "2024-12-20T00:00:00Z"
        )
        assertEquals(expectedFirstStory.id, differ.snapshot()[0]?.id)
        assertEquals(expectedFirstStory.name, differ.snapshot()[0]?.name)
        assertEquals(expectedFirstStory.description, differ.snapshot()[0]?.description)
        assertEquals(expectedFirstStory.photoUrl, differ.snapshot()[0]?.photoUrl)
        assertEquals(expectedFirstStory.createdAt, differ.snapshot()[0]?.createdAt)
    }

    @Test
    fun `when Get Stories Empty Should Return No Data`() = runTest {
        val data: PagingData<Story> = PagingData.from(emptyList())
        val expectedStories = flow {
            emit(data)
        }

        Mockito.`when`(storyRepository.getStoriesWithPaging("token")).thenReturn(expectedStories)

        storyViewModel.getStoriesWithPaging("token")
        val actualStories = storyViewModel.pagingDataFlow.asLiveData()
        val actualStoriesData = LiveDataTestUtil.getOrAwaitValue(actualStories)

        val differ = AsyncPagingDataDiffer(
            diffCallback = DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStoriesData ?: PagingData.from(emptyList()))

        // Test Case 4: Memastikan jumlah data yang dikembalikan nol
        assertEquals(0, differ.snapshot().size)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }
        }

        private val noopListUpdateCallback = object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {}
            override fun onRemoved(position: Int, count: Int) {}
            override fun onMoved(fromPosition: Int, toPosition: Int) {}
            override fun onChanged(position: Int, count: Int, payload: Any?) {}
        }
    }
}

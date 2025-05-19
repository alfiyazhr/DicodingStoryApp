package com.example.dicodingstoryapp.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.dicodingstoryapp.api.ApiService
import com.example.dicodingstoryapp.data.model.Story
import retrofit2.HttpException
import java.io.IOException

class StoryPagingSource(
    private val apiService: ApiService,
    private val token: String
) : PagingSource<Int, Story>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        val position = params.key ?: INITIAL_PAGE_INDEX

        return try {
            // Validate token
            if (token.isEmpty()) {
                throw IOException("Token is empty")
            }

            val authToken = "Bearer $token".replace("Bearer Bearer ", "Bearer ")
            Log.d("StoryPagingSource", "Loading page: $position with token: $authToken")

            val response = apiService.getAllStories(
                token = authToken,
                page = position,
                size = params.loadSize,
                location = 1
            )

            val stories = response.listStory ?: emptyList()
            Log.d("StoryPagingSource", "Loaded ${stories.size} stories")

            LoadResult.Page(
                data = stories,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (stories.isEmpty()) null else position + 1
            )
        } catch (e: IOException) {
            Log.e("StoryPagingSource", "Network error: ${e.message}")
            LoadResult.Error(e)
        } catch (e: HttpException) {
            Log.e("StoryPagingSource", "HTTP ${e.code()}: ${e.message()}")
            LoadResult.Error(e)
        } catch (e: Exception) {
            Log.e("StoryPagingSource", "Error loading stories: ${e.message}")
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}

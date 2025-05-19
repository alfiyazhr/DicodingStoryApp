package com.example.dicodingstoryapp.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.dicodingstoryapp.api.ApiClient
import com.example.dicodingstoryapp.api.ApiService
import com.example.dicodingstoryapp.data.model.LoginResponse
import com.example.dicodingstoryapp.data.model.RegisterResponse
import com.example.dicodingstoryapp.data.model.Story
import com.example.dicodingstoryapp.data.model.StoryResponse
import com.example.dicodingstoryapp.data.paging.StoryPagingSource
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class StoryRepository {
    private val apiService: ApiService = ApiClient.apiService

    companion object {
        private const val TAG = "StoryRepository"
        private const val NETWORK_PAGE_SIZE = 20
    }

    suspend fun register(name: String, email: String, password: String): Response<RegisterResponse> {
        return apiService.register(name, email, password)
    }

    suspend fun login(email: String, password: String): Response<LoginResponse> {
        return apiService.login(email, password)
    }

    suspend fun addStory(token: String, photo: MultipartBody.Part, description: RequestBody, lat: RequestBody?, lon: RequestBody?): Response<StoryResponse> {
        return apiService.addStory("Bearer $token", photo, description, lat, lon)
    }

    suspend fun getStoryDetail(token: String, id: String): Response<StoryResponse> {
        return apiService.getStoryDetail("Bearer $token", id)
    }

    suspend fun getStoriesWithLocation(token: String): Response<StoryResponse> {
        Log.d(TAG, "Making API call with token: $token")
        return apiService.getStoriesWithLocation(token)
    }

    fun getStoriesWithPaging(token: String): Flow<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false,
                initialLoadSize = NETWORK_PAGE_SIZE
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiService, "Bearer $token")
            }
        ).flow
    }
}

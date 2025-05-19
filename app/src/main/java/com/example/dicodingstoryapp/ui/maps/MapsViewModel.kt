package com.example.dicodingstoryapp.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicodingstoryapp.data.model.StoryResponse
import com.example.dicodingstoryapp.data.repository.StoryRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import android.util.Log

class MapsViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _storiesWithLocation = MutableLiveData<StoryResponse?>()
    val storiesWithLocation: LiveData<StoryResponse?> get() = _storiesWithLocation

    companion object {
        private const val TAG = "MapsViewModel"
    }

    fun fetchStoriesWithLocation(token: String) {
        viewModelScope.launch {
            try {
                val response = storyRepository.getStoriesWithLocation(token)
                if (response.isSuccessful) {
                    _storiesWithLocation.postValue(response.body())
                } else {
                    _storiesWithLocation.postValue(null)
                    Log.e(TAG, "Error fetching stories: ${response.errorBody()?.string()}")
                }
            } catch (exception: IOException) {
                _storiesWithLocation.postValue(null)
                Log.e(TAG, "Error fetching stories: ", exception)
            } catch (exception: HttpException) {
                _storiesWithLocation.postValue(null)
                Log.e(TAG, "Error fetching stories: ", exception)
            }
        }
    }
}

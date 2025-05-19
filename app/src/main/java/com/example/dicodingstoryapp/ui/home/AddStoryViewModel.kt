package com.example.dicodingstoryapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicodingstoryapp.data.model.StoryResponse
import com.example.dicodingstoryapp.data.repository.StoryRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.IOException

class AddStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _uploadResult = MutableLiveData<StoryResponse?>()
    val uploadResult: LiveData<StoryResponse?> = _uploadResult

    private val _uploadError = MutableLiveData<Throwable?>()
    val uploadError: LiveData<Throwable?> = _uploadError

    fun uploadStory(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody? = null,
        lon: RequestBody? = null
    ) {
        viewModelScope.launch {
            try {
                val response = storyRepository.addStory(token, file, description, lat, lon)
                if (response.isSuccessful) {
                    _uploadResult.postValue(response.body())
                } else {
                    _uploadError.postValue(HttpException(response))
                }
            } catch (exception: IOException) {
                _uploadError.postValue(exception)
            } catch (exception: HttpException) {
                _uploadError.postValue(exception)
            }
        }
    }
}

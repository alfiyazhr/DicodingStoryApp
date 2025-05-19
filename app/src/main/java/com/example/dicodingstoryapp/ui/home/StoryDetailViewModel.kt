package com.example.dicodingstoryapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicodingstoryapp.data.model.StoryResponse
import com.example.dicodingstoryapp.data.repository.StoryRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class StoryDetailViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _storyDetail = MutableLiveData<StoryResponse?>()
    val storyDetail: LiveData<StoryResponse?> = _storyDetail

    private val _detailError = MutableLiveData<Throwable?>()
    val detailError: LiveData<Throwable?> = _detailError

    fun fetchStoryDetail(token: String, id: String) {
        viewModelScope.launch {
            try {
                val response = storyRepository.getStoryDetail(token, id)
                if (response.isSuccessful) {
                    _storyDetail.postValue(response.body())
                } else {
                    _detailError.postValue(HttpException(response))
                }
            } catch (exception: IOException) {
                _detailError.postValue(exception)
            } catch (exception: HttpException) {
                _detailError.postValue(exception)
            }
        }
    }
}

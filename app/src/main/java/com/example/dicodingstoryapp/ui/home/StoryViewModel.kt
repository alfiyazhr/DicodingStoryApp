package com.example.dicodingstoryapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.dicodingstoryapp.data.model.Story
import com.example.dicodingstoryapp.data.repository.StoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _pagingDataFlow = MutableStateFlow<PagingData<Story>?>(null)
    val pagingDataFlow: StateFlow<PagingData<Story>?> = _pagingDataFlow.asStateFlow()

    fun getStoriesWithPaging(token: String) {
        viewModelScope.launch {
            try {
                storyRepository.getStoriesWithPaging(token)
                    .cachedIn(viewModelScope)
                    .collectLatest { pagingData ->
                        _pagingDataFlow.value = pagingData
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refresh(token: String) {
        getStoriesWithPaging(token)
    }
}

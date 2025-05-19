package com.example.dicodingstoryapp.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicodingstoryapp.data.model.RegisterResponse
import com.example.dicodingstoryapp.data.repository.StoryRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class RegisterViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _registerResult = MutableLiveData<RegisterResponse?>()
    val registerResult: LiveData<RegisterResponse?> = _registerResult

    private val _registerError = MutableLiveData<String>()
    val registerError: LiveData<String> = _registerError

    fun registerUser(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {

                if (password.length < 8) {
                    _registerError.postValue("Password must be at least 8 characters")
                    return@launch
                }

                Log.d("RegisterViewModel", "Registering user with name: $name, email: $email, password: $password")
                val response = storyRepository.register(name, email, password)

                if (response.isSuccessful && response.body() != null) {
                    Log.d("RegisterViewModel", "Register successful: ${response.body()}")
                    _registerResult.postValue(response.body())
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("RegisterViewModel", "Register failed: ${response.code()} $errorBody")
                    if (response.code() == 400 && errorBody?.contains("Email is already taken") == true) {
                        _registerError.postValue("Email is already taken. Please use a different email.")
                    } else {
                        _registerError.postValue(errorBody ?: "Registration failed")
                    }
                }
            } catch (e: IOException) {
                Log.e("RegisterViewModel", "Register failed: IOException ${e.message}")
                _registerError.postValue("Network error: ${e.message}")
            } catch (e: HttpException) {
                Log.e("RegisterViewModel", "Register failed: HttpException ${e.message()}")
                _registerError.postValue("Server error: ${e.message()}")
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Register failed: ${e.message}")
                _registerError.postValue("Error: ${e.message}")
            }
        }
    }
}

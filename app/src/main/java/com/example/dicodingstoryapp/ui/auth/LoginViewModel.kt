package com.example.dicodingstoryapp.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicodingstoryapp.data.model.LoginResponse
import com.example.dicodingstoryapp.data.repository.StoryRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResponse?>()
    val loginResult: LiveData<LoginResponse?> = _loginResult

    private val _loginError = MutableLiveData<Throwable?>()
    val loginError: LiveData<Throwable?> = _loginError

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "Logging in user with email: $email")
                val response = storyRepository.login(email, password)
                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        Log.d("LoginViewModel", "Login successful: Token: ${loginResponse.loginResult.token}")
                        _loginResult.postValue(loginResponse)
                    }
                } else {
                    Log.e("LoginViewModel", "Login failed: ${response.code()} ${response.message()}")
                    _loginError.postValue(HttpException(response))
                }
            } catch (exception: IOException) {
                Log.e("LoginViewModel", "Login failed: IOException ${exception.message}")
                _loginError.postValue(exception)
            } catch (exception: HttpException) {
                Log.e("LoginViewModel", "Login failed: HttpException ${exception.message()}")
                _loginError.postValue(exception)
            }
        }
    }
}

package com.example.dicodingstoryapp.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.dicodingstoryapp.data.model.LoginResponse
import com.example.dicodingstoryapp.data.repository.StoryRepository
import com.example.dicodingstoryapp.databinding.ActivityLoginBinding
import com.example.dicodingstoryapp.ui.home.HomeActivity
import com.example.dicodingstoryapp.utils.SessionManager
import com.example.dicodingstoryapp.R

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private var isPasswordVisible = false

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(StoryRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupListeners()
        startImageAnimation(binding.imageLogin)

        loginViewModel.loginResult.observe(this) { response ->
            showProgressBar(false)
            response?.let {
                handleLoginSuccess(it)
            }
        }

        loginViewModel.loginError.observe(this) { error ->
            showProgressBar(false)
            error?.let {
                handleLoginFailure(it)
            }
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {

            val email = binding.edLoginEmail.text?.toString()?.trim() ?: ""
            val password = binding.edLoginPassword.text?.toString()?.trim() ?: ""

            if (isValidInput(email, password)) {
                showProgressBar(true)
                loginViewModel.loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill out all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnShowPassword.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun isValidInput(email: String, password: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.isNotEmpty()
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible

        if (isPasswordVisible) {
            binding.edLoginPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.btnShowPassword.setImageResource(R.drawable.ic_eye_open)
        } else {
            binding.edLoginPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.btnShowPassword.setImageResource(R.drawable.ic_close)
        }

        binding.edLoginPassword.text?.let {
            binding.edLoginPassword.setSelection(it.length)
        }
    }

    private fun handleLoginSuccess(response: LoginResponse) {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_token", response.loginResult.token)
        editor.apply()

        sessionManager.saveAuthToken(response.loginResult.token)
        sessionManager.saveUserName(response.loginResult.name)

        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun handleLoginFailure(error: Throwable?) {
        Toast.makeText(this, "Login failed: ${error?.message}", Toast.LENGTH_SHORT).show()
    }

    private fun showProgressBar(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun startImageAnimation(imageView: ImageView) {
        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        val slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left)

        slideIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                imageView.startAnimation(slideOut)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        slideOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                imageView.startAnimation(slideIn)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        imageView.startAnimation(slideIn)
    }
}

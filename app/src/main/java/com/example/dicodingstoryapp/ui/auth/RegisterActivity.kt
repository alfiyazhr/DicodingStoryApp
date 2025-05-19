package com.example.dicodingstoryapp.ui.auth

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
import com.example.dicodingstoryapp.R
import com.example.dicodingstoryapp.data.repository.StoryRepository
import com.example.dicodingstoryapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var isPasswordVisible = false

    private val registerViewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory(StoryRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
        startImageAnimation(binding.imageSignup)
    }

    private fun setupObservers() {
        registerViewModel.registerResult.observe(this) { response ->
            showProgressBar(false)
            if (response != null) {
                handleRegisterSuccess()
            }
        }

        registerViewModel.registerError.observe(this) { errorMessage ->
            showProgressBar(false)
            handleRegisterFailure(errorMessage)
        }
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString().trim()
            val email = binding.edRegisterEmail.text.toString().trim()
            val password = binding.edRegisterPassword.text.toString().trim()

            when {
                name.isEmpty() -> {
                    binding.edRegisterName.error = "Name is required"
                    return@setOnClickListener
                }
                email.isEmpty() -> {
                    binding.edRegisterEmail.error = "Email is required"
                    return@setOnClickListener
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    binding.edRegisterEmail.error = "Invalid email format"
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    binding.edRegisterPassword.error = "Password is required"
                    return@setOnClickListener
                }
                else -> {
                    showProgressBar(true)
                    registerViewModel.registerUser(name, email, password)
                }
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnShowPassword.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        binding.edRegisterPassword.transformationMethod = if (isPasswordVisible) {
            binding.btnShowPassword.setImageResource(R.drawable.ic_eye_open)
            HideReturnsTransformationMethod.getInstance()
        } else {
            binding.btnShowPassword.setImageResource(R.drawable.ic_close)
            PasswordTransformationMethod.getInstance()
        }
        binding.edRegisterPassword.setSelection(binding.edRegisterPassword.text?.length ?: 0)
    }

    private fun handleRegisterSuccess() {
        Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun handleRegisterFailure(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
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

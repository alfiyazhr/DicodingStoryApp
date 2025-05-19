package com.example.dicodingstoryapp.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dicodingstoryapp.R
import com.example.dicodingstoryapp.data.repository.StoryRepository
import com.example.dicodingstoryapp.databinding.ActivityAddStoryBinding
import com.example.dicodingstoryapp.utils.SessionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var getResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var selectedImageUri: Uri? = null

    private val addStoryViewModel: AddStoryViewModel by viewModels {
        AddStoryViewModelFactory(StoryRepository())
    }

    private var currentLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.add_story_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sessionManager = SessionManager(this)

        setupListeners()
        registerActivityResult()
        registerPermissionResult()
        setupObservers()

        checkAndRequestLocationPermission()
    }

    private fun setupObservers() {
        addStoryViewModel.uploadResult.observe(this) { response ->
            showProgressBar(false)
            if (response != null) {
                handleUploadSuccess()
            }
        }

        addStoryViewModel.uploadError.observe(this) { error ->
            showProgressBar(false)
            handleUploadFailure(error)
        }
    }

    private fun checkAndRequestLocationPermission() {
        if (!hasLocationPermission()) {
            showLocationPermissionDialog()
        } else {
            getLastLocation()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun showLocationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Required")
            .setMessage("This app needs access to location to add location information to your story. Would you like to grant location permission?")
            .setPositiveButton("Grant Permission") { dialog, _ ->
                dialog.dismiss()
                requestLocationPermission()
            }
            .setNegativeButton("Not Now") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "Story will be uploaded without location information",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun setupListeners() {
        binding.btnAddPhoto.setOnClickListener {
            if (hasPermissions()) {
                openGallery()
            } else {
                requestPermissions()
            }
        }

        binding.btnUploadStory.setOnClickListener {
            val description = binding.edAddDescription.text.toString().trim()
            if (description.isNotEmpty() && selectedImageUri != null) {
                val file = uriToFile(selectedImageUri!!)
                uploadStory(file, description)
            } else {
                Toast.makeText(this, "Please provide a description and select a photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getResultLauncher.launch(intent)
    }

    private fun registerActivityResult() {
        getResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                binding.ivPreviewPhoto.setImageURI(selectedImageUri)
            }
        }
    }

    private fun registerPermissionResult() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (locationGranted) {
                getLastLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location permission denied. Story will be uploaded without location.",
                    Toast.LENGTH_LONG
                ).show()
            }

            if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && permissions[Manifest.permission.READ_MEDIA_IMAGES] == true)) {
                openGallery()
            }
        }
    }

    private fun getLastLocation() {
        if (hasLocationPermission()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = location
                    Toast.makeText(
                        this,
                        "Location acquired successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to get location. Story will be uploaded without location information.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun hasPermissions(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        }
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun uriToFile(uri: Uri): File {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
        val filePath = cursor?.getString(columnIndex!!)
        cursor?.close()
        return File(filePath!!)
    }

        private fun uploadStory(file: File, description: String) {
        showProgressBar(true)
        val descriptionRequestBody = RequestBody.create(MediaType.parse("text/plain"), description)
        val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file)
        val photo = MultipartBody.Part.createFormData("photo", file.name, requestFile)

        val latitudeRequestBody = currentLocation?.latitude?.let {
            RequestBody.create(MediaType.parse("text/plain"), it.toString())
        }
        val longitudeRequestBody = currentLocation?.longitude?.let {
            RequestBody.create(MediaType.parse("text/plain"), it.toString())
        }

        val token = sessionManager.fetchAuthToken()

        if (token != null) {
            addStoryViewModel.uploadStory(
                token,
                photo,
                descriptionRequestBody,
                latitudeRequestBody,
                longitudeRequestBody
            )
        } else {
            showProgressBar(false)
            Toast.makeText(this, "Failed to get auth token", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleUploadSuccess() {
        Toast.makeText(this, "Story uploaded successfully!", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun handleUploadFailure(error: Throwable?) {
        Toast.makeText(this, "Failed to upload story: ${error?.message}", Toast.LENGTH_SHORT).show()
    }

    private fun showProgressBar(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}

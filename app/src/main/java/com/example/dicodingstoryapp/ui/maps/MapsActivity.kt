package com.example.dicodingstoryapp.ui.maps

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModelProvider
import com.example.dicodingstoryapp.R
import com.example.dicodingstoryapp.data.model.StoryResponse
import com.example.dicodingstoryapp.data.repository.StoryRepository
import com.example.dicodingstoryapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

@Suppress("DEPRECATION")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val boundsBuilder = LatLngBounds.Builder()
    private lateinit var viewModel: MapsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val storyRepository = StoryRepository()
        viewModel = ViewModelProvider(this, MapsViewModelFactory(storyRepository))[MapsViewModel::class.java]

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel.storiesWithLocation.observe(this) { response ->
            Log.d(TAG, "Response received: $response")
            response?.let {
                if (it.listStory.isNullOrEmpty()) {
                    Toast.makeText(this, "No stories with location found", Toast.LENGTH_SHORT).show()
                    return@let
                }
                Log.d(TAG, "Stories count: ${it.listStory.size}")
                loadMarkersOnMap(it)
            } ?: run {
                Toast.makeText(this, "Failed to load stories", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_style -> {
                showStyleMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showStyleMenu() {
        val styleMenu = PopupMenu(this, findViewById(R.id.action_style))
        styleMenu.menuInflater.inflate(R.menu.map_style_options, styleMenu.menu)
        styleMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.light_style -> {
                    setMapStyle(R.raw.light_style)
                    true
                }
                R.id.dark_style -> {
                    setMapStyle(R.raw.dark_style)
                    true
                }
                R.id.coquette_style -> {
                    setMapStyle(R.raw.coquette_style)
                    true
                }
                else -> false
            }
        }
        styleMenu.show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isIndoorLevelPickerEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        setupMapInteractions()
        getMyLocation()
        setMapStyle(R.raw.light_style)

        fetchStoriesWithLocation()
    }

    private fun setupMapInteractions() {
        mMap.setOnMapLongClickListener { latLng ->
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("New Marker")
                    .snippet("Lat: ${latLng.latitude} Long: ${latLng.longitude}")
                    .icon(vectorToBitmap(R.drawable.ic_android, Color.parseColor("#3DDC84")))
            )
        }

        mMap.setOnPoiClickListener { pointOfInterest ->
            mMap.addMarker(
                MarkerOptions()
                    .position(pointOfInterest.latLng)
                    .title(pointOfInterest.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
            )?.showInfoWindow()
        }
    }

    private fun vectorToBitmap(@DrawableRes id: Int, @ColorInt color: Int): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
            ?: return BitmapDescriptorFactory.defaultMarker()

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, color)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setMapStyle(styleResourceId: Int) {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, styleResourceId))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
                Toast.makeText(this, "Failed to load map style", Toast.LENGTH_SHORT).show()
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
            Toast.makeText(this, "Map style resource not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchStoriesWithLocation() {
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("user_token", "") ?: ""
        if (token.isNotEmpty()) {
            val bearerToken = "Bearer $token"
            viewModel.fetchStoriesWithLocation(bearerToken)
        } else {
            Toast.makeText(this, "No valid token found", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadMarkersOnMap(storyResponse: StoryResponse) {
        mMap.clear() // Clear existing markers
        var hasValidLocations = false

        storyResponse.listStory?.forEach { story ->
            story.lat?.let { lat ->
                story.lon?.let { lon ->
                    try {
                        val latitude = lat.toDouble()
                        val longitude = lon.toDouble()
                        val location = LatLng(latitude, longitude)

                        mMap.addMarker(
                            MarkerOptions()
                                .position(location)
                                .title(story.name)
                                .snippet(story.description)
                        )
                        boundsBuilder.include(location)
                        hasValidLocations = true

                        Log.d(TAG, "Added marker for story: ${story.name} at $latitude, $longitude")
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "Error converting coordinates for story: ${story.name}", e)
                    }
                }
            }
        }

        if (hasValidLocations) {
            try {
                val bounds: LatLngBounds = boundsBuilder.build()
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        resources.displayMetrics.widthPixels,
                        resources.displayMetrics.heightPixels,
                        300
                    )
                )
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Error setting camera bounds", e)

                val defaultLocation = LatLng(-6.8957643, 107.6338462)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 5f))
            }
        } else {
            Toast.makeText(this, "No valid locations found in stories", Toast.LENGTH_SHORT).show()

            val defaultLocation = LatLng(-6.8957643, 107.6338462)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 5f))
        }
    }

    companion object {
        private const val TAG = "MapsActivity"
    }
}

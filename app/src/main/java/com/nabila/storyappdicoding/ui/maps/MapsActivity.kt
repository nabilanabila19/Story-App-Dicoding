package com.nabila.storyappdicoding.ui.maps

import android.content.ContentValues.TAG
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.asLiveData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.nabila.storyappdicoding.R
import com.nabila.storyappdicoding.databinding.ActivityMapsBinding
import com.nabila.storyappdicoding.di.Injection
import com.nabila.storyappdicoding.utils.Result.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val viewModel: MapsViewModel by viewModels {
        MapsViewModelFactory(Injection.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

        viewModel.storiesWithLocation.observe(this) { result ->
            if (result is Success) {
                val latLngBoundsBuilder = LatLngBounds.Builder()
                result.data.forEach { story ->
                    val lat = story.lat ?: 0.0
                    val lon = story.lon ?: 0.0
                    val latLng = LatLng(lat, lon)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(story.name)
                            .snippet(story.description)
                    )
                    latLngBoundsBuilder.include(latLng)
                }
                val latLngBounds = latLngBoundsBuilder.build()
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 100)
                mMap.animateCamera(cameraUpdate)
            }
        }

        val userRepository = Injection.provideRepository(this)
        userRepository.getSession().asLiveData().observe(this) { user ->
            if (user.isLogin) {
                viewModel.getStoriesWithLocation("Bearer ${user.token}")
            }
        }
    }
}
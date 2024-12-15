package com.nabila.storyappdicoding.ui.maps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.asLiveData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

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
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 100) // padding 100px
                mMap.animateCamera(cameraUpdate) // Animasikan pergerakan kamera
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
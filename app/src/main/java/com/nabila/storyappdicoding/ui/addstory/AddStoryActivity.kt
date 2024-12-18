package com.nabila.storyappdicoding.ui.addstory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.nabila.storyappdicoding.R
import com.nabila.storyappdicoding.data.remote.ApiService
import com.nabila.storyappdicoding.data.repository.UserRepository
import com.nabila.storyappdicoding.data.response.FileUploadResponse
import com.nabila.storyappdicoding.databinding.ActivityAddStoryBinding
import com.nabila.storyappdicoding.di.Injection
import com.nabila.storyappdicoding.ui.story.StoryListActivity
import com.nabila.storyappdicoding.utils.getImageUri
import com.nabila.storyappdicoding.utils.reduceFileImage
import com.nabila.storyappdicoding.utils.uriToFile
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private val viewModel: AddStoryViewModel by viewModels()
    private val userRepository: UserRepository by lazy {
        Injection.provideRepository(this)
    }
    private val apiService: ApiService by lazy {
        userRepository.apiService
    }
    private var latitude: Double? = null
    private var longitude: Double? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.uploadButton.setOnClickListener { uploadImage() }

        showImage()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.locationCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getMyLastLocation()
                binding.locationTextView.visibility = View.VISIBLE
            } else {
                latitude = null
                longitude = null
                binding.locationTextView.visibility = View.GONE
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            viewModel.currentImageUri = null
        }
    }

    private fun startCamera() {
        val imageUri = getImageUri(this)
        viewModel.currentImageUri = imageUri
        launcherIntentCamera.launch(imageUri)
    }

    private fun showImage() {
        viewModel.currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun uploadImage() {
        viewModel.currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")
            val description = binding.descriptionEditText.text.toString()
            showLoading(true)

            val requestBody = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )
            lifecycleScope.launch {
                try {
                    val successResponse =
                        apiService.uploadImage(multipartBody, requestBody, latitude, longitude)
                    showToast(successResponse.message)
                    showLoading(false)

                    val intent = Intent(this@AddStoryActivity, StoryListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, FileUploadResponse::class.java)
                    showToast(errorResponse.message)
                    showLoading(false)
                }
            }

        } ?: showToast(getString(R.string.empty_image_warning))
    }

    private fun getMyLastLocation() {
        // Pastikan Anda memiliki izin lokasi
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    binding.locationTextView.text = "Lokasi: $latitude, $longitude"
                } else {
                    Toast.makeText(
                        this@AddStoryActivity,
                        "Lokasi tidak ditemukan. Pastikan GPS aktif.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            // Minta izin lokasi jika belum diberikan
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Izin lokasi diberikan, dapatkan lokasi
                    getMyLastLocation()
                }

                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Izin lokasi diberikan, dapatkan lokasi
                    getMyLastLocation()
                }

                else -> {
                    // Izin lokasi ditolak
                    // Anda dapat menampilkan pesan atau menonaktifkan _checkbox_ lokasi
                    binding.locationCheckBox.isChecked = false
                }
            }
        }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
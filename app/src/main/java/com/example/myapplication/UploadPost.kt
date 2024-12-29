package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityUploadPostBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class UploadPost : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityUploadPostBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedLocation: LatLng? = null
    private var selectedImageUri: Uri? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set up the activity result launcher for picking images
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                binding.imageViewPhoto.setImageURI(selectedImageUri)
            }
        }

        val captureImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = (result.data?.extras?.get("data") as? Bitmap)
                imageBitmap?.let {
                    binding.imageViewPhoto.setImageBitmap(it)
                }
            }
        }

        // Set up the map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.buttonSelectImage.setOnClickListener {
            // Launch the image picker
            pickImage.launch("image/*")
        }
        binding.buttonCaptureImage.setOnClickListener {
            // Capture image using device's camera
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            captureImage.launch(captureIntent)
        }

        binding.buttonSavePost.setOnClickListener {
            savePost()
        }

        // Request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getDeviceLocation()
        }
        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Enable MyLocation layer if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }

        // Set a marker click listener
        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            selectedLocation = latLng
            // Move the camera to the selected location
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    private fun getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    mMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
                }
            }
        }
    }

    private fun savePost() {
        val price = binding.editTextPrice.text.toString() // Update to reflect the new price field
        val description = binding.editTextDescription.text.toString()
        val phoneNumber = binding.editTextPhone.text.toString()

        if (selectedLocation != null && price.isNotEmpty() && description.isNotEmpty() && selectedImageUri != null && phoneNumber.isNotEmpty()) {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                val postId = db.child("users").child(userId).child("posts").push().key!!
                val storageRef = storage.reference.child("post_images/$postId.jpg")

                storageRef.putFile(selectedImageUri!!)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            // Get the real address from the selected location
                            val address = selectedLocation?.let {
                                getAddress(this, it.latitude, it.longitude)
                            } ?: "Unknown Location"

                            val post = PostData(
                                price = price,
                                description = description,
                                place = address,
                                photoDownloadUrl = uri.toString(),
                                contactNumber = phoneNumber,
                                latitude = selectedLocation?.latitude,
                                longitude = selectedLocation?.longitude
                            )

                            db.child("users").child(userId).child("posts").child(postId)
                                .setValue(post)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Post saved successfully", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Failed to save post", Toast.LENGTH_SHORT).show()
                                }

                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAddress(context: UploadPost, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context)
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                listOfNotNull(
                    address.getAddressLine(0),
                    address.locality,
                    address.adminArea,
                    address.countryName
                ).joinToString(", ")
            } else {
                "Unknown Location"
            }
        } catch (e: Exception) {
            "Error fetching address: ${e.message ?: "Unknown error"}"
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getDeviceLocation()
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Camera permission granted, proceed with capturing images
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION_REQUEST_CODE = 2
    }
}

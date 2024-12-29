package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityPostDetailBinding
import com.squareup.picasso.Picasso

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the post data from the intent
        val price = intent.getStringExtra("price")
        val description = intent.getStringExtra("description")
        val place = intent.getStringExtra("place")
        val contactNumber = intent.getStringExtra("contactNumber")
        val photoUrl = intent.getStringExtra("photoUrl")

        // Set the data to the views
        binding.textViewPrice.text = price
        binding.textViewDescription.text = description
        binding.textViewPlace.text = place
        binding.textViewContactNumber.text = contactNumber
        Picasso.get().load(photoUrl).into(binding.imageViewPhoto)
    }
}

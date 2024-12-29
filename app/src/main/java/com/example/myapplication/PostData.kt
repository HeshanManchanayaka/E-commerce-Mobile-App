package com.example.myapplication

data class PostData(
    val price: String? = null,
    val place: String? = null,
    val description: String? = null,
    val photoDownloadUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val contactNumber: String? = null)

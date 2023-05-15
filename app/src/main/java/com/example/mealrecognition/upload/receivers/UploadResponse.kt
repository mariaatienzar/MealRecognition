package com.example.mealrecognition.upload.receivers

data class UploadResponse(
    val error: Boolean,
    val message: String,
    val image: String?
)

package com.example.mealrecognition.upload.receivers

data class ConfirmationResponse (
    val recognition_results: List<RecognitionResults2>,
    val source: String
)
data class RecognitionResults2(
    val id: Int,
    val name: String,
    val prob: Float,


)

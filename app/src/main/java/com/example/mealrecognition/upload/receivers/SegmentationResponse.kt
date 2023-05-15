package com.example.mealrecognition.upload.receivers

data class SegmentationResponse (
    val foodFamily: List<Family>,
    val foodType: FoodType,
    val imageId: Int,
    val occasion: String,
    val segmentation_results: List<Segmentation>
)

data class Family (
    val id: Int,
    val name: String
)

data class Segmentation (
    val food_item_position: Int,
    val description: String,
    val recognition_results: List<RecognitionResults>
)

data class RecognitionResults(
    val foodFamily: List<Family>,
    val id: Int,
    val name: String,
    val prob: Float,
    val subclasses: List<Subclasses>
)

data class Subclasses (
    val foodFamily: List<Family>,
    val id: Int,
    val name: String,
    val prob: Float
)

data class FoodType (
    var id: Int,
    var name: String
)
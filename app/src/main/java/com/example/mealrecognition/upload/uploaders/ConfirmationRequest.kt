package com.example.mealrecognition.upload.uploaders

data class ConfirmationRequest(val imageId: String, val confirmedClass: ArrayList<Int>, val source: ArrayList<String>, val food_item_position: ArrayList<Int>)

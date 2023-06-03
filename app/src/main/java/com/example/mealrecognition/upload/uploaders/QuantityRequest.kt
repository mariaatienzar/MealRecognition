package com.example.mealrecognition.upload.uploaders

import org.json.JSONObject

data class QuantityRequest(val imageId: String, val quantity: Map<String, Float>)
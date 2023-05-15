package com.example.mealrecognition

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject

fun parseSegmentation(data: JSONObject): ArrayList<ArrayList<String>> {
    val imageId = data.getString("imageId")
    val occasion = data.getString("occasion")
    val foodType = data.getJSONObject("foodType")
    val foodFamily = data.getJSONArray("foodFamily")
    val segmentation_results = data.getJSONArray("segmentation_results")

    val allRecognizedFood = ArrayList<ArrayList<String>>()
    for (i in 0 until segmentation_results.length()) {
        val recognizedFoodList = ArrayList<String>()
        val probFoodList = ArrayList<Double>()
        val item = segmentation_results.getJSONObject(i)
        val recognition_results = item.getJSONArray("recognition_results")
        for (j in 0 until recognition_results.length()) {
            val result = recognition_results.getJSONObject(j)
            val recognizedFood = result.getString("name")
            recognizedFoodList.add(recognizedFood)

            val probability = result.getDouble("prob")
            probFoodList.add(probability)
        }
        allRecognizedFood.add(recognizedFoodList)
    }

    return allRecognizedFood
}
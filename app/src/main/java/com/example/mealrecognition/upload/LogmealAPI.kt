package com.example.mealrecognition.upload

import com.example.mealrecognition.upload.receivers.ConfirmationResponse
import com.example.mealrecognition.upload.receivers.NutrientResponse
import com.example.mealrecognition.upload.receivers.QuantityResponse
import com.example.mealrecognition.upload.receivers.SegmentationResponse
import com.example.mealrecognition.upload.uploaders.ConfirmationRequest
import com.example.mealrecognition.upload.uploaders.NutritionRequest
import com.example.mealrecognition.upload.uploaders.QuantityRequest
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.http.Headers

interface LogmealAPI {

    @Multipart
    @Headers("Authorization: Bearer 2a21c8792e08485bab24d448866830dc98033f07")
    @POST("image/segmentation/complete")
    fun dishesDetection( //imagesegmentation
        @Part files: MultipartBody.Part
    ): Call<SegmentationResponse>


    @Headers("Authorization:Bearer 2a21c8792e08485bab24d448866830dc98033f07")
    @POST("image/confirm/dish")
    fun confirmationDish(
        @Body data: ConfirmationRequest
    ): Call<ConfirmationResponse>


    @Headers("Authorization: Bearer 2a21c8792e08485bab24d448866830dc98033f07",
        "Content-Type: application/json")
    @POST("nutrition/recipe/nutritionalInfo")
    fun nutrientInformation(
        @Body data: NutritionRequest
    ): Call<NutrientResponse>


    @Headers("Authorization:Bearer 2a21c8792e08485bab24d448866830dc98033f07",
        "Content-Type: application/json")
    @POST("nutrition/confirm/quantity")
    fun quantityDish(
        @Body data: QuantityRequest
    ): Call<QuantityResponse>



    companion object{
        operator fun invoke() : LogmealAPI {
            return Retrofit.Builder()
                .baseUrl("https://api.logmeal.es/v2/")  //cambiar la IP en servidor
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LogmealAPI::class.java)
        }
    }
}
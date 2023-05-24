package com.example.mealrecognition.upload

import com.example.mealrecognition.upload.receivers.ConfirmationResponse
import com.example.mealrecognition.upload.receivers.NutrientResponse
import com.example.mealrecognition.upload.receivers.QuantityResponse
import com.example.mealrecognition.upload.receivers.SegmentationResponse
import com.example.mealrecognition.upload.uploaders.ConfirmationRequest
import com.example.mealrecognition.upload.uploaders.NutritionRequest
import com.example.mealrecognition.upload.uploaders.NutritionRequest2
import com.example.mealrecognition.upload.uploaders.QuantityRequest
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.http.Headers

interface LogmealAPI {

    @Multipart
    @Headers("Authorization: Bearer a9bba9d61dbde110ffcf9a49f8cdebf2d2988baf")
    @POST("image/segmentation/complete")
    fun dishesDetection( //imagesegmentation
        @Part files: MultipartBody.Part
    ): Call<SegmentationResponse>


    @Headers("Authorization:Bearer a9bba9d61dbde110ffcf9a49f8cdebf2d2988baf",
    "Content-Type: application/json")
    @POST("image/confirm/dish")
    fun confirmationDish(
        @Body data: ConfirmationRequest
    ): Call<ConfirmationResponse>


    @Headers("Authorization: Bearer a9bba9d61dbde110ffcf9a49f8cdebf2d2988baf",
        "Content-Type: application/json")
    @POST("nutrition/recipe/nutritionalInfo")
    fun nutrientInformation(
        @Body data: NutritionRequest
    ): Call<NutrientResponse>


    @Headers("Authorization: Bearer a9bba9d61dbde110ffcf9a49f8cdebf2d2988baf",
        "Content-Type: application/json")
    @POST("nutrition/recipe/nutritionalInfo")
    fun nutrientInformation2(
        @Body data: NutritionRequest2
    ): Call<NutrientResponse>



    @Headers("Authorization:Bearer a9bba9d61dbde110ffcf9a49f8cdebf2d2988baf",
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
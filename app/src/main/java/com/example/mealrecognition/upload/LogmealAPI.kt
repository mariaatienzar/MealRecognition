package com.example.mealrecognition.upload

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.mealrecognition.upload.receivers.*
import com.example.mealrecognition.upload.uploaders.*
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.http.Headers
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

interface LogmealAPI {


    @Multipart
    @POST("image/segmentation/complete")
    fun dishesDetection(
        @Header("Authorization") authorization: String,

        @Part files: MultipartBody.Part
    ): Call<SegmentationResponse>





    @Headers("Content-Type: application/json")
    @POST("image/confirm/dish")
    fun confirmationDish(
        @Header("Authorization") authorization: String,

        @Body data: ConfirmationRequest
    ): Call<ConfirmationResponse>


    @Headers(
        "Content-Type: application/json")
    @POST("nutrition/recipe/nutritionalInfo")
    fun nutrientInformation(
        @Header("Authorization") authorization: String,

        @Body data: NutritionRequest
    ): Call<NutrientResponse>




    @Headers(
        "Content-Type: application/json")
    @POST("nutrition/confirm/quantity")
    fun quantityDish(
        @Header("Authorization") authorization: String,

        @Body data: QuantityRequest
    ): Call<QuantityResponse>



    @Headers("Authorization:Bearer 4dc568682f3424c08ed15b31c4dc1d2f67286f3d",
        "Content-Type: application/json")
    @POST("users/signUp")
    fun tokenUser(
        @Body data: UserRequest
    ): Call<UserResponse>

    fun userToken(username: String){

    }



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
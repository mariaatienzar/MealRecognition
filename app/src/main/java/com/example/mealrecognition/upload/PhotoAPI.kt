package com.example.mealrecognition.upload

import com.example.mealrecognition.upload.receivers.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part



interface PhotoAPI {
    @Multipart
    @POST("Api.php?apicall=photo")
    fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("patient_id") desc: RequestBody
    ): Call<UploadResponse>

    companion object{
        operator fun invoke() : PhotoAPI {
            return Retrofit.Builder()
                .baseUrl("http://138.4.10.37:8443/ImageUploader/")  //cambiar la IP en servidor 138.4.10.37:8443//192.168.1.81
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PhotoAPI::class.java)
        }
    }
}



package com.example.mealrecognition

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mealrecognition.databinding.ActivityCalculationBinding
import com.example.mealrecognition.upload.NutrientAPI
import com.example.mealrecognition.upload.getFileName
import com.example.mealrecognition.upload.receivers.UploadResponse
import com.example.mealrecognition.upload.snackbar
import com.example.mealrecognition.upload.uploaders.UploadFileBody
import com.example.mealrecognition.upload.uploaders.UploadRequest3
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class Calculation : AppCompatActivity() {

    private val RETRASO_MS: Long = 2000 // 3 segundos

    private lateinit var binding: ActivityCalculationBinding
    private lateinit var textViewHC: TextView
    private lateinit var textCal: TextView
    private lateinit var textCorrec: EditText
    private lateinit var progress_bar: ProgressBar
    private lateinit var imageView: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculation)

        binding = ActivityCalculationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        textViewHC = binding.detailedQuantityCarb
        textCal = binding.detailedIRC
        textCorrec = binding.detailedC1
        imageView = binding.homereturn

        //val spinner = binding.detailedQuantityDropdown
        progress_bar = binding.progressBar
        val button = binding.buttonCalc
        val imageUri = intent.getParcelableExtra<Uri>("image")
        val carbsApp = intent.getStringExtra("carbs")
        textViewHC.text = carbsApp +" g"
        // val carbsUser = intent.getStringExtra("carb")
        //Log.e("TAG", " $carbsUser")

        val sharedPref = getSharedPreferences("ch_estimation", Context.MODE_PRIVATE)
        val patient_estimation = sharedPref.getString("ch", null)



        textCal.text = patient_estimation + " g"

        imageView.setOnClickListener{
            startActivity(Intent(this,HomeActivity::class.java))
        }





        button.setOnClickListener{
            val patient_correction = textCorrec.text.toString()
            textCorrec.setBackgroundColor(Color.parseColor("#84C170"))

            if (imageUri != null && patient_correction.isNotEmpty()) {
                uploadImage(imageUri,patient_correction)
            }
            else {

                Toast.makeText(this, "Debe introducir un valor de CH", Toast.LENGTH_LONG).show()

            }


        }

    }

    private fun uploadImage(uriFile : Uri, patient_correction: String) {
        Log.e("TAG", patient_correction)
        val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm")
        val now: LocalDateTime = LocalDateTime.now()
        val date: String = dtf.format(now).toString()

        val parcelFileDescriptor = contentResolver.openFileDescriptor(uriFile, "r", null)
            ?: return
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val valuesActivity = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS).toString() + "/nutrients_info_%s.json".format(date)
        val filenut = File(valuesActivity)
        if (!filenut.exists()) {
            filenut.createNewFile()
        }


        val fileWriter = FileWriter(filenut)
        val bufferedWriter = BufferedWriter(fileWriter)

        val jsonObject = intent.getStringExtra("json_upd")
        val json = JSONObject(jsonObject)
        bufferedWriter.write(json.toString())
        Thread.sleep(10)
        bufferedWriter.close()


        val file = File(this.cacheDir, this.contentResolver!!.getFileName(uriFile))

        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        progress_bar.progress = 0
        val body = UploadRequest3(file, "image", this)
        val prefs = this.getSharedPreferences("id_pref", MODE_PRIVATE)
        val patient_id = prefs?.getInt("id", 0)
        val sharedPref = getSharedPreferences("ch_estimation", Context.MODE_PRIVATE)
        val patient_estimation = sharedPref.getString("ch", null)


        val sharedPrefOccasion = getSharedPreferences("meal_occasion", Context.MODE_PRIVATE)
        val meal_occasion = sharedPrefOccasion?.getString("meal", null)
        if (meal_occasion != null) {
            Log.e("TAG", meal_occasion)
        }

        val sharedPrefIRC = getSharedPreferences("irc_patient", Context.MODE_PRIVATE)
        val irc = sharedPrefIRC?.getString("irc",null)
        if (irc != null) {
            Log.e("TAG", irc)
        }

        val sharedPrefFactor = getSharedPreferences("factor_correction", Context.MODE_PRIVATE)
        val correc = sharedPrefFactor?.getString("factor", null)
        if (correc != null) {
            Log.e("TAG", correc)
        }

        val sharedDetec = getSharedPreferences("detec_incorrecta", Context.MODE_PRIVATE)
        val incorrect_detection = sharedDetec.getString("incorrecto", null)
        Log.e("TAG", incorrect_detection.toString())



        val body_nut = UploadFileBody(filenut, "json")



        NutrientAPI().uploadImage(

            MultipartBody.Part.createFormData("image", file.name, body),
            MultipartBody.Part.createFormData("json", filenut.name, body_nut),
            RequestBody.create(MediaType.parse("multipart/form-data"), patient_id.toString()),
            RequestBody.create(MediaType.parse("multipart/form-data"), irc.toString()),
            RequestBody.create(MediaType.parse("multipart/form-data"), correc.toString()),
            RequestBody.create(MediaType.parse("multipart/form-data"), patient_estimation.toString()),
            RequestBody.create(MediaType.parse("multipart/form-data"), patient_correction.toString()),
            RequestBody.create(MediaType.parse("multipart/form-data"), meal_occasion.toString()),
            RequestBody.create(MediaType.parse("multipart/form-data"), incorrect_detection.toString())


            ).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                response.body()?.let {

                    progress_bar.progress = 100
                    inputStream.close()
                    outputStream.close()

                }

                if (response.isSuccessful) {
                    imageView.snackbar("Datos registrados correctamente")
                    val confirmationResponse = response.body()
                    println(confirmationResponse)
                    val handler = Handler()
                    handler.postDelayed({
                        // Crear el Intent para iniciar la actividad deseada
                        startActivity(Intent(this@Calculation,HomeActivity::class.java))

                        // Finalizar la actividad actual si es necesario
                        finish()
                    }, RETRASO_MS)


                } else {
                    println("Error en la respuesta: ${response.code()}")
                }
            }


            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                imageView.snackbar("No ha sido posible subir la información, imagen guardada en galería")
                progress_bar.progress = 0
            }

        })
    }
    fun onProgressUpdate(percentage: Int) {
        progress_bar.progress = percentage
    }






}
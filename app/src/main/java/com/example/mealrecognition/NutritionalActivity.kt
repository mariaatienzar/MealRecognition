package com.example.mealrecognition

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.*
import com.example.mealrecognition.upload.LogmealAPI
import com.example.mealrecognition.upload.receivers.NutrientResponse
import com.example.mealrecognition.upload.snackbar
import com.example.mealrecognition.upload.uploaders.NutritionRequest
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class NutritionalActivity : AppCompatActivity() {
    private lateinit var buttonView: LinearLayout
    private lateinit var buttonText: Button
    private lateinit var progress_bar: ProgressBar
    private lateinit var binding: NutritionalActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutritional)

        //val progressBar = binding.progress_bar

        //val buttonView = binding.buttonView


        val dishesConfirmed = ArrayList<JSONObject>() //lista para almacenar las confirmaciones que llegan

       // val json_confirm = intent.getStringExtra("json_confirm")
//        val jsonObject = JSONObject(json_confirm)
      //  Log.e("TAG", jsonObject.toString())

        val unit_confirm = intent.getStringExtra("json_confirm")
        val unitObject = JSONObject(unit_confirm)
        Log.e("TAG", unitObject.toString())

        /* dishesConfirmed.add(jsonObject) //los platos confirmados que llegan al pulsar el boton confirmar estan dentro de un bucle,
        //no se si se deberia hacer una lista para almacenar todo despues de confirmDishes

        for (i in 0 until dishesConfirmed.size){
            val foodConfirmed = parseConfirm(dishesConfirmed[i])
            var textview = TextView(this)
            textview.text = "Item %s confirmado".format(i + 1)
            buttonView.addView(textview)

            val radioGroup = RadioGroup(this)
            radioGroup.id = i
            val button = RadioButton(this)
            //button.id = foodConfirmed.size + i + 1
            button.text = foodConfirmed[i].toString() //Aqui ya meteria el nombre de la comida (hacer again el button group por si hay mas de una comida)

            radioGroup.addView(button)
            button.setBackgroundResource(R.drawable.selection_button)

            }

        }

    fun parseConfirm(data: JSONObject): String { //mirar que lo que devuelve creo que es un string con el nombre
        val recognition_results = data.getJSONArray("recognition_results")
        val source = data.getString("source")
        val allConfirm = ArrayList<String>()
        for (i in 0 until recognition_results.length()) {
            val result = recognition_results.getJSONObject(i)
            val nameFood = result.getString("name")
            allConfirm.add(nameFood)
        }
        return allConfirm.toString()
    }

        */
    }

    private fun obtainNutrients(imageId: Int) {
        val request = NutritionRequest(imageId.toString())

        progress_bar.progress = 0
        LogmealAPI().nutrientInformation(request).enqueue(object : Callback<NutrientResponse> {
            override fun onResponse(
                call: Call<NutrientResponse>,
                response: Response<NutrientResponse>
            ) {
                response.body()?.let {
                    val foodName = JSONArray(it.foodName)
                    val hasNutritionalInfo = it.hasNutritionalInfo
                    val ids = JSONArray(it.ids)
                    val imageId = it.imageId
                    val nutritional_info = JSONObject(Gson().toJson(it.nutritional_info))
                    val nutritional_info_per_item =
                        JSONArray(Gson().toJson(it.nutritional_info_per_item))
                    val serving_size = it.serving_size

                    val response_data = JSONObject()
                    response_data.put("foodName", foodName)
                    response_data.put("hasNutritionalInfo", hasNutritionalInfo)
                    response_data.put("ids", ids)
                    response_data.put("imageId", imageId)
                    response_data.put("nutritional_info", nutritional_info)
                    response_data.put("nutritional_info_per_itemval", nutritional_info_per_item)
                    response_data.put("serving_size", serving_size)

                    val valuesActivity = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    ).toString() + "/nutrientes.json"
                    val file = File(valuesActivity)
                    if (!file.exists()) {
                        file.createNewFile()
                    }

                    val fileWriter = FileWriter(file)
                    val bufferedWriter = BufferedWriter(fileWriter)
                    bufferedWriter.write(response_data.toString())
                    Thread.sleep(10)
                    bufferedWriter.close()

                }

            }

            override fun onFailure(call: Call<NutrientResponse>, t: Throwable) {

                progress_bar.progress = 0
            }


        }
        )
    }
}

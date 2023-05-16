package com.example.mealrecognition

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mealrecognition.adapters.RecogAdapter
import com.example.mealrecognition.databinding.ActivityRecognitionBinding
import com.example.mealrecognition.upload.LogmealAPI
import com.example.mealrecognition.upload.receivers.ConfirmationResponse
import com.example.mealrecognition.upload.receivers.QuantityResponse
import com.example.mealrecognition.upload.snackbar
import com.example.mealrecognition.upload.uploaders.ConfirmationRequest
import com.example.mealrecognition.upload.uploaders.NutritionRequest
import com.example.mealrecognition.upload.uploaders.QuantityRequest
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.*
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter


class RecognitionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecognitionBinding
    private lateinit var progress_bar: ProgressBar
    private lateinit var imageView: ImageView
    lateinit var buttonsView: LinearLayout
    lateinit var confButton: Button
    var arrSegmentationResponse = ArrayList<ArrayList<String>>()
    var recogAdapter = RecogAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecognitionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buttonsView = binding.buttonsView
        confButton = binding.btnConf
        progress_bar = binding.progressBar
        imageView = binding.imageView


        val imageUri = intent.getParcelableExtra<Uri>("image")
        imageView.setImageURI(imageUri)
        val json = intent.getStringExtra("json")
        val jsonObject = JSONObject(json)

        val foodName = parseSegment(jsonObject)
        val lp = ActionBar.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(10, 10, 0, 0)
        val item = ArrayList<Int>()


        for (i in 0 until foodName.size) {
            val scrollView = ScrollView(this!!)
            var textview = TextView(this)
            textview.text = "Item %s reconocido".format(i + 1)
            buttonsView.addView(textview)
            buttonsView.addView(scrollView)
            val linearLayout = LinearLayout(this!!)
            linearLayout.orientation = LinearLayout.VERTICAL
            scrollView.addView(linearLayout)


            val radioGroup = RadioGroup(this)
            radioGroup.id = i
            for (j in 0 until foodName[i].size) {
                val button = RadioButton(this)
                button.id = foodName.size + j + 1
                button.layoutParams = lp
                button.text = foodName[0][j]
                radioGroup.addView(button)
                button.setBackgroundResource(R.drawable.selection_button)

            }

            buttonsView.addView(radioGroup)

        }
        confButton.setOnClickListener {
            val idDish = parseSegmentFoodId(jsonObject)
            val idImage = parseSegmentImageID(jsonObject)
            val radioLastItem = findViewById<RadioGroup>(foodName.size-1)
            val listItemPosition = ArrayList<Int>()
            for (i in 0 until foodName.size) {
                val radio = findViewById<RadioGroup>(i)
                val checked_button = radio.checkedRadioButtonId

                if (checked_button != -1) {
                    val conf = idDish[0][checked_button - (radioLastItem.id+2)]
                    item.add(conf)
                    //Log.e("TAG",radio.size.toString() + "/" + checked_button.toString()+ "/" + item.toString() )
                    listItemPosition.add(checked_button - (radioLastItem.id+2))
                    Log.e("TAG",listItemPosition.toString())
                }
            }
            confirmDish(idImage,item[0],"source", listItemPosition[0])
            Log.e("TAG",listItemPosition.toString() + listItemPosition.size + confirmDish(idImage,item[0],"source", listItemPosition[0]))

            //Log.e("TAG", item.toString() )
           /* val listConfirmation= ArrayList<Unit>()
             for (j in 0 until listItemPosition.size) {
                val objConfirmation = confirmDish(idImage,item[j],item, listItemPosition[j])
                listConfirmation.add(objConfirmation)
            }

            Log.e("TAG",listItemPosition.toString() + listItemPosition.size + listConfirmation)

            */
            //confirmDish(idImage,item[0],"logmeal", listItemPosition[0])

            //}
            //sendUnit(listConfirmation)





        }
    }




    fun parseSegment(data: JSONObject): ArrayList<ArrayList<String>> {
        val imageId = data.getString("imageId")
        val foodType = data.getJSONObject("foodType")
        val foodFamily = data.getJSONArray("foodFamily")
        val segmentation_results = data.getJSONArray("segmentation_results")

        val allRecognizedFood = ArrayList<ArrayList<String>>()
        for (i in 0 until segmentation_results.length()) {
            val recognizedFoodList = ArrayList<String>()
            val probFoodList = ArrayList<Double>()
            val idFoodList = ArrayList<Int>()
            val item = segmentation_results.getJSONObject(i)
            val recognition_results = item.getJSONArray("recognition_results")
            for (j in 0 until recognition_results.length()) {
                val result = recognition_results.getJSONObject(j)

                val recognizedFood = result.getString("name")
                recognizedFoodList.add(recognizedFood)

                val probability = result.getDouble("prob")
                probFoodList.add(probability)

                val idFood = result.getInt("id")
                idFoodList.add(idFood)
            }
            allRecognizedFood.add(recognizedFoodList)
        }

        return allRecognizedFood
    }


    fun parseSegmentFoodId(data: JSONObject): ArrayList<ArrayList<Int>> {
        val segmentation_results = data.getJSONArray("segmentation_results")
        val allIdFood = ArrayList<ArrayList<Int>>()
        for (i in 0 until segmentation_results.length()) {
            val idFoodList = ArrayList<Int>()
            val item = segmentation_results.getJSONObject(i)
            val recognition_results = item.getJSONArray("recognition_results")
            for (j in 0 until recognition_results.length()) {
                val result = recognition_results.getJSONObject(j)

                val idFood = result.getInt("id")
                idFoodList.add(idFood)
            }
            allIdFood.add(idFoodList)
        }

        return allIdFood
    }

    fun parseSegmentImageID(data: JSONObject): Int {
        val imageId = data.getString("imageId")
        return imageId.toInt()
    }

    private fun confirmDish(foodId: Int, confirmedClass: Int, source: String, foodItemPosition: Int) {
    //private fun confirmDish(foodId: Int, confirmedClass: List<Any>, source: List<Any>, foodItemPosition: List<Any>) {
        //val confirmedClass = ArrayList<Int>()
        //val source = ArrayList<String>()
        //val foodItemPosition = ArrayList<Int>()
        val request = ConfirmationRequest(foodId,confirmedClass,source, foodItemPosition)
        progress_bar.progress = 0
        LogmealAPI().confirmationDish(request).enqueue(object : Callback<ConfirmationResponse> {

            override fun onResponse(
                call: Call<ConfirmationResponse>,
                response: Response<ConfirmationResponse>
            ) {
                response.body()?.let {
                    progress_bar.progress = 100
                    val recognition_results = JSONArray(Gson().toJson(it.recognition_results))
                    val source = it.source
                    val responsedata = JSONObject()
                    responsedata.put("recognition_results", recognition_results)
                    responsedata.put("source", source)
                    sendConfirmation(responsedata)

                }
                if (response.isSuccessful) {
                    val confirmationResponse = response.body()
                    // respuesta exitosa
                    // confirmationResponse contiene la respuesta de la API
                    println(confirmationResponse)
                } else {
                    // respuesta de error
                    println("Error en la respuesta: ${response.code()}")
                }

                //Log.e("TAG",response.toString())




            }

            override fun onFailure(call: Call<ConfirmationResponse>, t: Throwable) {
                imageView.snackbar(t.message!!)
                progress_bar.progress = 0
            }

        })
    }





    private fun sendConfirmation (JObject: JSONObject){
        val intent = Intent(this@RecognitionActivity, NutritionalActivity::class.java)
        val jsonObject = JObject.toString()
        intent.putExtra("json_confirm", jsonObject)
        startActivity(intent)


    }

    private fun sendUnit (listJObject: ArrayList<Unit>){
        val intent = Intent(this@RecognitionActivity, NutritionalActivity::class.java)
        val listJsonObject = listJObject.toString()
        intent.putExtra("unit_confirm", listJsonObject)
        startActivity(intent)
        }

}






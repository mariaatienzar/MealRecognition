package com.example.mealrecognition

import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mealrecognition.databinding.ActivityRecognitionBinding
import com.example.mealrecognition.upload.LogmealAPI
import com.example.mealrecognition.upload.receivers.ConfirmationResponse
import com.example.mealrecognition.upload.receivers.NutrientResponse
import com.example.mealrecognition.upload.snackbar
import com.example.mealrecognition.upload.uploaders.ConfirmationRequest
import com.example.mealrecognition.upload.uploaders.NutritionRequest
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RecognitionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecognitionBinding
    private lateinit var progress_bar: ProgressBar
    private lateinit var imageView: TextView
    lateinit var buttonsView: LinearLayout
    lateinit var confButton: Button
    lateinit var textView: TextView
    private lateinit var imageUri: Uri
    private lateinit var button_scroll: HorizontalScrollView
    private var shouldResetItems = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecognitionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buttonsView = binding.buttonsView
        confButton = binding.btnConf
        progress_bar = binding.progressBar
        textView = binding.textView
        //button_scroll = binding.scrollbutton


        imageUri = intent.getParcelableExtra<Uri>("image")!!

        //imageView.setImageURI(imageUri)
        val json = intent.getStringExtra("json")
        val gramsCH = intent.getStringExtra("carb")
        val occasion = intent.getStringExtra("meal")
        Log.e("TAG","$occasion")

        val sharedPref = this.getSharedPreferences("ch_estimation", Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()
        editor?.putString("ch", gramsCH)
        editor?.apply()

        val sharedPrefOccasion = this.getSharedPreferences("meal_occasion", Context.MODE_PRIVATE)
        val editor1 = sharedPrefOccasion?.edit()
        editor1?.putString("meal", occasion)
        editor1?.apply()



        val jsonObject = JSONObject(json)

        val foodName = parseSegment(jsonObject)
        Log.e("TAG", "$jsonObject $foodName")
        val lp = ActionBar.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(10, 10, 0, 30)
        val item = ArrayList<Int>()
        val source = ArrayList<String>()

        val foodSize = foodName.size

        textView.setText("$foodSize alimentos reconocidos en su imagen. \n \n Selecciona de la lista aquellos que correspondan con su ingesta: ")


        for (i in 0 until foodName.size) {
            val scrollView = HorizontalScrollView(this!!)
            var textview = TextView(this)
            textview.text = "Alimento %s reconocido".format(i + 1)
            textview.setTypeface(textview.typeface, Typeface.BOLD);
            textview.textSize = 16F
            textview.setTextColor(Color.parseColor("#FF000000"))


            buttonsView.addView(textview)
            buttonsView.addView(scrollView)

            val linearLayout = LinearLayout(this!!)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            scrollView.addView(linearLayout)

            val radioGroup = RadioGroup(this)
            radioGroup.id = i
            radioGroup.orientation = RadioGroup.HORIZONTAL
            val scrollView2 = HorizontalScrollView(this)

            for (j in 0 until foodName[i].size) {
                val button = RadioButton(this)
                button.id = foodName.size + j + 1
                button.layoutParams = lp
                button.text = foodName[i][j]
                button.setTextColor(Color.parseColor("#FF009688"))
                radioGroup.addView(button)
                button.setBackgroundResource(R.drawable.selection_button)

            }
            scrollView2.addView(radioGroup)


            buttonsView.addView(scrollView2)

        }
        confButton.setOnClickListener {
            val idDish = parseSegmentFoodId(jsonObject)
            val idImage = parseSegmentImageID(jsonObject)
            val foodPosition = parseSegmentFoodPosition(jsonObject)
            val radioLastItem = findViewById<RadioGroup>(foodName.size - 1)
            val listFoodPosition = ArrayList<Int>()
            for (i in 0 until foodName.size) {
                val radio = findViewById<RadioGroup>(i)
                val checked_button = radio.checkedRadioButtonId

                if (checked_button != -1) {
                    val conf = idDish[i][checked_button - (radioLastItem.id + 2)]
                    item.add(conf)
                    source.add("logmeal")
                    listFoodPosition.add(foodPosition[i])

                }
            }

            val sizeItem = item.size

            val dialog = AlertDialog.Builder(this)

            dialog.setTitle("Aviso")
                .setMessage("Has seleccionado $sizeItem de $foodSize alimentos reconocidos. ¿Estás seguro de continuar?")
                .setPositiveButton("Si") { dialog, whichButton ->
                    confirmDish(idImage, item, source, listFoodPosition)
                    if (shouldResetItems) {
                        item.clear() // Reiniciar la lista de elementos seleccionados
                        listFoodPosition.clear()
                        source.clear()

                        shouldResetItems = false // Restablecer la bandera
                    }

                }
                .setNegativeButton("NO") { dialog, whichButton ->
                    if (shouldResetItems) {
                        item.clear() // Reiniciar la lista de elementos seleccionados
                        listFoodPosition.clear()
                        source.clear()

                        shouldResetItems = false // Restablecer la bandera
                    }
                    // DO YOUR STAFF
                    // dialog.close()
                }

            dialog.show()




        }
    }
    override fun onResume() {
        super.onResume()
        shouldResetItems = true
    }




    private fun obtainNutrients(imageId: Int) {
        val request = NutritionRequest(imageId.toString())
        val sharedPrefToken = getSharedPreferences("token_user", Context.MODE_PRIVATE)
        val token = "Bearer " + sharedPrefToken.getString("token", null)


        //progress_bar.progress = 0
        LogmealAPI().nutrientInformation(token,request).enqueue(object : Callback<NutrientResponse> {
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
                    val nutritional_info_per_item = JSONArray(Gson().toJson(it.nutritional_info_per_item))
                    val serving_size = it.serving_size

                    val response_data = JSONObject()
                    response_data.put("foodName", foodName)
                    response_data.put("hasNutritionalInfo", hasNutritionalInfo)
                    response_data.put("ids", ids)
                    response_data.put("imageId", imageId)
                    response_data.put("nutritional_info", nutritional_info)
                    response_data.put("nutritional_info_per_item", nutritional_info_per_item)
                    response_data.put("serving_size", serving_size)




                    if (imageUri != null) {
                        sendConfirmation(response_data, imageUri)
                    }



                    if (response.isSuccessful) {
                        val confirmationResponse = response.body()
                        println(confirmationResponse)

                    } else {
                        println("Error en la respuesta: ${response.code()}")
                    }


                }


            }

            override fun onFailure(call: Call<NutrientResponse>, t: Throwable) {

                //progress_bar.progress = 0
            }
        }
        )
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

    fun parseSegmentFoodPosition(data: JSONObject): ArrayList<Int> {
        val segmentation_results = data.getJSONArray("segmentation_results")

        val allItemPosition = ArrayList<Int>()
        for (i in 0 until segmentation_results.length()) {
            val item = segmentation_results.getJSONObject(i)
            val segment_position = item.getInt("food_item_position")
            allItemPosition.add(segment_position)
        }

        return allItemPosition
    }


    fun parseSegmentImageID(data: JSONObject): Int {
        val imageId = data.getString("imageId")
        return imageId.toInt()
    }


    private fun confirmDish(
        foodId: Int,
        confirmedClass: ArrayList<Int>,
        source: ArrayList<String>,
        food_item_position: ArrayList<Int>
    ) {
        val request =
            ConfirmationRequest(foodId.toString(), confirmedClass, source, food_item_position)
        progress_bar.progress = 0
        val sharedPrefToken = getSharedPreferences("token_user", Context.MODE_PRIVATE)
        val token = "Bearer " + sharedPrefToken.getString("token", null)
        Log.e("TAG", "$token")
        LogmealAPI.invoke()
        if (token != null) {
            LogmealAPI().confirmationDish(token, request).enqueue(object : Callback<ConfirmationResponse> {

                override fun onResponse(
                    call: Call<ConfirmationResponse>,
                    response: Response<ConfirmationResponse>
                ) {
                    response.body()?.let {
                        progress_bar.progress = 100
                        val recognition_results = JSONArray(Gson().toJson(it.recognition_results))
                        val responsedata = JSONObject()
                        responsedata.put("recognition_results", recognition_results)

                        obtainNutrients(foodId)

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


                }

                override fun onFailure(call: Call<ConfirmationResponse>, t: Throwable) {
                    imageView.snackbar(t.message!!)
                    progress_bar.progress = 0
                }

            })
        }
    }


    private fun sendConfirmation(JObjectConf: JSONObject, Image: Uri) {
        val intent = Intent(this@RecognitionActivity, NutritionalActivity::class.java)
        val jsonObjectConf = JObjectConf.toString()

        intent.putExtra("image", Image)
        intent.putExtra("json_confirm", jsonObjectConf)
        startActivity(intent)


    }

    private fun parsePositionItem(data : JSONObject): ArrayList<Int>{
        val nutritionalInfoPerItem = data.getJSONArray("nutritional_info_per_item")
        val allServingSize = ArrayList<Float>()
        val allPositionItem = ArrayList<Int>()
        val jsonObjectQuantity = JSONObject()
        for (i in 0 until nutritionalInfoPerItem.length()) {
            val result = nutritionalInfoPerItem.getJSONObject(i)
            val ss = result.getDouble("serving_size").toFloat()
            val fp = result.getInt("food_item_position")
            allServingSize.add(ss)
            allPositionItem.add(fp)

        }
        return allPositionItem
    }


}




package com.example.mealrecognition

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mealrecognition.databinding.ActivityNutritionalBinding
import com.example.mealrecognition.upload.LogmealAPI
import com.example.mealrecognition.upload.receivers.NutrientResponse
import com.example.mealrecognition.upload.receivers.QuantityResponse
import com.example.mealrecognition.upload.snackbar
import com.example.mealrecognition.upload.uploaders.NutritionRequest
import com.example.mealrecognition.upload.uploaders.NutritionRequest2
import com.example.mealrecognition.upload.uploaders.QuantityRequest
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import kotlin.math.roundToInt


class NutritionalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNutritionalBinding
    private lateinit var progress_bar : ProgressBar
    lateinit var image_view: ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNutritionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress_bar = binding.progressBar

        val buttonsView = binding.buttonsView
        val image_view = binding.imageView
        val buttonConfirmQuantity = binding.buttonConfirmQuantity

        val textCarb = binding.detailedCarbsValue
        val textCal = binding.detailedKcalValue
        val textFats = binding.detailedFatsValue
        val textProteins = binding.detailedProteinsValue


        val textUnitQuantity = binding.detailedQuantity


        val textNICal = binding.detailedNutritionCaloriesValue
        val textNICarb = binding.detailedNutritionCarbsValue
        val textNIProteins = binding.detailedNutritionProteinsValue
        val textNISugar = binding.detailedNutritionSugarsValue
        val textNIFiber = binding.detailedNutritionFiberValue
        val textNISaturatedFat = binding.detailedNutritionSValue
        val textNICholest = binding.detailedNutritionColestValue
        val textNIsodium = binding.detailedNutritionNaValue



        val json_confirm = intent.getStringExtra("json_confirm")
        val jsonObjectConfirm = JSONObject(json_confirm)
        val imageUri = intent.getParcelableExtra<Uri>("image")
        image_view.setImageURI(imageUri)


        val idImage = parseImageId(jsonObjectConfirm)

        val calories = ((parseCalories(jsonObjectConfirm)* 100).roundToInt().toFloat())/100
        val carboh = ((parseCarbohQuantity(jsonObjectConfirm)* 100).roundToInt().toFloat())/100
        val carbohUnit = parseCarbohUnit(jsonObjectConfirm)
        val proteins = ((parseProteinQuantity(jsonObjectConfirm)* 100).roundToInt().toFloat())/100
        val proteinsUnit = parseProteinUnit(jsonObjectConfirm)
        val fats = ((parseFatQuantity(jsonObjectConfirm)* 100).roundToInt().toFloat())/100
        val fatsUnit = parseFatUnit(jsonObjectConfirm)
        val sugar = parseSugarQuantity(jsonObjectConfirm)
        val fiber = parseFiberQuantity(jsonObjectConfirm)
        val satFats = parseSatFatQuantity(jsonObjectConfirm)
        val sodium = parseSodiumQuantity(jsonObjectConfirm)
        val cholest = parseCholestQuantity(jsonObjectConfirm)


        textCarb.text = "$carboh $carbohUnit"
        textCal.text = "$calories kcal"
        textProteins.text = "$proteins $proteinsUnit"
        textFats.text = "$fats $fatsUnit"


        textNICal.text = "$calories kcal"
        textNICarb.text = "$carboh g"
        textNIProteins.text = "$proteins g"
        textNISugar.text = "$sugar g"
        textNIFiber.text = "$fiber g"
        textNISaturatedFat.text = "$satFats g"
        textNICholest.text = "$cholest mg"
        textNIsodium.text = "$sodium mg"

        val totalServingSize = parseDishSize(jsonObjectConfirm)
        val itemsServingSize = parseServingItem(jsonObjectConfirm)
        val itemPosition = parsePositionItem(jsonObjectConfirm)
        Log.e("TAG", " ${parsePositionItem(jsonObjectConfirm)}")

        val jsonObjectQuantity = parseServingSizeItem(jsonObjectConfirm)

        //val listServingSize = ArrayList<Float>()
        val listServingSize = ArrayList<String>()
        val listPosition = ArrayList<Int>()

        val foodName = parseFoodName(jsonObjectConfirm)

        val jsonEdit = JSONObject()



        textUnitQuantity.text = "$totalServingSize g"

        for (i in 0 until foodName.length()) {
            var textviewFood = TextView(this)
            var textviewQuantity = AutoCompleteTextView(this)
            var textInputLayout = TextInputLayout(this)
            val rec = itemsServingSize[i]
            listServingSize.add(rec.toString())
            textviewFood.text = foodName[i].toString()
            textviewQuantity.setText(listServingSize[i])

            val scrollView = ScrollView(this!!)
            buttonsView.addView(textviewFood)
            buttonsView.addView(textviewQuantity)
            buttonsView.addView(scrollView)
            val linearLayout = LinearLayout(this!!)
            linearLayout.orientation = LinearLayout.VERTICAL
            scrollView.addView(linearLayout)


            val listEdit = ArrayList<Float>()

            val listJson = ArrayList<JSONObject>()
            listJson.add(jsonObjectQuantity)

            listPosition.add(i+1)

            buttonConfirmQuantity.setOnClickListener {
                confirmQuantity(idImage, jsonEdit)
                //obtainNutrients()

            }


            // Modificar textviewQuantity y guardar los valores en la lista
            textviewQuantity.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // No se necesita implementar
                }

                override fun onTextChanged(
                     s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int) {
                    // Validar el valor ingresado como Double
                    val input = s.toString()
                    if (input.isNotBlank()) {
                        try {
                            val value = input.toDouble()
                            listServingSize[i] = value.toString()
                        } catch (e: NumberFormatException) {
                            Toast.makeText(
                                this@NutritionalActivity,
                                "Introduce un valor numérico válido",
                                Toast.LENGTH_LONG
                            ).show()
                            listServingSize[i] = "" // Reiniciar el valor en la lista
                        }
                    }
                    for (j in 0 until foodName.length()){
                        val key = listPosition[j].toString()
                        val value = listServingSize[j].toFloat()
                        jsonEdit.put(key, value)

                    }
                    Log.e("TAG", " $jsonEdit $listServingSize $listPosition")


                }


                override fun afterTextChanged(s: Editable?) {
                    // No se necesita implementar
                }
            })
            buttonConfirmQuantity.setOnClickListener{
                Log.e("TAG", " $jsonEdit ")

                confirmQuantity(idImage, jsonEdit)
                if (imageUri != null) {
                    sendUpdate(jsonObjectConfirm,imageUri)
                }
                // sendConfirmation(calories,carboh)



            }

        }


    }

    private fun confirmQuantity(imageId: Int, jsonObjectQuantity: JSONObject){
        val request = QuantityRequest(imageId.toString(),jsonObjectQuantity)
        progress_bar.progress = 0
        LogmealAPI().quantityDish(request).enqueue(object : Callback<QuantityResponse> {

            override fun onResponse(
                call: Call<QuantityResponse>,
                response: Response<QuantityResponse>
            ) {
                response.body()?.let {
                    progress_bar.progress = 100

                    val result = it.result

                    val response_data = JSONObject()
                    response_data.put("result", result)


                    if (response.isSuccessful) {
                        val confirmationResponse = response.body()
                        // respuesta exitosa
                        // confirmationResponse contiene la respuesta de la API
                        println(confirmationResponse)

                    } else {
                        // respuesta de error
                        println("Error en la respuesta: ${response.code()}")
                    }
                    obtainNutrients(imageId)


                    val valuesActivity = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS).toString() + "/cantidad.json"
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

            override fun onFailure(call: Call<QuantityResponse>, t: Throwable) {
                image_view.snackbar(t.message!!)
                progress_bar.progress = 0
            }

        })
    }
    private fun obtainNutrients(imageId: Int) {
        val request = NutritionRequest(imageId.toString())

        //progress_bar.progress = 0
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

                     val imageUri = intent.getParcelableExtra<Uri>("image")

                    if (imageUri != null) {
                        sendUpdate(response_data, imageUri)
                    }

                    if (response.isSuccessful) {
                        val confirmationResponse = response.body()
                        println(confirmationResponse)

                    } else {
                        println("Error en la respuesta: ${response.code()}")
                    }


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

                //progress_bar.progress = 0
            }
        }
        )
    }

    fun parseImageId(data: JSONObject): Int { //mirar que lo que devuelve creo que es un string con el nombre
        val id= data.getInt("imageId")
        return id
    }

    private fun parseDishSize(data : JSONObject): Float {
        val serving_size = data.getDouble("serving_size").toFloat()
        return serving_size
    }


    private fun parseFoodName(data : JSONObject): JSONArray {
        val foodName = data.getJSONArray("foodName")
        return foodName
    }
    private fun parseidFood(data : JSONObject): JSONArray {
        val idsFood = data.getJSONArray("ids")
        return idsFood
    }

    private fun parseCalories(data : JSONObject): Double {
        val foodName = data.getJSONArray("foodName")
        val imageId = data.getString("imageId")
        val nutritionalInfo = data.getJSONObject("nutritional_info")
        val nutritionalInfoPerItem = data.getJSONArray("nutritional_info_per_item")
        val servingSize = data.getDouble("serving_size")

        val calories= nutritionalInfo.getDouble("calories")
        return calories


    }

    private fun parseItemNutritionalInfo(data : JSONObject): JSONArray {
         val nutritionalInfoPerItem = data.getJSONArray("nutritional_info_per_item")

        val allids = ArrayList<Int>()
        for (i in 0 until nutritionalInfoPerItem.length()) {
            val result = nutritionalInfoPerItem.getJSONObject(i)
            val ids = result.getInt("id")
            allids.add(ids)
            for (i in 0 until allids.size) {

            }
            val nutritionalInfo = result.getJSONObject("nutritional_info")
            val calories = nutritionalInfo.getDouble("calories")
            val totalNutrients = nutritionalInfo.getJSONObject("totalNutrients")
            val carbohItem = totalNutrients.getJSONObject("CHOCDF")


            for (i in 0 until nutritionalInfoPerItem.length()) {

            }

        }
        return nutritionalInfoPerItem
    }
    private fun parseServingSizeItem(data : JSONObject): JSONObject{
        val nutritionalInfoPerItem = data.getJSONArray("nutritional_info_per_item")
        val allServingSize = ArrayList<Float>()
        val allPositionItem = ArrayList<Int>()
        val jsonObjectQuantity = JSONObject()
        for (i in 0 until nutritionalInfoPerItem.length()) {
            val result = nutritionalInfoPerItem.getJSONObject(i)
            val ss = result.getDouble("serving_size").toFloat()
            val fp = result.getInt("foodItemPosition")
            allServingSize.add(ss)
            allPositionItem.add(fp)
            for (i in 0 until allServingSize.size){
                jsonObjectQuantity.put(allPositionItem[i].toString(),allServingSize[i])
            }

        }
        return jsonObjectQuantity
    }

    private fun parseServingItem(data : JSONObject): ArrayList<Float>{
        val nutritionalInfoPerItem = data.getJSONArray("nutritional_info_per_item")
        val allServingSize = ArrayList<Float>()
        val jsonObjectQuantity = JSONObject()
        for (i in 0 until nutritionalInfoPerItem.length()) {
            val result = nutritionalInfoPerItem.getJSONObject(i)
            val ss = result.getDouble("serving_size").toFloat()
             allServingSize.add(ss)

        }
        return allServingSize
    }
    private fun parsePositionItem(data : JSONObject): ArrayList<Int>{
        val nutritionalInfoPerItem = data.getJSONArray("nutritional_info_per_item")
        val allServingSize = ArrayList<Float>()
        val allPositionItem = ArrayList<Int>()
        val jsonObjectQuantity = JSONObject()
        for (i in 0 until nutritionalInfoPerItem.length()) {
            val result = nutritionalInfoPerItem.getJSONObject(i)
            val ss = result.getDouble("serving_size").toFloat()
            val fp = result.getInt("foodItemPosition")
            allServingSize.add(ss)
            allPositionItem.add(fp)

        }
        return allPositionItem
    }


    private fun parseCarbohQuantity(data : JSONObject):  Double {
        val nutritionalInfo = data.getJSONObject("nutritional_info")
        val totalNutrients= nutritionalInfo.getJSONObject("totalNutrients")
        val type = totalNutrients.getJSONObject("CHOCDF")
        val quantity = type.getDouble("quantity")
        val label = type.getString("label")
        val unit = type.getString("unit")

        return quantity

        // HAY QUE MIRAR val CHOCDF: Nutrient (CARBOHIDRATOS)


    }

    private fun parseFatQuantity(data : JSONObject):  Double {
        val nutritionalInfo = data.getJSONObject("nutritional_info")

        val totalNutrients= nutritionalInfo.getJSONObject("totalNutrients")
        val type = totalNutrients.getJSONObject("FAT")
        val quantity = type.getDouble("quantity")
        val label = type.getString("label")
        val unit = type.getString("unit")

        return quantity

        // HAY QUE MIRAR val CHOCDF: Nutrient (CARBOHIDRATOS)


    }
    private fun parseProteinQuantity(data : JSONObject):  Double {
        val nutritionalInfo = data.getJSONObject("nutritional_info")

        val totalNutrients= nutritionalInfo.getJSONObject("totalNutrients")
        val type = totalNutrients.getJSONObject("PROCNT")
        val quantity = type.getDouble("quantity")
        val label = type.getString("label")
        val unit = type.getString("unit")

        return quantity

    }
    private fun parseSugarQuantity(data : JSONObject):  Double {
        val nutritionalInfo = data.getJSONObject("nutritional_info")
        val totalNutrients = nutritionalInfo.getJSONObject("totalNutrients")
        val type = totalNutrients.getJSONObject("SUGAR")
        val quantity = type.getDouble("quantity")
        val label = type.getString("label")
        val unit = type.getString("unit")

        return quantity
    }
    private fun parseFiberQuantity(data : JSONObject):  Double {
        val nutritionalInfo = data.getJSONObject("nutritional_info")
        val totalNutrients = nutritionalInfo.getJSONObject("totalNutrients")
        val type = totalNutrients.getJSONObject("FIBTG")
        val quantity = type.getDouble("quantity")
        val label = type.getString("label")
        val unit = type.getString("unit")

        return quantity
    }
    private fun parseSatFatQuantity(data : JSONObject):  Double {
        val nutritionalInfo = data.getJSONObject("nutritional_info")
        val totalNutrients = nutritionalInfo.getJSONObject("totalNutrients")
        val type = totalNutrients.getJSONObject("FASAT")
        val quantity = type.getDouble("quantity")
        val label = type.getString("label")
        val unit = type.getString("unit")

        return quantity
    }
    private fun parseCholestQuantity(data : JSONObject):  Double {
        val nutritionalInfo = data.getJSONObject("nutritional_info")
        val totalNutrients = nutritionalInfo.getJSONObject("totalNutrients")
        val type = totalNutrients.getJSONObject("CHOLE")
        val quantity = type.getDouble("quantity")
        val label = type.getString("label")
        val unit = type.getString("unit")

        return quantity
    }
    private fun parseSodiumQuantity(data : JSONObject):  Double {
        val nutritionalInfo = data.getJSONObject("nutritional_info")
        val totalNutrients = nutritionalInfo.getJSONObject("totalNutrients")
        val type = totalNutrients.getJSONObject("NA")
        val quantity = type.getDouble("quantity")
        val label = type.getString("label")
        val unit = type.getString("unit")

        return quantity
    }
    private fun parseSodiumUnit(data : JSONObject):  String {
        val nutritionalInfo = data.getJSONObject("nutritional_info")
        val totalNutrients = nutritionalInfo.getJSONObject("totalNutrients")
        val type = totalNutrients.getJSONObject("NA")
        val quantity = type.getDouble("quantity")
        val label = type.getString("label")
        val unit = type.getString("unit")

        return unit
    }
    private fun parseCholestmUnit(data : JSONObject):  String {
        val nutritionalInfo = data.getJSONObject("nutritional_info")
        val totalNutrients = nutritionalInfo.getJSONObject("totalNutrients")
        val type = totalNutrients.getJSONObject("CHOLE")
        val quantity = type.getDouble("quantity")
        val label = type.getString("label")
        val unit = type.getString("unit")

        return unit
    }


    private fun parseCarbohUnit(data : JSONObject): String {
        val nutritionalInfo = data.getJSONObject("nutritional_info")

        val totalNutrients= nutritionalInfo.getJSONObject("totalNutrients")
        val type = totalNutrients.getJSONObject("CHOCDF")
        val quantity = type.getDouble("quantity")
        val label = type.getString("label")
        val unit = type.getString("unit")

        return unit
    }

    private fun parseProteinUnit(data : JSONObject): String {
        val nutritionalInfo = data.getJSONObject("nutritional_info")

        val totalNutrients= nutritionalInfo.getJSONObject("totalNutrients")
        val type = totalNutrients.getJSONObject("PROCNT")
        val quantity = type.getDouble("quantity")
        val label = type.getString("label")
        val unit = type.getString("unit")

        return unit
    }

    private fun parseFatUnit(data : JSONObject): String {
        val nutritionalInfo = data.getJSONObject("nutritional_info")

        val totalNutrients= nutritionalInfo.getJSONObject("totalNutrients")
        val type = totalNutrients.getJSONObject("FAT")
        val quantity = type.getDouble("quantity")
        val label = type.getString("label")
        val unit = type.getString("unit")

        return unit
    }

    private fun parseCarbohLabel(data : JSONObject): String {
        val nutritionalInfo = data.getJSONObject("nutritional_info")

        val totalNutrients= nutritionalInfo.getJSONObject("totalNutrients")
        val type = totalNutrients.getJSONObject("CHOCDF")
        val quantity = type.getDouble("quantity")
        val label = type.getString("label")
        val unit = type.getString("unit")

        return label

    }

    private fun sendUpdate(JObjectConf: JSONObject, Image: Uri) {
        val intent = Intent(this, NutritionalActivity2::class.java)
        val jsonObjectConf = JObjectConf.toString()
        intent.putExtra("photo", Image)
        intent.putExtra("json_updated", jsonObjectConf)
        startActivity(intent)


    }
    private fun sendConfirmation(Cal: Int, Carbs: Int) {
        val intent = Intent(this, Calculation::class.java)
        intent.putExtra("calories", Cal)
        intent.putExtra("carbs", Carbs)
        startActivity(intent)


    }





}

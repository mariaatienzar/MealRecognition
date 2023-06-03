package com.example.mealrecognition

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.mealrecognition.databinding.ActivityNutritional2Binding
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

class NutritionalActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityNutritional2Binding
    private lateinit var progress_bar : ProgressBar
    lateinit var image_view: ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutritional2)
        binding = ActivityNutritional2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val buttonsView = binding.buttonsView

        val buttonConfirmQuantity = binding.buttonConfirmQuantity


        val image_view = binding.imageView

        val json_confirm = intent.getStringExtra("json_updated")
        val jsonObjectConfirm = JSONObject(json_confirm)
        val imageUri = intent.getParcelableExtra<Uri>("photo")
        image_view.setImageURI(imageUri)


        val itemsServingSize = parseServingItem(jsonObjectConfirm)


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



        val idImage = parseImageId(jsonObjectConfirm)

        val calories = ((parseCalories(jsonObjectConfirm)* 100).roundToInt().toFloat())/100
        val carboh = ((parseCarbohQuantity(jsonObjectConfirm)* 100).roundToInt().toFloat())/100
        val carbohUnit = parseCarbohUnit(jsonObjectConfirm)
        val proteins = ((parseProteinQuantity(jsonObjectConfirm)* 100).roundToInt().toFloat())/100
        val proteinsUnit = parseProteinUnit(jsonObjectConfirm)
        val fats = ((parseFatQuantity(jsonObjectConfirm)* 100).roundToInt().toFloat())/100
        val fatsUnit = parseFatUnit(jsonObjectConfirm)
        val sugar = ((parseSugarQuantity(jsonObjectConfirm)* 100).roundToInt().toFloat())/100
        val fiber = ((parseFiberQuantity(jsonObjectConfirm)* 100).roundToInt().toFloat())/100
        val satFats = ((parseSatFatQuantity(jsonObjectConfirm)* 100).roundToInt().toFloat())/100
        val sodium = ((parseSodiumQuantity(jsonObjectConfirm)* 100).roundToInt().toFloat())/100
        val cholest = ((parseCholestQuantity(jsonObjectConfirm)* 100).roundToInt().toFloat())/100




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
        val itemPosition = parsePositionItem(jsonObjectConfirm)

        val jsonObjectQuantity = parseServingSizeItem(jsonObjectConfirm)

        val listServingSize = ArrayList<String>()
        val listPosition = ArrayList<Int>()

        val foodName = parseFoodName(jsonObjectConfirm)


        textUnitQuantity.text = "$totalServingSize g"

        for (i in 0 until foodName.length()) {
            var textviewFood = TextView(this)
            var textviewQuantity = TextView(this)
            var textInputLayout = TextInputLayout(this)
            val rec = itemsServingSize[i]
            listServingSize.add(rec.toString())
            textviewFood.text = foodName[i].toString()
            textviewFood.textSize = 18f
            textviewQuantity.text= listServingSize[i] + " g"
            textviewQuantity.textSize= 18f
            textviewQuantity.setTextColor(Color.parseColor("#84C170"))

            val scrollView = ScrollView(this!!)
            buttonsView.addView(textviewFood)
            buttonsView.addView(textviewQuantity)
            buttonsView.addView(scrollView)
            val linearLayout = LinearLayout(this!!)
            linearLayout.orientation = LinearLayout.VERTICAL
            scrollView.addView(linearLayout)


            val jsonEdit = JSONObject()

            val listJson = ArrayList<JSONObject>()
            listJson.add(jsonObjectQuantity)

            listPosition.add(i+1)
            for (j in 0 until listPosition.size) {
                val key = listPosition[j].toString()
                val value = listServingSize[j].toFloat()
                jsonEdit.put(key, value)
            }


        }

        buttonConfirmQuantity.setOnClickListener{
            if (imageUri != null) {
                sendConfirmation(calories,carboh,imageUri, jsonObjectConfirm)
            }

        }

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


    private fun parseServingSizeItem(data : JSONObject): JSONObject{
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
            val fp = result.getInt("food_item_position")
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

    private fun sendConfirmation(Cal: Float, Carbs: Float, Image: Uri, Json: JSONObject) {
        val intent = Intent(this, Calculation::class.java)
        val grams = intent.getStringExtra("chGrams")
        val json_upd = Json.toString()
        intent.putExtra("json_upd", json_upd)
        intent.putExtra("image", Image)
        intent.putExtra("calories", Cal.toString())
        intent.putExtra("carbs", Carbs.toString())
        intent.putExtra("carbsUs",grams)
        startActivity(intent)


    }







}
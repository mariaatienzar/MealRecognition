package com.example.mealrecognition

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.example.mealrecognition.databinding.ActivityCalculationBinding
import com.example.mealrecognition.databinding.ActivityCalculationBinding.inflate
import com.example.mealrecognition.databinding.ActivityNutritionalBinding
import org.json.JSONObject
import kotlin.math.roundToInt

class Calculation : AppCompatActivity() {
    private lateinit var binding: ActivityCalculationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculation)

        binding = ActivityCalculationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val textViewHC = binding.detailedQuantityCarb
        //val spinner = binding.detailedQuantityDropdown
        val button = binding.buttonCalc
        val insul = binding.detailedInsulin
        val textIRC = binding.detailedIRC
        val carbs = intent.getStringExtra("carbs")



        textViewHC.text = carbs


        button.setOnClickListener{


            val quantityList = arrayListOf<String>()
            val spinner = findViewById<AutoCompleteTextView>(R.id.detailed_quantity_dropdown)
            val adapter = ArrayAdapter(
                applicationContext, R.layout.spinner_default,
                quantityList
            )

            if (carbs != null) {
                quantityList.add(carbs)
            }
            spinner.setAdapter(adapter)

            var selectedText: String = ""


// Agregar el listener para capturar la selección del usuario
            spinner.setOnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position) as String
                selectedText = selectedItem
            }
            val enteredText = spinner.text.toString()

            Log.e("TAG", " $enteredText")


            val irc = textIRC.text.toString()
            if (irc.isEmpty()) {
                Toast.makeText(this, "Introduce un valor", Toast.LENGTH_LONG).show()
            } else {
                Log.e("TAG", irc)
            }


            if (carbs != null) {
                insul.text =((((enteredText.toDouble() / irc.toDouble())*100).roundToInt().toFloat())/100).toString() + " unidades"

            }

        }



    }


}
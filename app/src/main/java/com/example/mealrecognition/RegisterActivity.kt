package com.example.mealrecognition

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.example.mealrecognition.databinding.ActivityRegisterBinding
import com.example.mealrecognition.upload.LogmealAPI
import com.example.mealrecognition.upload.receivers.UserResponse
import com.example.mealrecognition.upload.uploaders.UserRequest
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var binding : ActivityRegisterBinding
    private lateinit var emailInput : EditText
    private lateinit var pswInput : EditText
    private lateinit var button: Button
    private lateinit var viewLogin : TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var idInput: EditText
    var patient_id : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        emailInput= binding.editText
        idInput = binding.editId
        pswInput = binding.editTextPassword
        button = binding.buttonRegister
        viewLogin = binding.textViewLogin
        progressBar = binding.progressbar
        mAuth= FirebaseAuth.getInstance()

        idInput = binding.editId


        val sp = getSharedPreferences("id_pref", MODE_PRIVATE)
        patient_id = sp.getInt("id", -1)


        button.setOnClickListener{
            val user = emailInput.editableText.toString()
            val password = pswInput.editableText.toString()

            if (user.isEmpty()) {
                Toast.makeText(this,"Usuario requerido", Toast.LENGTH_LONG).show()
            }

            if(password.isEmpty() || password.length <6){
                Toast.makeText(this,"Contraseña de más de 6 digitos requerida", Toast.LENGTH_LONG).show()

            }
            val idNumber = idInput.editableText.toString()
            if (idNumber.isEmpty()) {
                Toast.makeText(this,"Introduzca un ID", Toast.LENGTH_LONG).show()
            }


            val editor = sp.edit()
            editor.putInt("id", idNumber.toInt())
            editor.commit()
            registerUser(user,password)

        }

        viewLogin.setOnClickListener{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }



    }

    private fun registerUser (user: String, password: String) {
        progressBar.visibility = View.VISIBLE
        mAuth.createUserWithEmailAndPassword(user,password)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility=View.GONE
                if (task.isSuccessful) {
                    login()
                    obtaintoken(user)




                } else {
                    task.exception?.message?.let{
                        toast(it)
                    }
                }
            }
    }

    override fun onStart() {
        super.onStart()
        mAuth.currentUser?.let {
            login()
        }
    }

    /*fun getAuthorizationHeader(): String {
        val token = parseToken(obtaintoken(idInput.editableText.toString())) // Reemplaza "tu_token" con la lógica para obtener el token generado
        return "Bearer $token"
    }

     */



    private fun obtaintoken(username: String) {
        val language= "spa"
        val request = UserRequest(username, language)
        LogmealAPI().tokenUser(request).enqueue(object : Callback<UserResponse> {
            override fun onResponse(
                call: Call<UserResponse>,
                response: Response<UserResponse>
            ) {
                response.body()?.let {
                    val msg = it.msg
                    val token = it.token
                    val id = it.id

                    val response_data = JSONObject()
                    response_data.put("id", id)
                    response_data.put("msg", msg)
                    response_data.put("token", token)

                    val token_user = parseToken(response_data)
                    Log.e("TAG",  token_user)

                    val sharedPrefToken = getSharedPreferences("token_user", Context.MODE_PRIVATE)
                    val editor = sharedPrefToken?.edit()
                    editor?.putString("token", token_user)
                    editor?.apply()



                    if (response.isSuccessful) {
                        val confirmationResponse = response.body()
                        println(confirmationResponse)

                    } else {
                        println("Error en la respuesta: ${response.code()}")
                    }

                }

            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {

            }
        }
        )
    }

    private fun parseToken (data: JSONObject): String{
        val token = data.getString("token")

        return token


    }


}
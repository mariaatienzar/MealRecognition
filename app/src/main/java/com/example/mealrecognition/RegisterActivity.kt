package com.example.mealrecognition

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.mealrecognition.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRegistrar
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var binding : ActivityRegisterBinding
    private lateinit var idInput : EditText
    private lateinit var pswInput : EditText
    private lateinit var button: Button
    private lateinit var viewLogin : TextView
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idInput = binding.editText
        pswInput = binding.editTextPassword
        button = binding.buttonRegister
        viewLogin = binding.textViewLogin
        progressBar = binding.progressbar
        mAuth= FirebaseAuth.getInstance()


        button.setOnClickListener{
            val user = idInput.editableText.toString()
            val password = pswInput.editableText.toString()

            if (user.isEmpty()) {
                Toast.makeText(this,"Usuario requerido", Toast.LENGTH_LONG).show()
            }

            if(password.isEmpty() || password.length <6){
                Toast.makeText(this,"Contraseña de más de 6 digitos requerida", Toast.LENGTH_LONG).show()

            }

            //val intent = Intent(this, HomeActivity::class.java)
            //startActivity(intent)
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


}


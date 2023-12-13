package com.example.mealrecognition

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mealrecognition.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var btn: Button
    private lateinit var idInput: EditText
    private lateinit var pswInput: EditText
    private lateinit var viewRegister: TextView
    private lateinit var viewForgetPsw: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var binding: ActivityLoginBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btn = binding.buttonSignIn
        idInput = binding.editText
        pswInput = binding.editTextPassword
        viewRegister = binding.textViewRegister
        viewForgetPsw =binding.textViewForgetPassword

        progressBar = binding.progressbar

        mAuth = FirebaseAuth.getInstance()


        btn.setOnClickListener {
            val user = idInput.editableText.toString()
            val password = pswInput.editableText.toString()

            //startActivity(Intent(this, LogmealAPI::class.java).putExtra("user",user))



            loginUser(user,password)

        }


        viewRegister.setOnClickListener{
            val intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }
        viewForgetPsw.setOnClickListener{
            val intent2 = Intent(this,ForgotPasswordActivity::class.java)
            startActivity(intent2)
        }




    }
    private fun loginUser(user: String, password:String){
        if (user.isNotEmpty() && password.isNotEmpty()) {
            progressBar.visibility = View.VISIBLE
            mAuth.signInWithEmailAndPassword(user,password)
                .addOnCompleteListener(this){ task->
                    progressBar.visibility = View.GONE
                    if(task.isSuccessful){
                        login()
                    }
                    else{
                        task.exception?.message?.let {
                            toast(it)
                        }
                    }

                }

        }else if (user.isEmpty()){
            Toast.makeText(this,"Usuario requerido", Toast.LENGTH_LONG).show()


        }else if (password.isEmpty()){
            Toast.makeText(this,"Contraseña requerida", Toast.LENGTH_LONG).show()

        }


        if( password.length <6){
            Toast.makeText(this,"Contraseña de más de 6 digitos requerida", Toast.LENGTH_LONG).show()

        }


    }
    override fun onStart() {
        super.onStart()
        mAuth.currentUser?.let {
            login()
        }
    }





}





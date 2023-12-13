package com.example.mealrecognition

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.example.mealrecognition.databinding.ActivityForgotPassword2Binding
import com.example.mealrecognition.databinding.ActivityForgotPasswordBinding
import com.example.mealrecognition.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var btn: Button
    private lateinit var idInput: EditText
     private lateinit var progressBar: ProgressBar
    private lateinit var binding: ActivityForgotPassword2Binding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password2)


        binding = ActivityForgotPassword2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        btn = binding.btnSend
        idInput=binding.textEmail
        progressBar=binding.progressbar

        btn.setOnClickListener {
            val email = idInput.text.toString().trim()

            if (email.isEmpty()) {
                idInput.error = "Email Required"
                idInput.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                idInput.error = "Valid Email Required"
                idInput.requestFocus()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            FirebaseAuth.getInstance()
                .sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        this.toast("Check your email")
                    } else {
                        this.toast(task.exception?.message!!)
                    }
                }
        }
    }
}
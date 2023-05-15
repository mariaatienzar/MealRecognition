package com.example.mealrecognition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.example.mealrecognition.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var weightInput: EditText
    private lateinit var heightInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var sexInput: EditText
    private lateinit var button: Button
    private lateinit var progressBar: ProgressBar

    
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        weightInput = binding.textWeight
        heightInput = binding.textHeight
        ageInput = binding.textAge
        sexInput = binding.textSex
        button = binding.buttonSave
        progressBar = binding.progressbar
        currentUser?.let { user ->
            weightInput.setText(user.displayName)
            heightInput.setText(user.displayName)
            ageInput.setText(user.displayName)
            sexInput.setText(user.displayName)
        }
        button.setOnClickListener {
            val weight = weightInput.editableText.toString()
            val height = heightInput.editableText.toString()
            val age = ageInput.editableText.toString()
            val sex = sexInput.editableText.toString()

            if (weight.isEmpty()) {
                //Toast.makeText(activity, "La foto seleccionada ya ha sido subida", Toast.LENGTH_LONG).show()
                weightInput.error = "Indica el peso"
                //IllegalArgumentException("Indica el peso")
                weightInput.requestFocus()
                return@setOnClickListener
            }
            if (height.isEmpty()) {
                heightInput.error = "Indica la altura"
                heightInput.requestFocus()
                return@setOnClickListener
            }
            if (age.isEmpty()) {
                ageInput.error = "Indica la edad"
                ageInput.requestFocus()
                return@setOnClickListener
            }
            if (sex.isEmpty()) {
                sexInput.error = "Indica el sexo"
                sexInput.requestFocus()
                return@setOnClickListener

            }


            //MIRAR UPDATING EMAIL 10 TUTORIAL (crear fragmento de actualizacion , no se si se necesita TextView)
            val updates = UserProfileChangeRequest.Builder()
                .setDisplayName(weight)
                .setDisplayName(height)
                .setDisplayName(age)
                .setDisplayName(sex)
                .build()


            progressBar.visibility = View.VISIBLE


            currentUser?.updateProfile(updates)
                ?.addOnCompleteListener { task ->
                    progressBar.visibility = View.INVISIBLE
                    if (task.isSuccessful) {
                        context?.toast("Perfil actualizado")
                    } else {
                        context?.toast(task.exception?.message!!)
                    }
                }
        }

    }

}
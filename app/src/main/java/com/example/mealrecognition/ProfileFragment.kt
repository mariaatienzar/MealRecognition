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
    private lateinit var ircInput: EditText
    private lateinit var correcInput: EditText
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
        ircInput = binding.textIRC
        correcInput=binding.textFactorCorrec
        button = binding.buttonSave
        progressBar = binding.progressbar

        button.setOnClickListener {

            val weight = weightInput.text.toString()
            val height = heightInput.text.toString()
            val age = ageInput.text.toString()
            val sex = sexInput.text.toString()
            val irc = ircInput.text.toString()
            val correc = correcInput.text.toString()

            if (weight.isEmpty()) {
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

            if (irc.isEmpty()) {
                ircInput.error = "Indica el I:C"
                ircInput.requestFocus()
                return@setOnClickListener
            }
            if (correc.isEmpty()) {
                ircInput.error = "Indica el factor de correción de insulina"
                ircInput.requestFocus()
                return@setOnClickListener
            }


            //MIRAR UPDATING EMAIL 10 TUTORIAL (crear fragmento de actualizacion , no se si se necesita TextView)
            val updates = UserProfileChangeRequest.Builder()
                .setDisplayName("Peso: $weight kg")
                .setDisplayName("Altura: $height m")
                .setDisplayName("Edad: $age años")
                .setDisplayName("Sexo: $sex")
                .setDisplayName("Relación insulina-Carbohidratos: $irc")
                .setDisplayName("Factor correción insulina: $correc")
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
package com.example.mealrecognition

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.mealrecognition.databinding.FragmentGalleryBinding
import com.example.mealrecognition.databinding.FragmentHomeBinding
import com.example.mealrecognition.databinding.FragmentProfileBinding
import com.example.mealrecognition.upload.LogmealAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment() {
    lateinit var _binding: FragmentHomeBinding
    private val binding get() = _binding
    lateinit var currentTV: TextView
    lateinit var carbohTV: TextView
    lateinit var stepsTv: TextView
    lateinit var caloriesTV: TextView

    private val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
    private val ACTION_PREVIOUS_DATA = "com.example.bluetooth.le.ACTION_PREVIOUS_DATA"
    private val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
    private val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // on below line we are initializing our variables.
        carbohTV = binding.upMenuCarbsLeft
        stepsTv = binding.upMenuSubText
            //binding.upMenuProteinsLeft
        caloriesTV = binding.upMenuFatsLeft
        currentTV = binding.tvCurrent

        // on below line we are creating and initializing
        // variable for simple date format.
        val sdf = SimpleDateFormat("dd/MM/yyyy     HH:mm")

        // on below line we are creating a variable for
        // current date and time and calling a simple
        // date format in it.
        val currentDateAndTime = sdf.format(Date())

        // on below line we are setting current
        // date and time to our text view.
        currentTV.text = currentDateAndTime
        return root




    }
    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())

    }
    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter().apply {
            addAction(ACTION_DATA_AVAILABLE)
            addAction(ACTION_PREVIOUS_DATA)
            addAction(ACTION_GATT_CONNECTED)
            addAction(ACTION_GATT_DISCONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        }
        return intentFilter
    }
    private val gattUpdateReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action){
                ACTION_DATA_AVAILABLE -> {
                    displayData(intent)
                }
                ACTION_PREVIOUS_DATA -> {

                }
                ACTION_GATT_CONNECTED -> {
                    Log.e("APPCONN", "GATT CONNECTED")
                }
                ACTION_GATT_DISCONNECTED -> {
                    Log.e("APPCONN", "GATT DISCONNECTED")
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    Log.e("APPCONN", "ACL CONNECTED")
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    Log.e("APPCONN", "ACL DISCONNECTED")

                }
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayData(intent: Intent?) {
        var steps: String? = intent!!.getStringExtra("steps")
        if(steps != null) {
            try {
                stepsTv?.text = steps
            } catch (e1:NullPointerException) {
                Log.e("OVERVIEW FRAGMENT", "STEPS CATCH")
            }
        }
        var calories: String? = intent!!.getStringExtra("calories")
        if(calories != null) {
            try {
                caloriesTV?.text = calories

            } catch (e2:NullPointerException) {
                Log.e("OVERVIEW FRAGMENT", "CALORIES CATCH")
            }
        }
        var heartRate: String? = intent!!.getStringExtra("heart_rate")
        if(heartRate != null) {
            try {
                carbohTV?.setText(heartRate + " lpm")
            } catch (e3:NullPointerException) {
                Log.e("OVERVIEW FRAGMENT", "HR  CATCH")
            }
        }
    }


}












    /*private lateinit var _binding: FragmentHomeBinding
    private val binding get() = _binding!!

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var BottomNavigationView: BottomNavigationView
    private lateinit var navView : NavigationView
    private lateinit var drawerLayout: ConstraintLayout


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState): View {

            _binding = FragmentHomeBinding.inflate(inflater, container, false)

            return binding.root
        }
        BottomNavigationView= binding.navView
        //constraintLayout = binding.drawerLayout
        drawerLayout = binding.drawerLayout
        val navController = findNavController(R.id.fragment)

        //val navView : Toolbar = findViewById(R.id.nav_view)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.profileFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_home, container, false)
    }


}*/
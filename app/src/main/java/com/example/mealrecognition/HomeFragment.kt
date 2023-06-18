package com.example.mealrecognition

import android.R
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.mealrecognition.databinding.FragmentHomeBinding
import com.jjoe64.graphview.series.DataPoint
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment() {
    lateinit var _binding: FragmentHomeBinding
    private val binding get() = _binding
    lateinit var currentTV: TextView
    lateinit var carbohTV: TextView
    lateinit var stepsTv: TextView
    lateinit var caloriesTV: TextView
    lateinit var tv: TextView
    private lateinit var b1 : Button
    private lateinit var b2: TextView
    private lateinit var b3: Button
    private lateinit var fl: FrameLayout
    private lateinit var ll: LinearLayout
    private lateinit var cont: ConstraintLayout


    private var db: SQLiteDatabase? = null

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
        b1 = binding.intro1
        b2 = binding.intro2
        b3 = binding.intro3
        tv = binding.tv
        fl = binding.fl
        cont = binding.Container


        b1.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(fl.id, BtFragment()) // Replace R.id.frameLayout with the actual ID of your FrameLayout
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

        }

        b2.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            // Remove all fragments from the back stack
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

            // Replace the fragment container with the new fragment
            fragmentTransaction.replace(fl.id, ProfileFragment()) // Replace R.id.frameLayout with the actual ID of your FrameLayout
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }



        b3.setOnClickListener {
            startActivity(Intent(this.context, CameraActivity::class.java))

        }





/*

        // On button click, a bundle is initialized and the
        // text from the EditText is passed in the custom
        // fragment using this bundle
        b1.setOnClickListener {
            val anotherFragment = BtFragment()
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(
                this.id,
                anotherFragment
            ) // Replace R.id.container with the actual container ID of your layout
            fragmentTransaction.commit()
        }

        b2.setOnClickListener {
            val profileFragment = ProfileFragment()
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(
                this.id,
                profileFragment
            ) // Replace R.id.container with the actual container ID of your layout
            fragmentTransaction.commit()
        }

        b3.setOnClickListener{
            startActivity(Intent(this.context, HomeActivity::class.java))
        }

 */

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

        val lastHeartRate = getLastHeartRate()

        if (lastHeartRate != null) {

            // Do something with the lastHeartRate value
            // For example, display it in a TextView
            carbohTV.text = lastHeartRate + "lpm"
        } else {
            // Handle case when no Heart Rate values are available
            // Display an appropriate message or handle it as needed
        }

        val lastSteps = getLastSteps()

        if (lastSteps != null) {

            // Do something with the lastHeartRate value
            // For example, display it in a TextView
            stepsTv.text = lastSteps
        } else {
            // Handle case when no Heart Rate values are available
            // Display an appropriate message or handle it as needed
        }
        val lastCalories = getLastCalories()

        if (lastCalories != null) {

            // Do something with the lastHeartRate value
            // For example, display it in a TextView
            caloriesTV.text = lastCalories
        } else {
            // Handle case when no Heart Rate values are available
            // Display an appropriate message or handle it as needed
        }







        return root


    }

/*
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
                stepsTv?.text = "$steps"
            } catch (e1:NullPointerException) {
                Log.e("OVERVIEW FRAGMENT", "STEPS CATCH")
            }
        }
        else{
            val lastSteps = getLastSteps()
            stepsTv.text = "$lastSteps "

        }

        var calories: String? = intent!!.getStringExtra("calories")
        if(calories != null) {
            try {
                caloriesTV?.text = calories

            } catch (e2:NullPointerException) {
                Log.e("OVERVIEW FRAGMENT", "CALORIES CATCH")
            }
        }
        else{
            val lastCalories = getLastCalories()
            caloriesTV.text = "$lastCalories"

        }
        var heartRate: String? = intent!!.getStringExtra("heart_rate")
        if(heartRate != null) {
            try {
                carbohTV?.setText(heartRate + " lpm")
            } catch (e3:NullPointerException) {
                Log.e("OVERVIEW FRAGMENT", "HR  CATCH")
            }
        }
        else{
            val lastHeartRate = getLastHeartRate()
            carbohTV.text = lastHeartRate + " lpm"

        }
    }


 */

    fun getLastHeartRate(): String? {

       /* val absoluteHRArray: MutableList<DataPoint> = ArrayList()
        val dbHelper = MyOpenHelper(activity)
        db = dbHelper.writableDatabase
        val c: Cursor? = db!!.rawQuery("SELECT * FROM heartRates WHERE DATETIME <= datetime('now','localtime') AND DATETIME > datetime('now','localtime','-60 minutes') ORDER BY DATETIME ASC", null)


        */
        val dbHelper = MyOpenHelper(activity)
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "heartRates",
            arrayOf("lpm"),
            null,
            null,
            null,
            null,
            "_id DESC",
            "1"
        )

        val lastHeartRate: String? = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndex("lpm"))
        } else {
            null
        }

        cursor.close()
        return lastHeartRate
    }
    fun getLastCalories(): String? {

        val dbHelper = MyOpenHelper(activity)
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "calories_tb",
            arrayOf("calories"),
            null,
            null,
            null,
            null,
            "_id DESC",
            "1"
        )

        val lastCalories: String? = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndex("calories"))
        } else {
            null
        }

        cursor.close()
        return lastCalories
    }

    fun getLastSteps(): String? {

        val dbHelper = MyOpenHelper(activity)
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "steps_tb",
            arrayOf("steps"),
            null,
            null,
            null,
            null,
            "_id DESC",
            "1"
        )

        val lastSteps: String? = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndex("steps"))
        } else {
            null
        }

        cursor.close()
        return lastSteps
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


}

     */
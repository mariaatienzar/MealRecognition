package com.example.mealrecognition

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.example.mealrecognition.databinding.ActivityHomeBinding
import com.example.mealrecognition.databinding.ActivityLoginBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class HomeActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var appBarConfiguration_2: AppBarConfiguration
    private lateinit var toolBar : Toolbar
    private lateinit var navView : NavigationView
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navController: NavController

    private val REQUEST_ENABLE_BT: Int = 0
    private var bluetoothAdapter: BluetoothAdapter? = null
    var patient_id : Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navBottomView: BottomNavigationView = findViewById(R.id.bottom_navigation_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration_2 = AppBarConfiguration(
            setOf(R.id.homeFragment,R.id.physicalActFragment, R.id.bt_page, R.id.camFragment, R.id.intake),drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration_2)
        navView.setupWithNavController(navController)
        navBottomView.setupWithNavController(navController)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.profileFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        val prefs = this.getSharedPreferences("id_pref", MODE_PRIVATE)
        patient_id = prefs.getInt("id", 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_GSERVICES,


            )
            requestPermission(permissions)
        } else {
            val permissions = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            requestStoragePermission(permissions)
        }

    }





    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_logout){
            AlertDialog.Builder(this).apply {
                setTitle("¿Seguro de cerrar sesión?")
                setPositiveButton("Si") { _, _ ->

                    FirebaseAuth.getInstance().signOut()
                    logout()

                }
                setNegativeButton("Cancelar") { _, _ ->
                }
            }.create().show()

        }
        return super.onOptionsItemSelected(item)
    }

    private fun replaceFragment(fragment : Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment,fragment)
        fragmentTransaction.commit()


    }
    private fun requestStoragePermission(permissions: Array<String>) {
        if (ContextCompat.checkSelfPermission(this, permissions[0])
            == PackageManager.PERMISSION_GRANTED && (ContextCompat.checkSelfPermission(
                this,
                permissions[1]
            )
                    == PackageManager.PERMISSION_GRANTED)
        ) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                Log.i("TAG", "activo")
            }.launch(enableBtIntent)
            Log.i("MAIN", "Permiso concedido")
        } else {
            requestPermissions(permissions, 0)
        }
    }

    private fun requestPermission(permissions: Array<String>) {
        if (ContextCompat.checkSelfPermission(this, permissions[0])
            == PackageManager.PERMISSION_GRANTED && (ContextCompat.checkSelfPermission(
                this,
                permissions[1]
            )
                    == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                this,
                permissions[2]
            )
                    == PackageManager.PERMISSION_GRANTED)
        ) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                Log.i("TAG", "activo")
            }.launch(enableBtIntent)
            Log.i("MAIN", "Permiso concedido")
        } else {
            requestPermissions(permissions, 0)
        }
    }
    override fun onDestroy() {
        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, Restarter::class.java)
        this.sendBroadcast(broadcastIntent)
        super.onDestroy()
    }

}
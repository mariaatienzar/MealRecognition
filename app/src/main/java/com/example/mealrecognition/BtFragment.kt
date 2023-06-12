package com.example.mealrecognition

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mealrecognition.databinding.FragmentBtBinding

class BtFragment : Fragment() {

    private var _binding: FragmentBtBinding? = null
    private val binding get() = _binding!!

    private val REQUEST_ENABLE_BT: Int = 0
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothDevice: BluetoothDevice? = null
    private var mDeviceAddress: String? = null
    private var mBluetoothLeService: BluetoothLeService? = null

    private var mLeDevices: ArrayList<BluetoothDevice> = ArrayList<BluetoothDevice>()
    private var arrayAdapter: ArrayAdapter<String>? = null
    private var arrayString: ArrayList<String> = ArrayList<String>()
    private var listView: ListView? = null
    private var textViewValue: TextView? = null
    private var switch: Switch? = null
    private var isConnected:Boolean = false
    private var device:String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBtBinding.inflate(inflater, container, false)
        val root: View = binding.root

        switch = binding.swConnection
        textViewValue = binding.textViewValue
        listView = binding.listViewMA

        val bluetoothManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        val prefs = requireActivity().getSharedPreferences("sb_pref", MODE_PRIVATE)
        val editor = prefs.edit()
        device = prefs.getString("device", "").toString()

        if(device != "") {
            isConnected = true
            switch!!.setChecked(true)
            textViewValue!!.setText("Conectado a: " + device)
        }

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_CONNECT)
            == PackageManager.PERMISSION_GRANTED) {
            switch!!.isEnabled = true
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    switch!!.isEnabled = true
                } else {
                    Toast.makeText(activity, "Active el Bluetooth para conectar la pulsera", Toast.LENGTH_LONG).show()
                    switch!!.isEnabled = false
                }
            }.launch(enableBtIntent)
        }

        switch!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showDevicesList()
            } else {
                listView!!.visibility = View.INVISIBLE
                textViewValue!!.setText("")
                editor.putString("device", "")
                editor.commit()

                val stopIntent = Intent(activity, BluetoothLeService::class.java)
                activity?.stopService(stopIntent)
                val stopIntent2 = Intent(activity, DataUploader::class.java)
                activity?.stopService(stopIntent2)
            }
        }

        // Comienza la conexion con el dispositivo seleccionado
        listView!!.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                bluetoothDevice = mLeDevices.get(i) as BluetoothDevice
                mDeviceAddress = bluetoothDevice!!.address

                listView!!.visibility = View.INVISIBLE
                device = bluetoothDevice!!.name + " - " + mDeviceAddress
                textViewValue!!.setText("Conectado a: " + device)

                /*val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE)*/

                val startIntent = Intent(activity, BluetoothLeService::class.java)
                startIntent.putExtra("address", mDeviceAddress);
                activity?.startService(startIntent)

                val startIntent2 = Intent(activity, DataUploader::class.java)
                activity?.startService(startIntent2)

                editor.putString("device", device)
                editor.commit()
                isConnected = true
            }

        return root
    }

    private fun showDevicesList() {
        textViewValue!!.setText("Dispositivos encontrados")

        listView!!.visibility = View.VISIBLE

        arrayString.clear()
        mLeDevices.clear()
        arrayAdapter?.clear()

        //Dispositivos vinculados
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            arrayString.add(device.name + " - " + device.address)
            mLeDevices.add(device)
            Log.i("AndroidAPS MonitorActivity", "Bonded Address device: " + device.address)
            Log.i("AndroidAPS MonitorActivity", "Bonded Name device: " + device.name)
        }
        Log.i("AndroidAPS MonitorActivity", arrayString.toString())
        arrayAdapter = activity?.let { ArrayAdapter<String>(it, R.layout.my_list_view, arrayString) }
        listView!!.adapter = arrayAdapter
    }
}
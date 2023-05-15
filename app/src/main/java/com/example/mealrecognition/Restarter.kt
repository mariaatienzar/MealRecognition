package com.example.mealrecognition

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

import android.widget.Toast

class Restarter : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("Broadcast Listened", "Service tried to stop")
        val device: BluetoothDevice = intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) ?: return
        Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context!!.startForegroundService(Intent(context, BluetoothLeService::class.java))
            context!!.startForegroundService(Intent(context, DataUploader::class.java))

        } else {
            context!!.startService(Intent(context, BluetoothLeService::class.java))
            context!!.startService(Intent(context, DataUploader::class.java))
        }
    }
}
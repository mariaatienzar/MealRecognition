package com.example.mealrecognition

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.mealrecognition.databinding.FragmentMonitoringBinding
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

class MonitoringFragment : Fragment() {
    private var _binding: FragmentMonitoringBinding? = null
    private val binding get() = _binding!!

    private val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
    private val ACTION_PREVIOUS_DATA = "com.example.bluetooth.le.ACTION_PREVIOUS_DATA"
    private val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
    private val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"

    private var tvGraphHr: TextView? = null
    private var heartRateTv: TextView? = null
    private var tvGraphSteps: TextView? = null
    private var stepsTv: TextView? = null
    private var tvGraphCal: TextView? = null
    private var calTv: TextView? = null

    lateinit var id_button: ImageButton
    lateinit var id_tv : TextView

    private var db: SQLiteDatabase? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMonitoringBinding.inflate(inflater, container, false)
        val root: View = binding.root

        tvGraphHr = binding.graphsLayout.tvGraphHr
        heartRateTv = binding.statusMonitoringLayout.heartRateTv
        tvGraphSteps = binding.graphsLayout.tvGraphSteps
        stepsTv = binding.statusMonitoringLayout.stepsTv
        tvGraphCal = binding.graphsLayout.tvGraphCalorias
        calTv = binding.statusMonitoringLayout.caloriesTv

        id_button = binding.idButton
        id_tv = binding.idTextview

        id_button.setOnClickListener {
            val prefs = activity?.getSharedPreferences("id_pref", AppCompatActivity.MODE_PRIVATE)
            val patient_id = prefs?.getInt("id", 0)
            object : CountDownTimer(1000, 1000) {
                override fun onTick(p0: Long) {
                    id_tv.text = "Tu id es : %s".format(patient_id)
                }
                override fun onFinish() {
                    id_tv.text = ""
                }
            }.start()
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
        updateHRGraph()
        updateStepsGraph()
        updateCaloriesGraph()
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
                updateStepsGraph()
            } catch (e1:NullPointerException) {
                Log.e("OVERVIEW FRAGMENT", "STEPS CATCH")
            }
        }
        var calories: String? = intent!!.getStringExtra("calories")
        if(calories != null) {
            try {
                calTv?.text = calories
                updateCaloriesGraph()
            } catch (e2:NullPointerException) {
                Log.e("OVERVIEW FRAGMENT", "CALORIES CATCH")
            }
        }
        var heartRate: String? = intent!!.getStringExtra("heart_rate")
        if(heartRate != null) {
            try {
                heartRateTv?.setText(heartRate + " lpm")
                updateHRGraph()
            } catch (e3:NullPointerException) {
                Log.e("OVERVIEW FRAGMENT", "HR  CATCH")
            }
        }
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateHRGraph() {
        val absoluteHRArray: MutableList<DataPoint> = ArrayList()
        val dbHelper = MyOpenHelper(activity)
        db = dbHelper.writableDatabase
        val c: Cursor? = db!!.rawQuery("SELECT * FROM heartRates WHERE DATETIME <= datetime('now','localtime') AND DATETIME > datetime('now','localtime','-60 minutes')", null)
        if (c != null) {
            if((c.getCount() > 0)) {
                val calendar = Calendar.getInstance()
                var ini = calendar.getTimeInMillis()
                //Represento 1 hora, 6 horas (21600000L)
                var dif = 3600000L - ini
                c.moveToFirst()
                do {
                    var timestamp: Timestamp = Timestamp.valueOf(c.getString(c.getColumnIndex("DATETIME")))
                    var scaledTime = timestamp.getTime() + dif
                    scaledTime = scaledTime * 60
                    var scaledTimeDb = scaledTime.toDouble()/3600000L
                    absoluteHRArray.add(DataPoint(scaledTimeDb, (c.getString(c.getColumnIndex("lpm"))).toDouble()))
                    /*Log.e("OVERVIEW FRAGMENT", "{" + "\"timestamp\": " + c.getString(c.getColumnIndex("DATETIME")) + "\n"
                        + "\"heartRate\": " + c.getString(c.getColumnIndex("lpm")) + "}," + "\n")*/
                } while (c.moveToNext())
            }
        }
        //Cerramos el cursor
        c!!.close()
        dbHelper.close()

        val graph : GraphView = binding.graphsLayout.heartRGraph
        graph.series.clear()

        val series: LineGraphSeries<DataPoint> = LineGraphSeries(Array(absoluteHRArray.size) { i -> absoluteHRArray[i] }).also {
            it.isDrawBackground = true
            it.thickness = 1
        }
        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMaxX(60.0)
        graph.viewport.setMinX(0.0)
        graph.gridLabelRenderer.numHorizontalLabels = 7
        graph.gridLabelRenderer.isHighlightZeroLines = false
        graph.addSeries(series)

        val locaDate: LocalDateTime = LocalDateTime.now()
        val hoursFin: Int = locaDate.getHour()
        val hoursIni: Int = locaDate.getHour() - 1
        val minutes: Int = locaDate.getMinute()
        var minutes_s = minutes.toString()
        if (minutes < 10) minutes_s = "0" + minutes_s
        tvGraphHr?.setText("  Frecuencia cardíaca (" + hoursIni + ":" + minutes_s + " h - " + hoursFin + ":" + minutes_s + " h)")
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateStepsGraph() {
        val absoluteStepsArray: MutableList<DataPoint> = java.util.ArrayList()
        val dbHelper = MyOpenHelper(activity)
        db = dbHelper.writableDatabase
        val c: Cursor? = db!!.rawQuery("SELECT * FROM steps_tb WHERE DATETIME <= datetime('now','localtime') AND DATETIME > datetime('now','localtime','-1 hours')", null)
        if (c != null) {
            if((c.getCount() > 0)) {
                val calendar = Calendar.getInstance()
                var ini = calendar.getTimeInMillis()
                //Represento 1 hora, 6 horas (21600000L)
                var dif = 3600000L - ini
                c.moveToFirst()
                do {
                    var timestamp: Timestamp = Timestamp.valueOf(c.getString(c.getColumnIndex("DATETIME")))
                    var scaledTime = timestamp.getTime() + dif
                    scaledTime = scaledTime * 60
                    var scaledTimeDb = scaledTime.toDouble()/3600000L
                    absoluteStepsArray.add(DataPoint(scaledTimeDb, (c.getString(c.getColumnIndex("steps"))).toDouble()))
                    /*Log.e("OVERVIEW FRAGMENT", "{" + "\"timestamp\": " + c.getString(c.getColumnIndex("DATETIME")) + "\n"
                        + "\"heartRate\": " + c.getString(c.getColumnIndex("steps")) + "}," + "\n")*/
                } while (c.moveToNext())
            }
        }
        //Cerramos el cursor
        c!!.close()
        dbHelper.close()

        val graph : GraphView = binding.graphsLayout.stepsGraph
        graph.series.clear()

        val series: LineGraphSeries<DataPoint> = LineGraphSeries(Array(absoluteStepsArray.size) { i -> absoluteStepsArray[i] }).also {
            it.isDrawBackground = true
            it.thickness = 1
        }
        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMaxX(60.0)
        graph.viewport.setMinX(0.0)
        graph.gridLabelRenderer.numHorizontalLabels = 7
        graph.gridLabelRenderer.isHighlightZeroLines = false
        graph.addSeries(series)

        val locaDate: LocalDateTime = LocalDateTime.now()
        val hoursFin: Int = locaDate.getHour()
        val hoursIni: Int = locaDate.getHour() - 1
        val minutes: Int = locaDate.getMinute()
        var minutes_s = minutes.toString()
        if (minutes < 10) minutes_s = "0" + minutes_s
        tvGraphSteps?.text = "  Pasos (" + hoursIni + ":" + minutes_s + " h - " + hoursFin + ":" + minutes_s + " h)"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCaloriesGraph() {
        val absoluteCaloriesArray: MutableList<DataPoint> = java.util.ArrayList()
        val dbHelper = MyOpenHelper(activity)
        db = dbHelper.writableDatabase
        val c: Cursor? = db!!.rawQuery("SELECT * FROM calories_tb WHERE DATETIME <= datetime('now','localtime') AND DATETIME > datetime('now','localtime','-1 hours')", null)
        if (c != null) {
            if((c.getCount() > 0)) {
                val calendar = Calendar.getInstance()
                var ini = calendar.getTimeInMillis()
                //Represento 1 hora, 6 horas (21600000L)
                var dif = 3600000L - ini
                c.moveToFirst()
                do {
                    var timestamp: Timestamp = Timestamp.valueOf(c.getString(c.getColumnIndex("DATETIME")))
                    var scaledTime = timestamp.getTime() + dif
                    scaledTime = scaledTime * 60
                    var scaledTimeDb = scaledTime.toDouble()/3600000L
                    absoluteCaloriesArray.add(DataPoint(scaledTimeDb, (c.getString(c.getColumnIndex("calories"))).toDouble()))
                    /*Log.e("OVERVIEW FRAGMENT", "{" + "\"timestamp\": " + c.getString(c.getColumnIndex("DATETIME")) + "\n"
                        + "\"heartRate\": " + c.getString(c.getColumnIndex("calories")) + "}," + "\n")*/
                } while (c.moveToNext())
            }
        }
        //Cerramos el cursor
        c!!.close()
        dbHelper.close()

        val graph : GraphView = binding.graphsLayout.caloriesGraph
        graph.series.clear()

        val series: LineGraphSeries<DataPoint> = LineGraphSeries(Array(absoluteCaloriesArray.size) { i -> absoluteCaloriesArray[i] }).also {
            it.isDrawBackground = true
            it.thickness = 1
        }
        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMaxX(60.0)
        graph.viewport.setMinX(0.0)
        graph.gridLabelRenderer.numHorizontalLabels = 7
        graph.gridLabelRenderer.isHighlightZeroLines = false
        graph.addSeries(series)

        val locaDate: LocalDateTime = LocalDateTime.now()
        val hoursFin = locaDate.getHour().toString()
        val hoursIni = (locaDate.getHour() - 1).toString()
        val minutes: Int = locaDate.getMinute()
        var minutes_s = minutes.toString()
        if (minutes < 10) minutes_s = "0" + minutes_s
        tvGraphCal?.text = "  Calorías (" + hoursIni + ":" + minutes_s + " h - " + hoursFin + ":" + minutes_s + " h)"
    }
}
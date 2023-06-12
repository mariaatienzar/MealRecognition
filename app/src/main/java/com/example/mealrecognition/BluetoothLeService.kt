package com.example.mealrecognition

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.math.BigInteger
import java.text.DecimalFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.*
import java.util.concurrent.TimeUnit

private val TAG = "AppBLE:::BLEService"

private val STATE_DISCONNECTED = 0
private val STATE_CONNECTING = 1
private val STATE_CONNECTED = 2
private val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
private val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
private val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
private val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
private val ACTION_PREVIOUS_DATA = "com.example.bluetooth.le.ACTION_PREVIOUS_DATA"
private val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"

private val UUID_HEART_RATE_MEASUREMENT_XIAOMI:UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
private val UUID_ACTIVITY_MEASUREMENT_XIAOMI = UUID.fromString("00000007-0000-3512-2118-0009af100700")
private val UUID_MEASUREMENT_FITBIT = UUID.fromString("558dfa01-4fa8-4105-9f02-4eaa93e62980")
private val UUID_PREVIOUS_ACTIVITY_DATA = UUID.fromString("00000005-0000-3512-2118-0009af100700")
private val UUID_PREVIOUS_ACTIVITY_DATA_2 = UUID.fromString("00000004-0000-3512-2118-0009af100700")
private val CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

private var bluetoothManager: BluetoothManager? = null
private var bluetoothAdapter: BluetoothAdapter? = null
private var bluetoothDeviceAddress: String? = null
private var bluetoothGatt: BluetoothGatt? = null
private var connectionState = STATE_DISCONNECTED

private var characteristics:ArrayList<BluetoothGattCharacteristic> = ArrayList<BluetoothGattCharacteristic>()
private var characteristics2:ArrayList<BluetoothGattCharacteristic> = ArrayList<BluetoothGattCharacteristic>()

private var dbS: SQLiteDatabase? = null

private var esPrimera: Boolean = true
private var inicio: Long = 0
private var fin: Long = 0

private var listHearRate: ArrayList<String> = ArrayList<String>()
private var listSteps: ArrayList<String> = ArrayList<String>()
private var listCalories: ArrayList<String> = ArrayList<String>()

private var address: String = ""

lateinit var task: TimerTask
lateinit var realDate : LocalDateTime
lateinit var localDate : LocalDateTime
private var lastIndex = -1
private var lastValue: String? = null


//Servicio que interactua con el dispositivo BLE
class BluetoothLeService : Service() {

    private var connectionState = STATE_DISCONNECTED

    override fun onCreate() {
        Log.d(TAG, "Servicio creado...")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startMyOwnForeground() else startForeground(
            1,
            Notification()
        )
    }

    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "example.permanence"
        val channelName = "Background Service"
        val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName,
            NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running in background")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }

    override fun onStartCommand(intent: Intent?, flag: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand executed")

        if (intent != null && intent.getExtras() != null) {
            address = intent!!.getStringExtra("address").toString()
        }

        if (!initialize()) {
            Log.e("AppBLE Activity", "Unable to initialize Bluetooth")
            stopSelf() //Se para el servicio
            //finish()
        }

        if (!connect(address)) {
            Log.e("AppBLE Activity", "Unable to connect device")
            stopSelf() //Se para el servicio
        }

        return START_STICKY
    }

    override fun onDestroy() {
        close()
        super.onDestroy()
        Log.e(TAG, "onDestroy executed")
        val prefs = getSharedPreferences("sb_pref", MODE_PRIVATE)
        val editor = prefs.edit()

        if (prefs.getString("device", "").toString() != "") {
            editor.putString("device", "")
            editor.commit()
        }
    }

    //Metodos callback
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            val intentAction: String
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    intentAction = ACTION_GATT_CONNECTED
                    connectionState = STATE_CONNECTED
                    broadcastUpdate(intentAction)
                    Log.i(TAG, "Connected to GATT server.")
                    Log.i(TAG, "Attempting to start service discovery: " + bluetoothGatt?.discoverServices())
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    intentAction = ACTION_GATT_DISCONNECTED
                    connectionState = STATE_DISCONNECTED
                    Log.i(TAG, "Disconnected from GATT server.")
                    broadcastUpdate(intentAction)
                }
            }
        }

        //Discovered services
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    loopGattServices(gatt)
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                }
                else -> Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        //Resultado de la operacion de lectura de una caracteristica
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt,
                                             characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == UUID_PREVIOUS_ACTIVITY_DATA_2 &&
                !characteristic.value.contentEquals(byteArrayOf(0x10, 0x02, 0x04))
            ) {
                val byteResponse = characteristic.value
                //val miByte = BigInteger(byteArrayOf(byteResponse[6], byteResponse[5], byteResponse[4], byteResponse[3]))
                val año = BigInteger(byteArrayOf(byteResponse[8], byteResponse[7]))
                val month = Month.of(byteResponse[9].toInt())
                val dia = byteResponse[10]
                val hour = byteResponse[11]
                val minute = byteResponse[12]

                val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm")
                realDate = LocalDateTime.of(año.toInt(), month, dia.toInt(), hour.toInt(), minute.toInt(), 0)
                Log.e("TIMER", "Real timestamp: %s".format(realDate.format(dtf)))
                val requested = localDate.atZone(ZoneId.of("Europe/Madrid")).toInstant().toEpochMilli()
                val obtained = realDate.atZone(ZoneId.of("Europe/Madrid")).toInstant().toEpochMilli()
                val difference = requested - obtained

                if (difference < 3*3600000) {
                    startPreviousData(gatt, characteristic)
                } else {
                    val byteArray = byteArrayOf(0x03)
                    characteristic.value = byteArray
                    gatt.writeCharacteristic(characteristic)
                }
            } else {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
            if (characteristic.uuid == UUID_PREVIOUS_ACTIVITY_DATA) {
                broadcastUpdate(ACTION_PREVIOUS_DATA, characteristic)
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.i("BLE SERVICE","ENTRA EN ONDESCRIPTOR WRITE")
            /*characteristics.removeAt(0)
            if (gatt != null) {
                subscribeToCharacteristics(gatt)
            }*/
            var characteristic = characteristics2[0]
            var index = 0
            if (characteristic.uuid == UUID_PREVIOUS_ACTIVITY_DATA_2 && characteristics2.size > 1) {
                characteristic = characteristics2[1]
                index = 1
            }

            if (characteristic.uuid == UUID_PREVIOUS_ACTIVITY_DATA_2) {
                characteristics.add(characteristics2[0])
                if (gatt != null) {
                    val timer = Timer()
                    task = object : TimerTask() {
                        override fun run() {
                            Log.e("TIMER", LocalTime.now().toString())
                            writeActivityData(gatt)
                        }
                    }
                    //Envio de datos cada hora
                    timer.scheduleAtFixedRate(task, TimeUnit.MINUTES.toMillis(0), TimeUnit.MINUTES.toMillis(15))
                }
            }
            characteristics2.removeAt(index)

            if (gatt != null) {
                subscribePreviousActivity(gatt)
            }
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)
        Log.i(TAG, "Broadcast update entra: " + characteristic.uuid.toString())
        val dbHelperS = MyOpenHelper(this)
        dbS = dbHelperS.writableDatabase
        when (characteristic.uuid) {
            UUID_HEART_RATE_MEASUREMENT_XIAOMI -> {
                Log.i(TAG, "Broadcast update entra XIAOMI HEART RATE")
                var flag = characteristic.properties
                var format = when (flag and 0x01) {
                    0x01 -> {
                        Log.i(TAG, "Heart rate format UINT16.")
                        BluetoothGattCharacteristic.FORMAT_UINT16
                    }
                    else -> {
                        Log.i(TAG, "Heart rate format UINT8.")
                        BluetoothGattCharacteristic.FORMAT_UINT8
                    }
                }
                var heartRate = characteristic.getIntValue(format, 1)

                listHearRate.add(heartRate.toString())
                dbS!!.execSQL("INSERT INTO heartRates (lpm) VALUES('" + heartRate.toString() + "')")

                if (esPrimera) {
                    Log.i(TAG, "ES PRIMERA")
                    inicio = System.currentTimeMillis()
                    esPrimera = false
                } else {
                    fin = System.currentTimeMillis()
                    Log.i(TAG, "TIEMPO FIN: " + fin)
                    var dif = fin - inicio
                    if(dif >= 300000L || (dif in 270001L..300000L)) {
                        Log.i(TAG, "HAN PASADO 5 MINUTOS")
                        inicio = fin
                        //esPrimera = true
                        var meanHR:String = "0"
                        var deviationHR: String = "0"
                        var max = 0
                        var min = 0
                        var devHR = 0.0
                        var stepsV: String = "0"
                        var caloriesV: String = "0"
                        if(listHearRate.size > 0) {
                            Log.i(TAG, "HEART RATE Array List: " + listHearRate.get(0))
                            var sum = 0
                            for (element in listHearRate) {
                                Log.i(TAG, "ELEMENT: " + element)
                                sum += element.toInt()
                                if (element.toInt() > max) {
                                    max = element.toInt()
                                    Log.i(TAG, "MAX: " + max)
                                }
                                if (min == 0 || element.toInt() < min) {
                                    min = element.toInt()
                                    Log.i(TAG, "MIN: " + min)
                                }
                            }
                            Log.i(TAG, "SUM: " + sum)
                            Log.i(TAG, "SIZE: " + listHearRate.size)
                            meanHR = Math.round(sum.toDouble() / listHearRate.size.toDouble()).toInt().toString()
                            Log.i(TAG, "MEAN: " + meanHR)

                            var rango: Double
                            for (i in 0..listHearRate.size-1) {
                                rango = Math.pow(listHearRate.get(i).toDouble() - (sum.toDouble()/listHearRate.size.toDouble()), 2.0)
                                devHR = devHR + rango
                            }
                            Log.i("HANDLER", "devHR: " + devHR.toString())
                            devHR = devHR / listHearRate.size
                            Log.i("HANDLER", "devHR 2: " + devHR.toString())
                            val df = DecimalFormat("#.00")
                            deviationHR = df.format(Math.sqrt(devHR)).toString()
                            if(deviationHR.startsWith(",")) {
                                deviationHR = "0" + deviationHR
                            }
                            listHearRate.clear()
                        }

                        if (listSteps.size > 0) {
                            var lastIdx: Int = listSteps.size - 1
                            var lastStep = listSteps.get(lastIdx)
                            Log.i(TAG, "STEPS primero: " + listSteps.get(0) + " y ultimo: " + listSteps.get(lastIdx))
                            stepsV = (lastStep.toInt() - listSteps.get(0).toInt()).toString()
                            if ((lastStep.toInt() - listSteps.get(0).toInt()) < 0) { //Cambio de dia
                                for (i in lastIdx downTo 0) {
                                    if((listSteps.get(i).toInt() - listSteps.get(0).toInt()) >= 0) {
                                        stepsV = (listSteps.get(i).toInt() - listSteps.get(0).toInt() + listSteps.get(lastIdx).toInt()).toString()
                                    }
                                }
                            }
                            Log.i(TAG, "STEPS: : " + stepsV)
                            listSteps.clear()
                            listSteps.add(lastStep)
                        }

                        if (listCalories.size > 0) {
                            var lastIdx: Int = listCalories.size - 1
                            var lastCalorie = listCalories.get(lastIdx)
                            Log.i("HANDLER", "CALORIES primero: " + listCalories.get(0) + "  y ultimo: " + lastCalorie)
                            caloriesV = (lastCalorie.toInt() - listCalories.get(0).toInt()).toString()
                            if ((lastCalorie.toInt() - listCalories.get(0).toInt()) < 0) { //Cambio de dia
                                for (i in lastIdx downTo 0) {
                                    if((listCalories.get(i).toInt() - listCalories.get(0).toInt()) >= 0) {
                                        caloriesV = (listCalories.get(i).toInt() - listCalories.get(0).toInt() + listCalories.get(lastIdx).toInt()).toString()
                                    }
                                }
                            }
                            Log.i(TAG, "CALORIES: : " + caloriesV)
                            listCalories.clear()
                            listCalories.add(lastCalorie)
                        }

                        dbS!!.execSQL("INSERT INTO activityData (lpmAvg, devHR, maxHR, minHR, steps, calories)" +
                                "VALUES ('"+ meanHR + "', ' " + deviationHR + "', '" + max.toString() + "', '" + min.toString() + "', '" + stepsV + "', '" + caloriesV + "')")
                    }
                }

                Log.i(TAG, "TIEMPO INICIO " + inicio.toString())
                Log.i(TAG, String.format("Received heart rate: %d", heartRate))
                intent.putExtra("heart_rate", (heartRate).toString())
            }
            UUID_ACTIVITY_MEASUREMENT_XIAOMI -> {
                Log.i(TAG, "Broadcast update entra XIAOMI Activity")
                var value = characteristic.value
                var steps = byteArrayOf(value.get(4), value.get(3), value.get(2), value.get(1))
                var iSteps = BigInteger(steps)
                var strSteps: String = iSteps.toString()
                dbS!!.execSQL("INSERT INTO steps_tb (steps) VALUES('" + strSteps + "')")
                listSteps.add(strSteps)
                intent.putExtra("steps", strSteps)
                var calories = byteArrayOf(value.get(12), value.get(11), value.get(10), value.get(9))
                var iCalories = BigInteger(calories)
                var strCalories: String = iCalories.toString()
                dbS!!.execSQL("INSERT INTO calories_tb (calories) VALUES('" + strCalories + "')")
                listCalories.add(strCalories)
                intent.putExtra("calories", strCalories)
                Log.i(TAG, "Broadcast update entra XIAOMI STEPS: " + strSteps)
                Log.i(TAG, "Broadcast update entra XIAOMI CALORIES: " + strCalories)
            }
            UUID_PREVIOUS_ACTIVITY_DATA -> {
                val pastActivity = characteristic.value
                val packageIndex = pastActivity[0]
                val numRegistros = (pastActivity.size - 1)/8
                Log.e("TIMER", "Num regis: %s".format(numRegistros))
                var lastDate = realDate

                if (lastIndex != packageIndex.toInt()) {
                    lastIndex = packageIndex.toInt()
                    for (i in 0 until numRegistros) {
                        val activity_type = pastActivity[1+8*i]
                        val intensityV = pastActivity[2+8*i]
                        val stepsV = pastActivity[3+8*i].toInt().and(0xFF)
                        val heartRateV = pastActivity[4+8*i].toInt().and(0xFF)
                        val unk1 = pastActivity[5+8*i].toInt().and(0xFF)
                        val unk2 = pastActivity[6+8*i].toInt().and(0xFF)
                        val unk3 = pastActivity[7+8*i].toInt().and(0xFF)
                        val unk4 = pastActivity[8+8*i].toInt().and(0xFF)

                        val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd_MM_yyyy HH:mm")
                        var timestamp = realDate.plusMinutes((i).toLong())
                        lastDate = timestamp
                        val millis = lastDate.atZone(ZoneId.of("Europe/Madrid")).toInstant().toEpochMilli()

                        dbS!!.execSQL("INSERT INTO previousData (timestamp, DATETIME, activityType, intensity, steps, heartRate, unknow1, unknow2, unknow3, unknow4) " +
                                "VALUES('" + millis + "', '" + timestamp.format(dtf) + "', '" + activity_type + "', '" + intensityV + "', '" + stepsV + "', '" + heartRateV + "', '" + unk1 + "', '" + unk2 + "', '" + unk3 + "', '" + unk4 + "')")
                    }
                }
            }
            else -> {
                val data: ByteArray? = characteristic.value
                if (data?.isNotEmpty() == true) {
                    val hexString: String = data.joinToString(separator = " ") {
                        String.format("%02X", it)
                    }
                    intent.putExtra(EXTRA_DATA, "$data\n$hexString")
                }
            }
        }
        //dbHelperS.close()
        sendBroadcast(intent)
    }

    private fun loopGattServices (gatt: BluetoothGatt) {
        var gattServices: List<BluetoothGattService>? = gatt.getServices()
        if (gattServices == null) return
        var uuid: String?
        // Loops through available GATT Services
        gattServices.forEach { gattService ->
            uuid = gattService.uuid.toString()
            Log.i(TAG, "UUID Service: " + uuid)
            val gattCharacteristics = gattService.characteristics
            // Loops through available Characteristics
            gattCharacteristics.forEach { gattCharacteristic ->
                uuid = gattCharacteristic.uuid.toString()
                Log.i(TAG, "UUID Characteristic: " + uuid)
                if (uuid == UUID_HEART_RATE_MEASUREMENT_XIAOMI.toString() ||
                    uuid == UUID_ACTIVITY_MEASUREMENT_XIAOMI.toString()||
                    uuid == UUID_MEASUREMENT_FITBIT.toString()) {
                    characteristics2.add(gattCharacteristic)
                }

                if (uuid == UUID_PREVIOUS_ACTIVITY_DATA.toString()||
                    uuid == UUID_PREVIOUS_ACTIVITY_DATA_2.toString()) {
                    characteristics2.add(gattCharacteristic)
                }
            }
        }
        subscribePreviousActivity(gatt)
        //subscribeToCharacteristics(gatt)
    }

    private fun subscribePreviousActivity(gatt: BluetoothGatt) {
        if (characteristics2.size == 0) return

        var chara: BluetoothGattCharacteristic = characteristics2[0]
        if (chara.uuid == UUID_PREVIOUS_ACTIVITY_DATA_2 && characteristics2.size > 1) {
            chara = characteristics2[1]
        }

        if (chara.uuid == UUID_PREVIOUS_ACTIVITY_DATA_2) {
            chara.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            gatt.setCharacteristicNotification(chara, true)
        } else {
            chara.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            gatt.setCharacteristicNotification(chara, true)
        }
        var descriptor = chara.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG).apply {
            value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        }
        gatt.writeDescriptor(descriptor)
    }

    private fun writeActivityData(gatt: BluetoothGatt) {
        val dbHelperS = MyOpenHelper(this)
        dbS = dbHelperS.writableDatabase

        val chara: BluetoothGattCharacteristic = characteristics[0]
        localDate = LocalDateTime.now().minusHours(1)
        var stringAño = localDate.year
        var año = localDate.year.toBigInteger().toByteArray()
        var mes = localDate.monthValue.toByte()
        var dia = localDate.dayOfMonth
        var hora = localDate.hour
        var minuto = localDate.minute

        val c: Cursor? = dbS!!.rawQuery("SELECT DATETIME, timestamp FROM previousData WHERE timestamp = (SELECT max(timestamp) FROM previousData)", null)
        try {
            if (c != null) {
                if (c.count > 0) {
                    c.moveToFirst()
                    do {
                        lastValue = c.getString(0)
                        if (lastValue != null) {
                            val lastValueSplit = lastValue!!.split("_", " ", ":")
                            stringAño = lastValueSplit[2].toInt()
                            año = stringAño.toBigInteger().toByteArray()
                            mes = lastValueSplit[1].toInt().toByte()
                            dia = lastValueSplit[0].toInt()
                            hora = lastValueSplit[3].toInt()
                            minuto = lastValueSplit[4].toInt() + 1
                            localDate = Instant.ofEpochMilli(c.getString(1).toLong()).atZone(ZoneId.of("Europe/Madrid")).toLocalDateTime()
                            lastValue = null
                        }
                    } while (c.moveToNext())
                }
            }


        } catch (e: SQLException){
            Log.e("Unhandled exception", e.toString())
        }

        Log.i("ACTIVITY", "Requested timestamp: %s-%s-%s %s:%s".format(dia, mes, stringAño, hora, minuto))
        val byteArray = byteArrayOf(
            0x01, 0x01, año[1], año[0], mes, dia.toByte(), hora.toByte(), minuto.toByte(), 0x00, 0x08)
        // val first = BigInteger(byteArrayOf(byteArray[3], byteArray[2]))
        // val sec = byteArray[0].toInt().and(0xFF)
        chara.value = byteArray
        gatt.writeCharacteristic(chara)
    }

    private fun startPreviousData (gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val byteArray = byteArrayOf(0x02)
        characteristic.value = byteArray
        lastIndex = -1
        gatt.writeCharacteristic(characteristic)
    }

    private fun subscribeToCharacteristics(gatt: BluetoothGatt) {
        Log.i(TAG,"Entra en subscribe to characteristics")
        Log.i(TAG,"Characteristics size: " + characteristics.size)
        if (characteristics.size == 0) return

        val chara: BluetoothGattCharacteristic = characteristics.get(0)
        gatt.setCharacteristicNotification(chara, true)
        chara.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        gatt.setCharacteristicNotification(chara, true)
        var descriptor = chara.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG).apply {
            value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        }
        gatt.writeDescriptor(descriptor)
    }

    inner class LocalBinder : Binder() {
        internal val service: BluetoothLeService
            get() = this@BluetoothLeService
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        //Cleans resources
        close()
        return super.onUnbind(intent)
    }

    private val mBinder = LocalBinder()

    fun initialize(): Boolean {
        if (bluetoothManager == null) {
            bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (bluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
                return false
            }
        }
        bluetoothAdapter = bluetoothManager!!.adapter
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }

        return true
    }

    fun connect(address: String?): Boolean {
        if (bluetoothAdapter == null || address == null || address.equals("null") || address.equals("")) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
            return false
        }

        // Previously connected device
        if (bluetoothDeviceAddress != null && address == bluetoothDeviceAddress && bluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.")
            if (bluetoothGatt!!.connect()) {
                connectionState = STATE_CONNECTING
                return true
            } else {
                return false
            }
        }

        val device = bluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return false
        }

        bluetoothGatt = device.connectGatt(this, false, gattCallback)
        Log.d(TAG, "Trying to create a new connection.")
        bluetoothDeviceAddress = address
        connectionState = STATE_CONNECTING

        return true
    }

    fun close() {
        Log.i("BLE SERV", "entra en close")
        if (bluetoothGatt == null)
            return
        bluetoothGatt!!.close()
        bluetoothGatt = null
        esPrimera = true
        inicio = 0
        fin = 0
    }

}




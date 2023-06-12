package com.example.mealrecognition


import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mealrecognition.upload.*
import com.example.mealrecognition.upload.receivers.UploadResponse
import com.example.mealrecognition.upload.uploaders.UploadFileBody
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

private const val TAG = "AppBLE:::BLEService"

class DataUploader : Service() {
    val NOTIFICATION_CHANNEL_ID = "example.permanence"
    private var idArray : ArrayList<Int> = ArrayList<Int>()
    private var idArray2 : ArrayList<Int> = ArrayList<Int>()
    private var idArray3 : ArrayList<Int> = ArrayList<Int>()

    private lateinit var db: SQLiteDatabase
    val dbHelper = MyOpenHelper(this)
    var task: TimerTask? = null

    override fun onCreate() {
        Log.d(TAG, "Servicio creado...")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startMyOwnForeground() else startForeground(
            1,
            Notification()
        )
        sendActivityData()
    }

    fun sendActivityData() {
        db = dbHelper.writableDatabase
        Log.e("NSClientService", "ENTRA EN sendActivityDataNS")
        if (task != null) return //Solo crear un hilo
        val timer = Timer()

        task = object : TimerTask() {
            @SuppressLint("Range")
            override fun run() {
                val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm")
                val now: LocalDateTime = LocalDateTime.now()
                val date: String = dtf.format(now).toString()

                try {
                    checkPreviousFiles()

                    val c1: Cursor? = db.rawQuery("SELECT * FROM activityData WHERE isSent='0'", null)

                    if (c1 != null) {

                        if((c1.getCount() > 0)) {

                            val valuesActivity = Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS).toString() + "/activity_%s.json".format(date)
                            val file = File(valuesActivity)
                            if (!file.exists()) {
                                file.createNewFile()
                            }

                            val fileWriter = FileWriter(file)
                            val bufferedWriter = BufferedWriter(fileWriter)
                            val message = JSONArray()//Hay datos de glucosa

                            c1.moveToFirst()
                            do {
                                val data = JSONObject()
                                data.put("timestamp", c1.getString(c1.getColumnIndex("DATETIME")))
                                data.put("lpmAvg", c1.getString(c1.getColumnIndex("lpmAvg")))
                                data.put("devHR", c1.getString(c1.getColumnIndex("devHR")))
                                data.put("maxHR", c1.getString(c1.getColumnIndex("maxHR")))
                                data.put("minHR", c1.getString(c1.getColumnIndex("minHR")))
                                data.put("steps", c1.getString(c1.getColumnIndex("steps")))
                                data.put("calories", c1.getString(c1.getColumnIndex("calories")))
                                /*Log.e("SENDACTDB", "{" + "\"heartRate\": " + c1.getString(c1.getColumnIndex("lpm")) + "}," + "\n"
                                    + "\"timestamp\": " + c1.getString(c1.getColumnIndex("DATETIME")) + "}," + "\n")*/
                                message.put(data)
                                idArray.add(c1.getInt(c1.getColumnIndex("_id")))
                                db.execSQL("UPDATE activityData SET isSent='1' WHERE _id=" + c1.getInt(c1.getColumnIndex("_id")))
                            } while (c1.moveToNext())

                            bufferedWriter.write(message.toString())
                            Thread.sleep(10)
                            bufferedWriter.close()

                            uploadActivityFile(file, 0)
                        }
                    }
                    c1!!.close()

                    val c2: Cursor? = db.rawQuery("SELECT * FROM previousData WHERE isSent='0'", null)

                    if (c2 != null && (c2.getCount() > 0)) {

                        try {
                            val prevActivityValues = Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS).toString() + "/previous_%s.json".format(date)
                            val prevFile = File(prevActivityValues)
                            if (!prevFile.exists()) {
                                prevFile.createNewFile()
                            }

                            val fileWriter2 = FileWriter(prevFile)
                            val bufferedWriter2 = BufferedWriter(fileWriter2)
                            val prevDataArray = JSONArray()

                            c2.moveToFirst()
                            do {
                                val prevData = JSONObject()
                                prevData.put("timestamp", c2.getString(c2.getColumnIndex("DATETIME")))
                                prevData.put("activityType", c2.getString(c2.getColumnIndex("activityType")))
                                prevData.put("intensity", c2.getString(c2.getColumnIndex("intensity")))
                                prevData.put("steps", c2.getString(c2.getColumnIndex("steps")))
                                prevData.put("heartRate", c2.getString(c2.getColumnIndex("heartRate")))
                                prevData.put("unk1", c2.getString(c2.getColumnIndex("unknow1")))
                                prevData.put("unk2", c2.getString(c2.getColumnIndex("unknow2")))
                                prevData.put("unk3", c2.getString(c2.getColumnIndex("unknow3")))
                                prevData.put("unk4", c2.getString(c2.getColumnIndex("unknow4")))

                                prevDataArray.put(prevData)
                                idArray2.add(c2.getInt(c2.getColumnIndex("_id")))
                                db.execSQL("UPDATE previousData SET isSent='1' WHERE _id=" + c2.getInt(c2.getColumnIndex("_id")))
                            } while (c2.moveToNext())

                            bufferedWriter2.write(prevDataArray.toString())
                            Thread.sleep(10)
                            bufferedWriter2.close()

                            uploadActivityFile(prevFile, 1)
                        }
                        catch (e: Exception) {
                            Log.e("DATABASE", "Not readable database")
                        }
                    }
                    c2!!.close()

                    val c3: Cursor? = db.rawQuery("SELECT * FROM heartRates WHERE isSent='0'", null)
                    if (c3 != null) {

                        if((c3.getCount() > 0)) {

                            val valuesActivity = Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS).toString() + "/hr_%s.json".format(date)
                            val file = File(valuesActivity)
                            if (!file.exists()) {
                                file.createNewFile()
                            }

                            val fileWriter = FileWriter(file)
                            val bufferedWriter = BufferedWriter(fileWriter)
                            val message = JSONArray()//Hay datos de glucosa

                            c3.moveToFirst()
                            do {
                                val data = JSONObject()
                                data.put("timestamp", c3.getString(c3.getColumnIndex("DATETIME")))
                                data.put("bpm", c3.getString(c3.getColumnIndex("lpm")))
                                message.put(data)
                                idArray3.add(c3.getInt(c3.getColumnIndex("_id")))
                                db.execSQL("UPDATE heartRates SET isSent='1' WHERE _id=" + c3.getInt(c3.getColumnIndex("_id")))
                            } while (c3.moveToNext())

                            bufferedWriter.write(message.toString())
                            Thread.sleep(10)
                            bufferedWriter.close()

                            uploadActivityFile(file, 2)
                        }
                    }
                    c3!!.close()

                    try {
                        //BORRADO DE DATOS DE LA BBDD LOCAL CADA 7 DIAS
                        db.execSQL("DELETE FROM activityData WHERE DATETIME < datetime('" + date + "','-6 days') AND isSent='1'")
                        db.execSQL("DELETE FROM heartRates WHERE DATETIME < datetime('" + date + "','-6 days') AND isSent='1'")
                        db.execSQL("DELETE FROM steps_tb WHERE DATETIME < datetime('" + date + "','-6 days')")
                        db.execSQL("DELETE FROM calories_tb WHERE DATETIME < datetime('" + date + "','-6 days')")
                        db.execSQL("DELETE FROM previousData WHERE DATETIME < datetime('" + date + "','-6 days') AND isSent='1'")

                    } catch (e : Exception) {
                        Log.e("TAG", "No hay 7 dias de datos")
                    }
                } catch (e: JSONException) {
                    Log.e("Unhandled exception", e.toString())
                }
            }
        }
        //Envio de datos cada hora
        timer.scheduleAtFixedRate(task, TimeUnit.MINUTES.toMillis(60), TimeUnit.MINUTES.toMillis(120))
    }

    private fun checkPreviousFiles() {
        /**val checkFiles = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS).toString()
        val prevFiles = File(checkFiles)
        val downloadedFiles = prevFiles.list()
        val previousFiles = downloadedFiles.filter {
        it.contains("previous")
        }
        if (previousFiles.isNotEmpty()) {
        for (i in previousFiles.indices) {
        val prevActivity = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS).toString() + "/%s.json".format(i)
        val file = File(prevActivity)
        uploadActivityFile(file, 1)
        }
        }
        val activityFiles = downloadedFiles.filter {
        it.contains("activity")
        }
        if (activityFiles.isNotEmpty()) {
        for (i in activityFiles.indices) {
        val prevActivity = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS).toString() + "/%s.json".format(i)
        val file = File(prevActivity)
        uploadActivityFile(file, 0)
        }
        }
        val hrFiles = downloadedFiles.filter {
        it.contains("hr")
        }
        if (hrFiles.isNotEmpty()) {
        for (i in hrFiles.indices) {
        val prevActivity = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS).toString() + "/%s.json".format(i)
        val file = File(prevActivity)
        uploadActivityFile(file, 2)
        }
        }*/
    }

    private fun uploadActivityFile(file : File, prevActivity : Int) {
        val body = UploadFileBody(file, "image")
        val prefs = this.getSharedPreferences("id_pref", MODE_PRIVATE)
        val patient_id = prefs?.getInt("id", 0)


        FileAPI().uploadFile(
            MultipartBody.Part.createFormData("image", file.name, body),
            RequestBody.create(MediaType.parse("multipart/form-data"), patient_id.toString())
        ).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                response.body()?.let {
                    Log.e("UploaderService", "Subido correctamente")
                    if (prevActivity == 0)
                        idArray.clear()
                    if (prevActivity == 1)
                        idArray2.clear()
                    if (prevActivity == 2)
                        idArray3.clear()
                    file.delete()
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                if (prevActivity == 0) {
                    for (i in 0 until idArray.size) {
                        db.execSQL("UPDATE activityData SET isSent='0' WHERE _id=" + idArray[i])
                    }
                    idArray.clear()
                }
                if (prevActivity == 1) {
                    for (i in 0 until idArray2.size) {
                        db.execSQL("UPDATE previousData SET isSent='0' WHERE _id=" + idArray2[i])
                    }
                    idArray2.clear()
                }
                if (prevActivity == 2) {
                    for (i in 0 until idArray3.size) {
                        db.execSQL("UPDATE heartRates SET isSent='0' WHERE _id=" + idArray3[i])
                    }
                    idArray3.clear()
                }
                file.delete()

                val builder = NotificationCompat.Builder(this@DataUploader, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.upm)
                    .setContentTitle("Error de servidor")
                    .setContentText("No se ha podido subir el archivo, se almacenará localmente")
                    .setStyle(NotificationCompat.BigTextStyle()
                        .bigText("No se han podido subir el archivo, se almacenará localmente en la carpeta de descargas"))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)

                with(NotificationManagerCompat.from(this@DataUploader)) {
                    notify(3, builder.build())
                }
            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
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

    inner class LocalBinder : Binder() {
        internal val service: DataUploader
            get() = this@DataUploader
    }

    private val mBinder = LocalBinder()

    override fun onBind(p0: Intent?): IBinder {
        return mBinder
    }
}
package com.example.mealrecognition

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.mealrecognition.databinding.FragmentCamBinding
import com.example.mealrecognition.upload.*
import com.example.mealrecognition.upload.receivers.*
import com.example.mealrecognition.upload.uploaders.ConfirmationRequest
import com.example.mealrecognition.upload.uploaders.UploadRequestBody
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*


class CamFragment : Fragment() {
    private lateinit var _binding: FragmentCamBinding
    private val binding get() = _binding

    lateinit var photo_view : ImageView
    lateinit var upload_bt : Button
    lateinit var repeat_bt : Button
    lateinit var progress_bar : ProgressBar
    lateinit var buttonsView : LinearLayout
    lateinit var image_uri : Uri
    lateinit var ch_text : EditText
    lateinit var autocomplete: AutoCompleteTextView
    lateinit var requestLauncher : ActivityResultLauncher<Intent>

    private var arrayAdapter: ArrayAdapter<String>? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCamBinding.inflate(inflater, container, false)
        val root: View = binding.root

        photo_view = binding.imageView
        upload_bt = binding.uploadBt
        repeat_bt = binding.repeatBt
        progress_bar = binding.progressBar
        buttonsView = binding.linearLayout
        autocomplete = binding.detailedQuantityDropdown

        ch_text = binding.ch




        val quantityList = arrayListOf<String>()
        val adapter = activity?.applicationContext?.let {
            ArrayAdapter(
                it, R.layout.spinner_default,
                quantityList
            )
        }
        quantityList.add("Desayuno")
        quantityList.add("Almuerzo")
        quantityList.add("Snack")
        quantityList.add("Cena")

        autocomplete.setAdapter(adapter)



        initiateActivityResult()
        requestPermission()



        upload_bt.setOnClickListener {
            if (::image_uri.isInitialized) {
                if (image_uri != "".toUri()) {
                    if (ch_text.text.isEmpty()) {
                        Toast.makeText(activity, "Debe introducir un valor de Carbohidratos", Toast.LENGTH_LONG).show()
                    }
                    else if ( autocomplete.text.isEmpty()) {
                        Toast.makeText(activity, "Debe elegir el momento de su ingesta", Toast.LENGTH_LONG).show()
                    }
                    else{

                        uploadSegmentation(image_uri)
                        Log.e("TAG", ch_text.text.toString())
                        Log.e("TAG", autocomplete.text.toString())

                    }
                }
                else
                    Toast.makeText(activity, "La foto seleccionada ya ha sido subida", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(activity, "Haga una foto para subirla", Toast.LENGTH_LONG).show()
            }

        }
        repeat_bt.setOnClickListener {
            requestLauncher.launch(Intent(activity, CameraActivity::class.java))
        }
        return root
    }

    private fun initiateActivityResult() {
        requestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                image_uri = result.data?.data!!
                photo_view.setImageURI(image_uri)

            }
        }
    }



    private fun requestPermission() {
        requestCameraPermissionIfMissing { granted ->
            if (granted)
                requestLauncher.launch(Intent(activity, CameraActivity::class.java))
            else
                Toast.makeText(activity, "Por favor acepte el permiso", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestCameraPermissionIfMissing(onResult: (Boolean) -> Unit) {
        if (activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } == PackageManager.PERMISSION_GRANTED)
            onResult(true)
        else
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                onResult(it)
            }.launch(Manifest.permission.CAMERA)
    }

    private fun uploadImage(uriFile : Uri) {
        val parcelFileDescriptor = activity?.contentResolver?.openFileDescriptor(uriFile, "rw", null)
            ?: return
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(activity?.cacheDir, activity?.contentResolver!!.getFileName(uriFile))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        progress_bar.progress = 0
        val body = UploadRequestBody(file, "image", this)
        val prefs = activity?.getSharedPreferences("id_pref", AppCompatActivity.MODE_PRIVATE)
        val patient_id = prefs?.getInt("id", 0)

        PhotoAPI().uploadImage(
            MultipartBody.Part.createFormData("image", file.name, body),
            RequestBody.create(MediaType.parse("multipart/form-data"), patient_id.toString())
        ).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                response.body()?.let {
                    photo_view.snackbar(it.message)
                    progress_bar.progress = 100
                    inputStream.close()
                    outputStream.close()

                    val delete_file = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "/Prophecy_Imagenes/%s".format(activity?.contentResolver!!.getFileName(uriFile)))
                    Thread.sleep(100)
                    uploadSegmentation(uriFile)
                    //delete_file.delete()
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                photo_view.snackbar("No es posible subir la imagen, guardada en la galería")
                progress_bar.progress = 0
            }

        })
    }



    private fun uploadSegmentation(uriFile : Uri) {
        val parcelFileDescriptor = activity?.contentResolver?.openFileDescriptor(uriFile, "rw", null)
            ?: return
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(activity?.cacheDir, activity?.contentResolver!!.getFileName(uriFile))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        progress_bar.progress = 0
        val body = UploadRequestBody(file, "image", this)
        val sharedPrefToken = activity?.getSharedPreferences("token_user", Context.MODE_PRIVATE)
        val token = "Bearer " + sharedPrefToken?.getString("token", null)
        LogmealAPI.invoke()
        if (token != null) {
            LogmealAPI().dishesDetection(
                token,
                MultipartBody.Part.createFormData(
                    "image",
                    file.name,
                    body
                )
            ).enqueue(object : Callback<SegmentationResponse> {
                override fun onFailure(call: Call<SegmentationResponse>, t: Throwable) {
                    photo_view.snackbar("No es posible segmentar la imagen")
                    progress_bar.progress = 0
                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<SegmentationResponse>,
                    response: Response<SegmentationResponse>
                ) {
                    response.body()?.let {
                        val imageId = it.imageId
                        val occasion = it.occasion
                        val foodType = JSONObject(Gson().toJson(it.foodType))
                        val foodFamily = JSONArray(Gson().toJson(it.foodFamily))
                        val segmentation_results = JSONArray(Gson().toJson(it.segmentation_results))

                        val response_data = JSONObject()
                        response_data.put("imageId", imageId)
                        response_data.put("occasion", occasion)
                        response_data.put("foodType", foodType)
                        response_data.put("foodFamily", foodFamily)
                        response_data.put("segmentation_results", segmentation_results)

                        sendSegmentation(response_data, uriFile)


                    }
                }
            })
        }

    }






    fun onProgressUpdate(percentage: Int) {
        progress_bar.progress = percentage
    }

    override fun onDestroy() {
        super.onDestroy()
       // buttonsView.removeAllViews()
        progress_bar.progress = 0
    }


    private fun sendSegmentation(obj: JSONObject, uriFile: Uri) {
        val intent = Intent(activity, RecognitionActivity::class.java)
        val ch = ch_text.text.toString()


        autocomplete.setOnItemClickListener { parent, _, position, _ ->
            parent.adapter.getItem(position) as String
        }
        val enteredText = autocomplete.text.toString()

        intent.putExtra("image", uriFile)
        intent.putExtra("meal", enteredText)
        intent.putExtra("carb", ch)
         val json = obj.toString()
        intent.putExtra("json", json)
        startActivity(intent)


    }




}


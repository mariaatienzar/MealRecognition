package com.example.mealrecognition

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import com.example.mealrecognition.upload.getFileName
import com.example.mealrecognition.upload.receivers.*
import com.example.mealrecognition.upload.snackbar
import com.example.mealrecognition.upload.uploaders.ConfirmationRequest
import com.example.mealrecognition.upload.uploaders.NutritionRequest
import com.example.mealrecognition.upload.uploaders.QuantityRequest
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
        buttonsView = binding.buttonsView

        initiateActivityResult()
        requestPermission()




        upload_bt.setOnClickListener {
            if (::image_uri.isInitialized) {
                if (image_uri != "".toUri()) {
                    uploadImage(image_uri)

                }
                else
                    Toast.makeText(activity, "La foto seleccionada ya ha sido subida", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(activity, "Haga una foto para subirla", Toast.LENGTH_LONG).show()
            }
        }
        repeat_bt.setOnClickListener {
            buttonsView.removeAllViews()
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
                Toast.makeText(activity, "Please allow the permission", Toast.LENGTH_LONG).show()
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
                    //image_uri.path?.let { it1 -> openListRecipe(it1) }
                   // image_uri = "".toUri()

                    val delete_file = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "/Pictures/%s".format(activity?.contentResolver!!.getFileName(uriFile)))
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
        LogmealAPI().dishesDetection(
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

                    sendSegmentation(response_data,uriFile)

                }
            }
        })

    }



    private fun obtainNutrients(imageId: Int) {
        val request = NutritionRequest(imageId.toString())

        progress_bar.progress = 0
        LogmealAPI().nutrientInformation(request).enqueue(object : Callback<NutrientResponse> {
            override fun onResponse(
                call: Call<NutrientResponse>,
                response: Response<NutrientResponse>
            ) {
                response.body()?.let {
                    val foodName = JSONArray(it.foodName)
                    val hasNutritionalInfo = it.hasNutritionalInfo
                    val ids = JSONArray(it.ids)
                    val imageId = it.imageId
                    val nutritional_info = JSONObject(Gson().toJson(it.nutritional_info))
                    val nutritional_info_per_item = JSONArray(Gson().toJson(it.nutritional_info_per_item))
                    val serving_size = it.serving_size

                    val response_data = JSONObject()
                    response_data.put("foodName", foodName)
                    response_data.put("hasNutritionalInfo", hasNutritionalInfo)
                    response_data.put("ids", ids)
                    response_data.put("imageId", imageId)
                    response_data.put("nutritional_info", nutritional_info)
                    response_data.put("nutritional_info_per_itemval", nutritional_info_per_item)
                    response_data.put("serving_size", serving_size)

                    val valuesActivity = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS).toString() + "/nutrientes.json"
                    val file = File(valuesActivity)
                    if (!file.exists()) {
                        file.createNewFile()
                    }

                    val fileWriter = FileWriter(file)
                    val bufferedWriter = BufferedWriter(fileWriter)
                    bufferedWriter.write(response_data.toString())
                    Thread.sleep(10)
                    bufferedWriter.close()

                }

            }

            override fun onFailure(call: Call<NutrientResponse>, t: Throwable) {
                photo_view.snackbar(t.message!!)
                progress_bar.progress = 0
            }

        })
    }


    private fun confirmQuantity(imageId: Int){
        val request = QuantityRequest(imageId.toString())
        progress_bar.progress = 0
        LogmealAPI().quantityDish(request).enqueue(object : Callback<QuantityResponse> {

            override fun onResponse(
                call: Call<QuantityResponse>,
                response: Response<QuantityResponse>
            ) {
                response.body()?.let {
                    progress_bar.progress = 100

                    val result = it.result

                    val response_data = JSONObject()
                    response_data.put("result", result)

                    val valuesActivity = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS).toString() + "/cantidad.json"
                    val file = File(valuesActivity)
                    if (!file.exists()) {
                        file.createNewFile()
                    }

                    val fileWriter = FileWriter(file)
                    val bufferedWriter = BufferedWriter(fileWriter)
                    bufferedWriter.write(response_data.toString())
                    Thread.sleep(10)
                    bufferedWriter.close()


                }
            }

            override fun onFailure(call: Call<QuantityResponse>, t: Throwable) {
                photo_view.snackbar(t.message!!)
                progress_bar.progress = 0
            }

        })
    }



    fun onProgressUpdate(percentage: Int) {
        progress_bar.progress = percentage
    }

    override fun onDestroy() {
        super.onDestroy()
        buttonsView.removeAllViews()
        progress_bar.progress = 0
    }


    private fun sendSegmentation(obj: JSONObject, uriFile: Uri) {
        val intent = Intent(activity, RecognitionActivity::class.java)
        intent.putExtra("image", uriFile)
        val json = obj.toString()
        intent.putExtra("json", json)
        startActivity(intent)
    }




}

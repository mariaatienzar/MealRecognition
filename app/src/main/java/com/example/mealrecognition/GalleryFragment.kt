package com.example.mealrecognition

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.mealrecognition.databinding.FragmentGalleryBinding
import com.example.mealrecognition.upload.LogmealAPI
import com.example.mealrecognition.upload.PhotoAPI
import com.example.mealrecognition.upload.getFileName
import com.example.mealrecognition.upload.receivers.SegmentationResponse
import com.example.mealrecognition.upload.receivers.UploadResponse
import com.example.mealrecognition.upload.snackbar
import com.example.mealrecognition.upload.uploaders.UploadRequestBody
import com.example.mealrecognition.upload.uploaders.UploadRequestBody2
import com.google.android.material.snackbar.Snackbar
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


class GalleryFragment : Fragment() {
    lateinit var SelectImage: ImageView
    //lateinit var IVPreviewImage: ImageView
    lateinit var BUpload: Button
    lateinit var selectedImageUri: Uri
    lateinit var requestLauncher: ActivityResultLauncher<Intent>
    lateinit var progress_bar: ProgressBar
    lateinit var layout_root: ConstraintLayout

    // constant to compare the activity result code
    var SELECT_PICTURE = 200
    private lateinit var _binding: FragmentGalleryBinding
    private val binding get() = _binding
    private val REQUEST_CODE_GALLERY = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        SelectImage = binding.imageView
        BUpload = binding.buttonUpload
        progress_bar = binding.progressBar
        layout_root= binding.layoutRoot
        //initiateActivityResult()


        SelectImage.setOnClickListener {
            opeinImageChooser()
        }


        BUpload.setOnClickListener {
            uploadImage()


        }
        return root

    }
    private fun uploadImage() {

        if (selectedImageUri == null) {
            layout_root.snackbar("Select an Image First")
            return
        }
        val targetPackage = "com.example.mealrecognition"
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context?.grantUriPermission(targetPackage, selectedImageUri, flags)


        val contentResolver = context?.contentResolver
        val parcelFileDescriptor = contentResolver?.openFileDescriptor(selectedImageUri, "r", null)
            ?: return
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(context?.cacheDir, contentResolver.getFileName(selectedImageUri!!))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        progress_bar.progress = 0
        val body = UploadRequestBody2(file,"image",this)

        PhotoAPI().uploadImage(MultipartBody.Part.createFormData(
            "image",
            file.name,
            body
        ),
            RequestBody.create(MediaType.parse("multipart/form-data"),"json")
        ).enqueue(object : Callback<UploadResponse>{
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                response.body()?.let {
                    layout_root.snackbar(it.message)
                    progress_bar.progress = 100
                    uploadSegmentation(selectedImageUri)
                    //selectedImageUri.path?.let { it -> openListMeal(it) }
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                layout_root.snackbar("No es posible subir la imagen, guardada en la galería")
                progress_bar.progress = 0
            }

        })


    }
    private fun uploadSegmentation(uriFile : Uri) {
        val parcelFileDescriptor = activity?.contentResolver?.openFileDescriptor(uriFile, "r", null)
            ?: return
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(activity?.cacheDir, activity?.contentResolver!!.getFileName(uriFile))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        progress_bar.progress = 0
        val body = UploadRequestBody2(file, "image", this)
        LogmealAPI().dishesDetection(
            MultipartBody.Part.createFormData(
                "image",
                file.name,
                body
            )
        ).enqueue(object : Callback<SegmentationResponse> {
            override fun onFailure(call: Call<SegmentationResponse>, t: Throwable) {
                SelectImage.snackbar("No es posible segmentar la imagen")
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



    private fun opeinImageChooser() {

        Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(it, REQUEST_CODE_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_IMAGE -> {
                    selectedImageUri = data?.data!!
                     // Decode the image file into a bitmap
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                        BitmapFactory.decodeStream(context?.contentResolver?.openInputStream(selectedImageUri), null, this)
                        val imageHeight = outHeight
                        val imageWidth = outWidth
                        val maxSize = 1024 // Set your desired max image size
                        val ratio = Math.min(imageWidth/maxSize, imageHeight/maxSize)
                        inSampleSize = ratio
                        inJustDecodeBounds = false
                    }
                    val bitmap = BitmapFactory.decodeStream(context?.contentResolver?.openInputStream(selectedImageUri), null,options )
                    SelectImage.setImageBitmap(bitmap)
                    //SelectImage.setImageURI(selectedImageUri)



                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_IMAGE = 101
    }

    fun onProgressUpdate(percentage: Int) {
        progress_bar.progress = percentage
    }


    private fun ContentResolver.getFileName(selectedImageUri: Uri): String {
        var name = ""
        val returnCursor = this.query(selectedImageUri,null,null,null,null)
        if (returnCursor!=null){
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }

        return name
    }
    private fun openListMeal(path: String) {
        val intent = Intent(activity, IntakeInfoActivity::class.java)
        intent.putExtra("Food Image", path)
        startActivity(intent)
    }

    private fun sendSegmentation(obj: JSONObject, uriFile: Uri) {
        val intent = Intent(activity, RecognitionActivity::class.java)
        intent.putExtra("image", uriFile)
        val json = obj.toString()
        intent.putExtra("json", json)
        startActivity(intent)
    }




}










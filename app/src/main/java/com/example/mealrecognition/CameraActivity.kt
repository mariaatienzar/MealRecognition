package com.example.mealrecognition

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.mealrecognition.databinding.ActivityCameraBinding
import com.example.mealrecognition.databinding.ActivityLoginBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraActivity : AppCompatActivity() {
    private lateinit var binding : ActivityCameraBinding

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture : ImageCapture
    private lateinit var button : FloatingActionButton
    private lateinit var backButton: ImageButton
    private lateinit var image_view : PreviewView

    companion object {
        private const val TAG = "mealRecognition"

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        image_view = binding.camImView
        button = binding.floatingActionButton2
        backButton = binding.buttonBack
        startCamera()

        backButton.setOnClickListener{
            startActivity(Intent(this,HomeActivity::class.java))
        }


        button.setOnClickListener {
            takePhoto()
        }
    }

    private fun startCamera() {
        val processCameraProvider = ProcessCameraProvider.getInstance(this)
        processCameraProvider.addListener({
            try {
                val cameraProvider = processCameraProvider.get()
                val previewUseCase = Preview.Builder().build()
                previewUseCase.setSurfaceProvider(image_view.surfaceProvider)
                previewUseCase.setSurfaceProvider(image_view.surfaceProvider)
                imageCapture = ImageCapture.Builder()
                    .setTargetResolution(Size(720, 720))
                    .setTargetRotation(image_view.display!!.rotation)
                    .build()

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, previewUseCase, imageCapture)
            } catch (e: Exception) {
                Log.e("ERROR", "Error al iniciar la cámara")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
         val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm")
        val now: LocalDateTime = LocalDateTime.now()
        val name: String = dtf.format(now).toString()

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/mealRecognition")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(this.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Fallo en la captura de fotos", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Foto capturada correctamente"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                    Intent("com.example.RESULT_ACTION", Uri.parse("content://result_uri")).also { result ->
                        result.data = output.savedUri
                        setResult(Activity.RESULT_OK, result)
                    }
                    finish()
                }
            }
        )
    }
}
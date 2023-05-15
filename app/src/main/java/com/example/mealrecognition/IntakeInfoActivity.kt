package com.example.mealrecognition


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import okhttp3.*

import java.io.File


class IntakeInfoActivity : AppCompatActivity() {
    //val recyclerView = findViewById<RecyclerView>(R.id.rv_recipe)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intake_info)
        //val adapter = PostAdapter()
        //recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //recyclerView.adapter = adapter



    }
}



/*
        // Load image

        val imgPath = "<>"
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        val bmp = BitmapFactory.decodeFile(imgPath, options)

// [IMPORTANT to keep image orientation] get exif information
        val exif = ExifInterface(imgPath)
        val exifBytes = exif.thumbnail ?: exif.getThumbnail()

// get the size of the image in MB
        var sizeMb = File(imgPath).length() / (1024 * 1024).toFloat()
        val width = bmp.width
        val height = bmp.height

// iteratively reduce the image a percentage of its size until it is smaller than 1MB
        while (sizeMb >= 1) {

            // resize the image 75%
            val matrix = Matrix()
            matrix.postScale(0.75f, 0.75f)
            val scaledBmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true)

            // save the resized image
            val path = "<path-of-resized-image>"
            File(path).outputStream().use { out ->
                scaledBmp.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            // get the size in MB
            sizeMb = File(path).length() / (1024 * 1024).toFloat()
        }


    }
}


*/




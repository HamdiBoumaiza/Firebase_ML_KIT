package app.hb.firebasetestml

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {


    val REQUEST_IMAGE_CAPTURE = 1
    val TAG = MainActivity::class.java.simpleName

    private var imageBitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initClickListeners()
    }

    private fun initClickListeners() {
        rbBarCode.setOnClickListener(this)
        rbText.setOnClickListener(this)
        rbBuildings.setOnClickListener(this)
        fabCamera.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.rbText -> if (imageBitmap != null) detectText(imageBitmap!!)
            R.id.rbBarCode -> if (imageBitmap != null) detectBarCode(imageBitmap!!)
            R.id.rbBuildings -> if (imageBitmap != null) detectBuilding(imageBitmap!!)
            R.id.fabCamera -> dispatchTakePictureIntent()
        }
    }


    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val extras = data.extras
            imageBitmap = extras.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)


            if (rbText.isChecked) {
                detectText(imageBitmap!!)

            } else if (rbBuildings.isChecked) {
                detectBuilding(imageBitmap!!)

            } else if (rbBarCode.isChecked) {
                detectBarCode(imageBitmap!!)
            }

        }
    }


    private fun detectBarCode(imageFromCamera: Bitmap) {
        Log.e(TAG, "detectBarCode")

        val image = FirebaseVisionImage.fromBitmap(imageFromCamera)
        val detector = FirebaseVision.getInstance().visionBarcodeDetector
        detector.detectInImage(image).addOnSuccessListener {
            for (codeBar in it) {

                var contactInfo = codeBar.contactInfo!!.name!!.formattedName.toString()
                tvResult.setText(contactInfo)

            }

        }
                .addOnFailureListener {
                    showSnakbar(applicationContext.getString(R.string.fail))
                }

    }


    private fun detectBuilding(imageFromCamera: Bitmap) {
        Log.e(TAG, "detectBuilding")
        // This API method requires billing to be enabled. Please enable billing on project
        val options = FirebaseVisionCloudDetectorOptions.Builder()
                .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                .setMaxResults(15)
                .build()


        val image = FirebaseVisionImage.fromBitmap(imageFromCamera)
        val detector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector(options)



        detector.detectInImage(image).addOnSuccessListener {
            for (landmark in it) {

                Log.e(TAG, landmark.landmark)

                var nameOfLandmark = landmark.landmark
                var boundingBox = landmark.boundingBox

                tvResult.setText(nameOfLandmark)
            }


        }.addOnFailureListener {
            showSnakbar(applicationContext.getString(R.string.fail))

        }


    }


    private fun detectText(imageFromCamera: Bitmap) {
        Log.e(TAG, "detectText")
        val image = FirebaseVisionImage.fromBitmap(imageFromCamera)
        val detector = FirebaseVision.getInstance().visionTextDetector
        detector.detectInImage(image).addOnSuccessListener { firebaseVisionText ->
            processText(firebaseVisionText)
        }
                .addOnFailureListener {
                    showSnakbar(applicationContext.getString(R.string.fail))
                }
    }


    private fun processText(text: FirebaseVisionText) {
        val blocks = text.blocks
        if (blocks.size == 0) {
            showSnakbar(applicationContext.getString(R.string.cant_read))
            return
        }
        for (block in text.blocks) {
            val txt = block.text
            tvResult.setText(txt)
        }
    }


    private fun showSnakbar(message: String) {
        Snackbar.make(parentView, message, Snackbar.LENGTH_LONG).show()

    }


}

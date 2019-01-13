package com.lukmannudin.assosiate.uploadfile

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    private var filePath: Uri? = null
    private val PICK_IMAGE_REQUEST = 71


    var storage: FirebaseStorage? = null
    var storageReference: StorageReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val storage = FirebaseStorage.getInstance("gs://testmyproject-2afdc.appspot.com/")

        var storageRef = storage.reference

        var imagesRef: StorageReference? = storageRef.child("images")
        var spaceRef: StorageReference? = storageRef.child("images/space,jpg")

        val rooRef = spaceRef?.root

        val fileName = "space.jpg"
        spaceRef = imagesRef?.child(fileName)

        val path = spaceRef?.path
        val name = spaceRef?.name

        imagesRef = spaceRef?.parent

        val file = Uri.fromFile(File("path/to/images/rivers.jpg"))
        val riversRef = storageRef.child("images/${file.lastPathSegment}")

        btnChoose.setOnClickListener { chooseImage() }
        btnUpload.setOnClickListener { uploadImage() }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null
        ) {
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imgView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage() {
        val storage = FirebaseStorage.getInstance()

        var storageReference:StorageReference?  = storage.reference
        if (filePath != null) {
            Log.i("cek",filePath.toString())
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            val ref = storageReference?.child("images/" + UUID.randomUUID().toString())
            ref?.putFile(filePath!!)
                ?.addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this@MainActivity, "Uploaded", Toast.LENGTH_SHORT).show()
                }
                ?.addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this@MainActivity, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                }
                ?.addOnProgressListener { taskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                        .totalByteCount
                    progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
                }
        }
    }
}

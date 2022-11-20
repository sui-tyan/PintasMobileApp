package com.example.pintas

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text

class EditUserDetails : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri?= null
    private var storageProfilePicRef: StorageReference?= null


//    private val changeImage: TextView ?= null
//    private val saveDetails: Button ?= null
    private var edit_username: EditText ?= null
    private var edit_user_picture: CircleImageView ?= null

    //firestore
    private lateinit var firestoreDb: FirebaseFirestore

    private val GALLERY_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user_details)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        val changeImage = findViewById<TextView>(R.id.changeImage)
        val saveDetails = findViewById<Button>(R.id.saveDetails)
        edit_username = findViewById<EditText>(R.id.edit_username)
        edit_user_picture = findViewById<CircleImageView>(R.id.edit_user_picture)
        
        

        //firestore
        firestoreDb = FirebaseFirestore.getInstance()

        userInfo()



        changeImage.setOnClickListener {
            checker = "clicked"
            pickFromGallery()
        }


        saveDetails.setOnClickListener {
            if (checker == "clicked") {
                uploadImageAndInfo()
            }else{
                updateUserInfoOnly()
            }
        }
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type =  "image/*"
        val mimeType = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun uploadImageAndInfo() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Edit Account")
        progressDialog.setMessage("Updating your profile...")
        progressDialog.show()

        when {
            edit_username?.text.toString() == "" -> {
                Toast.makeText(this, "Please write your Name.", Toast.LENGTH_SHORT).show()
            }
            imageUri == null -> {
                Toast.makeText(this, "Please select your image.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val fileRef = storageProfilePicRef!!.child(firebaseUser!!.uid + ".jpg")

                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                    if (!task.isSuccessful){
                        task.exception?.let{
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener ( OnCompleteListener<Uri>{ task ->
                    if (task.isSuccessful){
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()


                        //firestore
                        val ref = FirebaseFirestore.getInstance().collection("Users")


                        val userMap = HashMap<String, Any>()
                        userMap["username"] = edit_username!!.text.toString()
                        userMap["image"] = myUrl

                        ref.document(firebaseUser.uid).update(userMap)

                        Toast.makeText(this, "Account information has ben updated successfully!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }
                    else{
                        Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    }
                }else{
                    Toast.makeText(this, "Error has occurred. Please try again.", Toast.LENGTH_SHORT).show()
                    Log.e("UploadImage:", "Image Selection Error")
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK){
                    imageUri = result.uri
                    imageUri?.let {
                        setImageURI(it)
                    }
                }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.e("UploadImage:", "Crop Error: ${result.error}")
                }
            }

        }

    }

    private fun setImageURI(imageUri: Uri) {
        Glide.with(this)
            .load(imageUri)
            .into(edit_user_picture!!)
    }

    private fun launchImageCrop(uri: Uri) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1,1)
            .start(this@EditUserDetails)
    }

    private fun updateUserInfoOnly() {
        when {
            edit_username!!.text.toString() == "" -> {
                Toast.makeText(this, "Please write your Name.", Toast.LENGTH_SHORT).show()
            }
            else -> {

                //firestore
                val userRef = FirebaseFirestore.getInstance().collection("Users")

                val userMap = HashMap<String, Any>()
                userMap["username"] = edit_username!!.text.toString()

                userRef.document(firebaseUser.uid).update(userMap)

                //firebase database
                //userRef.child(firebaseUser.uid).updateChildren(userMap)

                checker = ""

                Toast.makeText(this, "Account information has been updated successfully!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun userInfo() {
        //firestore
        val userRef = firestoreDb.collection("Users").document(firebaseUser.uid)

        userRef.addSnapshotListener { snapshot, exception ->
            edit_username!!.setText(snapshot?.getString("username"))
            Picasso.get().load(snapshot?.getString("image")).placeholder(R.drawable.default_profile)
                .into(edit_user_picture)
        }
    }

}
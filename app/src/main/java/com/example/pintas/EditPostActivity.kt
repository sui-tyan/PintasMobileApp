package com.example.pintas

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
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
import kotlinx.android.synthetic.main.activity_post.*

class EditPostActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser

    private var postID = ""
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri?= null
    private var storagePostPicRef: StorageReference?= null

    private val GALLERY_REQUEST_CODE = 1234

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)

        postID  = intent.getStringExtra("postID")!!


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)


        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Post Pictures")


        val post_description = findViewById<EditText>(R.id.descriptionPost)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val image_post = findViewById<ImageView>(R.id.imagePost)

        loadPost(image_post, post_description)

        image_post.setOnClickListener {
            checker = "clicked"
            pickFromGallery()
        }

        val savePost = findViewById<MaterialButton>(R.id.save_post)
        savePost.setOnClickListener {
            if (checker == "clicked") {
                savePost(post_description)
            }else{
                updateInfoOnly(post_description)
            }
        }
    }

    private fun updateInfoOnly(post_description: EditText) {
        when {
            post_description.text.toString() == "" -> {
                Toast.makeText(this, "Please write your Bio.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                //firebase database
                //val userRef = FirebaseDatabase.getInstance().reference.child("profile")

                //firestore
                val userRef = FirebaseFirestore.getInstance().collection("Posts").document(postID)

                val userMap = HashMap<String, Any>()
                userMap["description"] = post_description.text.toString()


                userRef.update(userMap)

                //firebase database
                //userRef.child(firebaseUser.uid).updateChildren(userMap)

                checker = ""

                Toast.makeText(this, "Post has been updated successfully!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }



    private fun savePost(post_description: EditText) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Edit Post")
        progressDialog.setMessage("Saving your changes...")
        progressDialog.show()


        when {
            post_description.text.toString() == "" -> {
                Toast.makeText(this, "Please write description.", Toast.LENGTH_SHORT).show()
            }
            imageUri == null -> {
                Toast.makeText(this, "Please select your image.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val fileRef = storagePostPicRef!!.child(firebaseUser!!.uid + ".jpg")

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

                        //firebase database
                        //val ref = FirebaseDatabase.getInstance().reference.child("profile")

                        //firestore
                        val ref = FirebaseFirestore.getInstance().collection("Posts")


                        val userMap = HashMap<String, Any>()
                        userMap["description"] = post_description.text.toString()
                        userMap["image"] = myUrl

                        ref.document(postID).update(userMap)

                        //firebase database
                        //userRef.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(this, "Account information has ben updated successfully!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }
                    else{
                        progressDialog.dismiss()
                    }
                })
            }
        }

    }

    private fun loadPost(image_post: ImageView, post_description: EditText) {
        val userRef = FirebaseFirestore.getInstance().collection("Posts").document(postID)

        userRef.addSnapshotListener { snapshot, exception ->
            val description = snapshot?.getString("description")
            post_description.setText(description)
            Picasso.get().load(snapshot?.getString("postimage")).into(image_post)
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
            .into(image_post)
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type =  "image/*"
        val mimeType = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun launchImageCrop(uri: Uri) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1920,1080)
            .start(this@EditPostActivity)
    }

}
package com.example.pintas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    var uName = ""
    var uEmail = ""
    var uPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = Firebase.auth

        db = FirebaseFirestore.getInstance()

        val signup = findViewById<Button>(R.id.signup)
        val login = findViewById<TextView>(R.id.login)
        val loginActivity = Intent(this, Login_Activity::class.java)

        login.setOnClickListener {
            val intent = Intent(this, loginActivity::class.java)
            startActivity(intent)
        }

        signup.setOnClickListener {
            validateData()
        }


    }

    private fun validateData() {
        //get data
        val userName = findViewById<TextView>(R.id.user_name)
        val userEmail = findViewById<TextView>(R.id.user_email)
        val userPassword = findViewById<TextView>(R.id.user_password)

        uName = userName.text.toString().trim()
        uEmail = userEmail.text.toString().trim()
        uPassword = userPassword.text.toString().trim()


        //validate data
        if (TextUtils.isEmpty(uName)) {
            userName.error = "Please Enter Username"
        }else if (!Patterns.EMAIL_ADDRESS.matcher(uEmail).matches()) {
            userEmail.error = "Invalid Email Format"
        }else if (TextUtils.isEmpty(uPassword)) {
            userPassword.error = "Please Enter Password"
        }else if (uPassword.length < 6) {
            userPassword.error = "Your password must contain atleast 6 characters"
        }else{
            firebaseSignUp()
        }

    }

    private fun firebaseSignUp() {
        //create account
        auth.createUserWithEmailAndPassword(uEmail, uPassword)
            .addOnSuccessListener {
                //get current user
                val firebaseUser = auth.currentUser

                //get email
                val db_Email = firebaseUser!!.email

                //get current user uid
                val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid

                //initial followers
                val followers = 0

                //schema
                val userMap = HashMap <String, Any>()
                userMap["uid"] = currentUserID
                userMap["username"] = uName.capitalize()
                userMap["email"] = db_Email.toString()
                userMap["followers"] = followers.toString()
                userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/pintas-b85f7.appspot.com/o/default%20profile%2Fdefault_profile.png?alt=media&token=74ef885c-1672-467b-b026-b0f1bb1aa952"

                //put or update database
                val userRef = db.collection("Users").document(currentUserID)
                userRef.set(userMap)

                Toast.makeText(this, "Account Successfully Created.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Sign Up Failed. Please Check Your Internet Connection.", Toast.LENGTH_SHORT).show()
            }
    }
}
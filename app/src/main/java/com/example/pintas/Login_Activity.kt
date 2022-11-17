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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login_Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var uEmail = ""
    private var uPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        auth = Firebase.auth


        var loginButton = findViewById<Button>(R.id.login_button)
        var signup = findViewById<TextView>(R.id.sign_up)
        val signupView = Intent(this, SignupActivity::class.java)

        loginButton.setOnClickListener {
//            startActivity(Intent(this, MainActivity::class.java))
//            Toast.makeText(applicationContext,"Logged In",Toast.LENGTH_SHORT).show()
            validateData()
        }
        signup.setOnClickListener{
            startActivity(signupView)
        }

    }

    private fun validateData() {
        //get data
        val userEmail = findViewById<TextView>(R.id.user_email)
        val userPassword = findViewById<TextView>(R.id.user_password)

        uEmail = userEmail.text.toString().trim()
        uPassword = userPassword.text.toString().trim()


        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(uEmail).matches()) {
            userEmail.error = "Invalid Email Format"
            userEmail.requestFocus()
        }else if (TextUtils.isEmpty(uPassword)) {
            userPassword.error = "Please Enter Password"
            userPassword.requestFocus()
        }else {
            firebaseLogin()
        }
    }

    private fun firebaseLogin() {
        auth.signInWithEmailAndPassword(uEmail, uPassword)
            .addOnSuccessListener {
                //login success
                val firebaseUser = auth.currentUser
                val db_Email = firebaseUser!!.email

                Toast.makeText(this, "Logged In as $db_Email", Toast.LENGTH_SHORT).show()
                updateUI(firebaseUser)
            }.addOnFailureListener {
                Toast.makeText(this, "Incorrect Email/Password", Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
    }

    public override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser == null) {
            return
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}
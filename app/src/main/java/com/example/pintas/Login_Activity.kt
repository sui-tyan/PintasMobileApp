package com.example.pintas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login_Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        auth = Firebase.auth


        var loginButton = findViewById<Button>(R.id.login_button)
        var signup = findViewById<TextView>(R.id.sign_up)
        val signupView = Intent(this, SignupActivity::class.java)

        loginButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            Toast.makeText(applicationContext,"Logged In",Toast.LENGTH_SHORT).show()
        }
        signup.setOnClickListener{
            startActivity(signupView)
        }

    }

    public override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
    }

}
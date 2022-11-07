package com.example.pintas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login_Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        auth = Firebase.auth

        val signup = findViewById<TextView>(R.id.sign_up)
        val signup_view = Intent(this, SignupActivity::class.java)
        var login_button = findViewById<Button>(R.id.login_button)
        val home = Intent(this, MainActivity::class.java)

        login_button.setOnClickListener {
            startActivity(home)
        }
        signup.setOnClickListener{
            startActivity(signup_view)
        }

    }

    public override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
    }

}
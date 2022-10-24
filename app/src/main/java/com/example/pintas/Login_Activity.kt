package com.example.pintas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Login_Activity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        var login_button = findViewById<Button>(R.id.login_button)
        val home = Intent(this, MainActivity::class.java)

        login_button.setOnClickListener {
            startActivity(home)
        }
    }
}
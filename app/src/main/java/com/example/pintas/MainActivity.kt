package com.example.pintas

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.pintas.fragment.HomeFragment
import com.example.pintas.fragment.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private val HomeFragment = HomeFragment()
    private val ProfileFragment = ProfileFragment()

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        database = FirebaseFirestore.getInstance()

        replaceFragment(HomeFragment)

        var bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.home -> replaceFragment(HomeFragment)
                R.id.search -> gotoSearchAct()
                R.id.profile -> replaceFragment(ProfileFragment)
            }
            true
        }
    }

    private fun gotoSearchAct() {
        val intent = Intent(this, SearchActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()

        //check if the user is logged in
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser == null) {
            startActivity(Intent(this, Login_Activity::class.java))
            finish()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        if (fragment != null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, fragment)
            transaction.commit()
        }
    }
}
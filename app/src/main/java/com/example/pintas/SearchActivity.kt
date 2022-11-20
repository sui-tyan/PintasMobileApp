package com.example.pintas

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build.VERSION_CODES.M
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintas.adapter.UserAdapter
import com.example.pintas.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SearchActivity : AppCompatActivity() {

    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null

    private var searchQuery: SearchView? = null

    private lateinit var firestoreDb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val replaceYouFrag = findViewById<FrameLayout>(R.id.replaceProfileFrag)


        firestoreDb = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.searchRecyclerView)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(this)

        mUser = ArrayList()
        userAdapter = UserAdapter(this, mUser as ArrayList<User>, true)
        recyclerView?.adapter = userAdapter


        val searchQuery = findViewById<SearchView>(R.id.search)
        searchQuery.setOnQueryTextListener (object : SearchView.OnQueryTextListener{

            override fun onQueryTextSubmit(query: String?): Boolean {
                searchQuery.clearFocus()
                if (query.toString() == ""){
                    recyclerView?.visibility = View.GONE
                }else{
                    recyclerView?.visibility = View.VISIBLE
                    replaceYouFrag?.visibility = View.GONE


                    retrieveUsers()
                    searchUser(query.toString())
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {


                if (newText.toString() == ""){
                    recyclerView?.visibility = View.GONE
                }else{
                    recyclerView?.visibility = View.VISIBLE
                    replaceYouFrag?.visibility = View.GONE


                    retrieveUsers()
                    searchUser(newText.toString())
                }
                return false
            }

        })



    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }






    @SuppressLint("NotifyDataSetChanged")
    private fun retrieveUsers() {
        //firestore
        val userRef = FirebaseFirestore.getInstance().collection("Users")

        userRef.addSnapshotListener { snapshot, exception ->
            val userList = snapshot!!.toObjects(User::class.java)
            if (searchQuery?.query.toString() == "") {
                mUser?.clear()
                mUser?.addAll(userList)
                userAdapter?.notifyDataSetChanged()
            }

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchUser(input: String) {
        //firestore
        val query = FirebaseFirestore.getInstance()
            .collection("Users")
            .orderBy("username")
            .startAt(input)
            .endAt(input + "\uf8ff")

        query.addSnapshotListener { snapshot, exception ->
            val userList = snapshot!!.toObjects(User::class.java)
            mUser?.clear()

            mUser?.addAll(userList)
            userAdapter?.notifyDataSetChanged()

        }
    }
}
package com.example.pintas.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintas.EditUserDetails
import com.example.pintas.Login_Activity
import com.example.pintas.PostActivity
import com.example.pintas.R
import com.example.pintas.adapter.PostAdapter
import com.example.pintas.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    //initialize auth
    private lateinit var auth: FirebaseAuth

    //initialize firestore
    private lateinit var firestoreDb: FirebaseFirestore

    //get user info
    private lateinit var userUID: String
    private lateinit var userName: String
    private lateinit var profileImage: String
    private lateinit var firebaseUser: FirebaseUser


    private lateinit var recyclerView: RecyclerView
    private lateinit var photoAdapter: PostAdapter
    private lateinit var myPhotoList: MutableList<Post>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }


        setHasOptionsMenu(true)

        //auth
        auth = Firebase.auth

        //firestore
        firestoreDb = FirebaseFirestore.getInstance()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val inflate = inflater.inflate(R.layout.fragment_profile, container, false)

        val toolbar = inflate.findViewById<androidx.appcompat.widget.Toolbar>(R.id.appbar)
        toolbar.title = ""
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        val edit_btn = inflate.findViewById<Button>(R.id.edit_button)
        val follow_btn = inflate.findViewById<Button>(R.id.follow_button)


        recyclerView = inflate.findViewById(R.id.profileRecycler)
        recyclerView.setHasFixedSize(true)
        val lLManager = LinearLayoutManager(context)
        lLManager.reverseLayout = true
        lLManager.stackFromEnd = true
        recyclerView.layoutManager = lLManager

        myPhotoList = ArrayList()
        photoAdapter = PostAdapter(requireActivity(), myPhotoList as ArrayList<Post>)
        recyclerView.adapter = photoAdapter

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        if (pref != null){
            this.userUID = pref.getString("profileId", "none").toString()
            this.profileImage = pref.getString("profileImage", "none").toString()
            this.userName = pref.getString("userName", "none").toString()
        }




        edit_btn.setOnClickListener {
            val intent = Intent(context, EditUserDetails::class.java)
            startActivity(intent)
        }



        if (userUID == firebaseUser.uid) {
            edit_btn.text = "Edit"
//            message_button.setOnClickListener {
//                val intent = Intent(activity, MyMessages::class.java)
//                startActivity(intent)
//                activity?.overridePendingTransition(
//                    com.google.firebase.database.R.anim.slide_in_up,
//                    com.google.firebase.database.R.anim.slide_out_up)
//            }
        }else if (userUID != firebaseUser.uid) {
            edit_btn?.visibility = View.GONE
            follow_btn?.visibility = View.VISIBLE
            follow_btn.setOnClickListener {
                val ref = firestoreDb.collection("Users").document(userUID).collection("Followers").document(firebaseUser.uid)

//                    .addSnapshotListener { snapshot, exception ->
//                        userName = snapshot?.getString("name").toString()
//                        profileImage = snapshot?.getString("image").toString()
//                    }

                Log.i("name", userName)
                Log.i("image", profileImage)
                Log.i("uid", userUID)

            }
        }




        userInfo()

        return inflate
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_profile, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.post_btn){
            //do action here
            val intent = Intent(activity, PostActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        if (id == R.id.tutorial){
            Toast.makeText(activity, "Tutorial", Toast.LENGTH_SHORT).show()
        }

        //logout
        if (id == R.id.sign_out){
            val dialog = AlertDialog.Builder(activity)
                .setTitle("Logout")
                .setMessage("Are you sure to leave?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", null)
                .show()

            val currentID = FirebaseAuth.getInstance().uid
            firestoreDb.collection("profile").document(currentID.toString()).collection("presence")
                .document(currentID.toString()).update("Presence", "Offline")

            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                // Logout the user
                Firebase.auth.signOut()
                Toast.makeText(activity, "Logged out", Toast.LENGTH_SHORT).show()
                val logoutIntent = Intent(activity, Login_Activity::class.java)
                logoutIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(logoutIntent)
                activity?.finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }


    private fun userInfo() {
        //firestore
        Log.e("currentUser:", firebaseUser.uid)
        val userRef = firestoreDb.collection("Users").document(userUID)

        userRef.addSnapshotListener { snapshot, exception ->
            view?.findViewById<TextView>(R.id.user_name)?.text = snapshot?.getString("username")
            view?.findViewById<TextView>(R.id.num_followers)?.text = snapshot?.getString("followers").toString()
            Picasso.get().load(snapshot?.getString("image")).into(view?.findViewById<CircleImageView>(R.id.user_picture))
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }
}
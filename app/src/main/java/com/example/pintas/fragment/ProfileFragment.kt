package com.example.pintas.fragment

import android.app.AlertDialog
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
import com.example.pintas.Login_Activity
import com.example.pintas.R
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

        edit_btn.setOnClickListener {
            TODO("Not yet implemented")
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        userInfo()

        return inflate
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_appbar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.post_btn){
            //do action here
            Toast.makeText(activity, "Intent to Post Activity", Toast.LENGTH_SHORT).show()
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
        val userRef = firestoreDb.collection("Users").document(firebaseUser.uid)

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
}
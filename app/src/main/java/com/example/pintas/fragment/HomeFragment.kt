package com.example.pintas.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintas.Login_Activity
import com.example.pintas.PostActivity
import com.example.pintas.R
import com.example.pintas.adapter.PostAdapter
import com.example.pintas.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var postList: MutableList<Post>
    private lateinit var postAdapter: PostAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val inflate = inflater.inflate(R.layout.fragment_home, container, false)

        val toolbar = inflate.findViewById<androidx.appcompat.widget.Toolbar>(R.id.homeAppbar)
        toolbar.title = ""
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        recyclerView = inflate.findViewById(R.id.homeRecycler)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = PostAdapter(requireActivity(), postList as ArrayList<Post>)
        recyclerView.adapter = postAdapter

        retrievePosts()

        return inflate
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun retrievePosts() {
        val postRef = FirebaseFirestore.getInstance().collection("Posts")

        postRef.addSnapshotListener{ snapshot, exception ->
            val list = snapshot!!.toObjects(Post::class.java)

            postList.clear()
            postList.addAll(list)
            postAdapter.notifyDataSetChanged()
        }
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

//            val currentID = FirebaseAuth.getInstance().uid
//            firestoreDb.collection("profile").document(currentID.toString()).collection("presence")
//                .document(currentID.toString()).update("Presence", "Offline")

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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
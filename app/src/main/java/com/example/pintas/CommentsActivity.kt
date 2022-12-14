package com.example.pintas

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintas.adapter.CommentAdapter
import com.example.pintas.model.Comment
import com.example.pintas.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsActivity : AppCompatActivity() {

    private var addComment: EditText? = null
    private var imageProfile: CircleImageView? = null
    private var post: ImageButton? = null

    private var postId: String? = null
    private var authorId: String? = null

    private var firebaseUser: FirebaseUser? = null

    private var recyclerView: RecyclerView? = null
    private var commentAdapter: CommentAdapter? = null
    private var mComment: MutableList<Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        addComment = findViewById(R.id.addComment)
        imageProfile = findViewById(R.id.image_profile)
        post = findViewById(R.id.post)

        val intent = intent
        postId = intent.getStringExtra("postId")
        authorId = intent.getStringExtra("authorId")

        firebaseUser = FirebaseAuth.getInstance().currentUser

        recyclerView = findViewById(R.id.commentRecycler)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(this)

        mComment = ArrayList()
        commentAdapter = CommentAdapter(this, mComment as ArrayList<Comment>)
        recyclerView?.adapter = commentAdapter

        getUserImage()

        post?.setOnClickListener{
            if (TextUtils.isEmpty(addComment?.text.toString())){
                Toast.makeText(this, "No comment added.", Toast.LENGTH_SHORT).show()
            }else{
                putComment()
                addComment?.setText("")
            }
        }

        getComment()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getComment() {
        FirebaseFirestore.getInstance().collection("Posts").document(postId.toString()).collection("Comments")
            .addSnapshotListener { snapshot, exception ->
                val commentList = snapshot!!.toObjects(Comment::class.java)
                mComment?.clear()
                mComment?.addAll(commentList)
                commentAdapter?.notifyDataSetChanged()
            }
    }

    private fun putComment() {
        val ref = FirebaseFirestore.getInstance().collection("Posts").document(postId.toString()).collection("Comments")
        val commentId: Long = System.currentTimeMillis()

        val commentMap = HashMap<String, Any>()
        commentMap["comment"] = addComment?.text.toString()
        commentMap["publisher"] = firebaseUser!!.uid
        commentMap["commentid"] = commentId!!.toString()
        commentMap["postid"] = postId!!.toString()

        ref.document(commentId.toString()).set(commentMap)

    }

    private fun getUserImage() {
        FirebaseFirestore.getInstance().collection("Users").document(firebaseUser!!.uid)
            .addSnapshotListener { snapshot, error ->
                val user = snapshot!!.toObject(User::class.java)
                if(imageProfile == null) {
                    // Load default image
                    imageProfile?.setImageResource(R.drawable.default_profile)
                }else{
                    Picasso.get().load(user!!.image).into(imageProfile)
                }
            }
    }
}
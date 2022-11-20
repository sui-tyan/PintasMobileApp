package com.example.pintas.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import com.example.pintas.model.Post
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.pintas.EditPostActivity
import com.example.pintas.R
import com.example.pintas.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter (private val mContext: Context,
                   private val mPost: List<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder> () {

    private var firebaseUser: FirebaseUser? = null

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage : CircleImageView
        var postImage : ImageView
        var likeButton : ImageView
        var commentButton : ImageView
        //        var saveButton : ImageView
        var moreButton : ImageView

        var userName : TextView
        var likes : TextView
        var publisher : TextView
        var description : TextView
        var comments : TextView

        init {
            profileImage = itemView.findViewById(R.id.user_profile_image_post)
            postImage = itemView.findViewById(R.id.post_image_community)
            likeButton = itemView.findViewById(R.id.post_image_like_btn)
            commentButton = itemView.findViewById(R.id.post_image_comment_btn)
//            saveButton = itemView.findViewById(R.id.post_save_comment_btn)
            moreButton = itemView.findViewById(R.id.post_image_more_btn)

            userName = itemView.findViewById(R.id.user_name)
            likes = itemView.findViewById(R.id.likes)
            publisher = itemView.findViewById(R.id.publisher)
            description = itemView.findViewById(R.id.description)
            comments = itemView.findViewById(R.id.numberOfComments)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]

        if(holder.postImage == null) {
            // Load default image
            holder.postImage.setImageResource(R.drawable.add_image_icon)
        }else{
            //Picasso.get().load(post.postimage).into(holder.postImage)

            val options: RequestOptions = RequestOptions()
                .placeholder(R.drawable.add_image_icon)
                .error(R.drawable.add_image_icon)

            Glide.with(mContext).load(post.postimage).apply(options).into(holder.postImage)
        }

        publisherInfo(holder.profileImage, holder.userName, holder.publisher, post.publisher)

        isLiked(post.postid, holder.likeButton)
        noOfLikes(post.postid, holder.likes)
        noOfComments(post.postid, holder.comments)

        holder.description.text = post!!.description

        holder.likeButton.setOnClickListener {
            val ref = FirebaseFirestore.getInstance().collection("Posts").document(post.postid)
                .collection("Likes").document(firebaseUser!!.uid)

            noOfLikes(post.postid, holder.likes)

            if (holder.likeButton.tag.equals("like")) {
                val likeMap = HashMap<String, Any>()
                likeMap[firebaseUser!!.uid] = true
                likeMap["liked"] = true

                ref.set(likeMap)
            } else {
                ref.delete()
            }
        }

//        holder.commentButton.setOnClickListener {
//            val intent = Intent(mContext, CommentsActivity::class.java)
//            intent.putExtra("postId", post.postid)
//            intent.putExtra("authorId", post.publisher)
//            mContext.startActivity(intent)
//
//        }
//        holder.comments.setOnClickListener {
//            val intent = Intent(mContext, CommentsActivity::class.java)
//            intent.putExtra("postId", post.postid)
//            intent.putExtra("authorId", post.publisher)
//            mContext.startActivity(intent)
//        }


        //edit feature
        holder.moreButton.setOnClickListener {
            if (firebaseUser!!.uid == post.publisher){

                val options = arrayOf("Edit", "Delete")

                val builder = AlertDialog.Builder(mContext)
                builder.setTitle("Choose Option")
                builder.setItems(options) { dialogInt, which ->

                    if (which == 0) {
                        val intent = Intent(mContext, EditPostActivity::class.java)
                        intent.putExtra("postID", post.postid)
                        mContext.startActivity(intent)
                    } else if (which == 1) {
                        deletePost(post)
                    }
                }

                    .show()

            }else{
                Toast.makeText(mContext, "This post is from another user.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deletePost(post: Post) {
        val dialog = AlertDialog.Builder(mContext)
        dialog.setTitle("Delete")
        dialog.setMessage("Are you sure to delete this post?")
        dialog.setPositiveButton("Delete") { dialogInt, which ->
            FirebaseFirestore.getInstance().collection("Posts").document(post.postid).delete()
            Toast.makeText(mContext, "Post Deleted Successfully!", Toast.LENGTH_SHORT).show()
        }
        dialog.setNegativeButton("Cancel") {dialogInt, which ->
            dialogInt.dismiss()
        }

        dialog.create().show()
    }

    private fun noOfLikes (postId: String, text: TextView) {
        val db = FirebaseFirestore.getInstance().collection("Posts").document(postId)
        db.collection("Likes").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.apply {
                    text.text = "${size()}"
                }
            } else {
                task.exception?.message?.let {
                    Log.e("Likes", "Error Counting Likes")
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun noOfComments (postId: String, text: TextView) {
        val db = FirebaseFirestore.getInstance().collection("Posts").document(postId)
        db.collection("Comments").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.apply {
                    text.text = "View all "+"${size()}" + " comments."
                }
            } else {
                task.exception?.message?.let {
                    Log.e("Comment", "Error Counting Comments")
                }
            }
        }
    }

    private fun isLiked (postId: String, imageView: ImageView) {
        val likeRef = FirebaseFirestore.getInstance().collection("Posts").document(postId).collection("Likes").document(firebaseUser!!.uid)
        likeRef.addSnapshotListener{ snapshot, exception ->
            if (snapshot != null) {
                if (snapshot.exists()) {
                    imageView.setImageResource(R.drawable.like)
                    imageView.tag = "liked"
                }else{
                    imageView.setImageResource(R.drawable.dislike)
                    imageView.tag = "like"
                }
            }
        }
    }

    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherID: String) {
        //firestore
        val userRef = FirebaseFirestore.getInstance().collection("Users").document(publisherID)

        userRef.addSnapshotListener { snapshot, exception ->
            val user = snapshot!!.toObject(User::class.java)


            val options: RequestOptions = RequestOptions()
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)

            Glide.with(mContext).load(user!!.image).apply(options).into(profileImage)
            userName.text = user!!.username
            publisher.text = user!!.username


        }

    }

    override fun getItemCount(): Int {
        return mPost.size
    }
}
package com.example.pintas.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.pintas.EditComment
import com.example.pintas.R
import com.example.pintas.model.Comment
import com.example.pintas.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class CommentAdapter (private val mContext: Context,
                      private val mComment: List<Comment>) : RecyclerView.Adapter<CommentAdapter.ViewHolder> (){

    private var firebaseUser: FirebaseUser? = null

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView
        var userName: TextView
        var comment: TextView

        init {
            profileImage = itemView.findViewById(R.id.image_profile)
            userName = itemView.findViewById(R.id.user_name)
            comment = itemView.findViewById(R.id.comment)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val comment = mComment[position]

        publisherInfo(holder.profileImage, holder.userName, comment.publisher)

        holder.comment.text = comment.comment
        holder.userName

        //edit feature
        holder.itemView.setOnClickListener{
            if (firebaseUser!!.uid == comment.publisher){

                val options = arrayOf("Edit", "Delete")

                val builder = AlertDialog.Builder(mContext)
                builder.setTitle("Choose Option")
                builder.setItems(options) { dialogInt, which ->

                    if (which == 0) {
                        val intent = Intent(mContext, EditComment::class.java)
                        intent.putExtra("commentid", comment.commentid)
                        intent.putExtra("postID", comment.postid)
                        mContext.startActivity(intent)
                    } else if (which == 1) {
                        deleteComment(comment)
                    }
                }

                    .show()

            }else{
                Toast.makeText(mContext, "This comment is from another user.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteComment(comment: Comment) {
        val dialog = AlertDialog.Builder(mContext)
        dialog.setTitle("Delete")
        dialog.setMessage("Are you sure to delete this comment?")
        dialog.setPositiveButton("Delete") { dialogInt, which ->
            FirebaseFirestore.getInstance().collection("Posts").document(comment.postid)
                .collection("Comments").document(comment.commentid).delete()
            Toast.makeText(mContext, "Comment Deleted Successfully!", Toast.LENGTH_SHORT).show()
        }
        dialog.setNegativeButton("Cancel") {dialogInt, which ->
            dialogInt.dismiss()
        }

        dialog.create().show()
    }

    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisherID: String) {
        val userRef = FirebaseFirestore.getInstance().collection("Users").document(publisherID)

        userRef.addSnapshotListener { snapshot, exception ->
            val user = snapshot!!.toObject(User::class.java)

            userName.text = user!!.username
            if(profileImage == null) {
                // Load default image
                profileImage.setImageResource(R.drawable.default_profile)
            }else{
                Picasso.get().load(user!!.image).placeholder(R.drawable.default_profile).into(profileImage)
            }

        }
    }

    override fun getItemCount(): Int {
        return mComment.size
    }
}
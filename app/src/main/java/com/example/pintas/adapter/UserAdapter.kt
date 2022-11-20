package com.example.pintas.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.pintas.R
import com.example.pintas.fragment.ProfileFragment
import com.example.pintas.model.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter (private var mContext: Context,
                   private var mUser: List<User>,
                   private var isFragment: Boolean = false) : RecyclerView.Adapter<UserAdapter.ViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    class ViewHolder (@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userName: TextView = itemView.findViewById(R.id.user_name)
        var userImage: CircleImageView = itemView.findViewById(R.id.user_picture)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUser[position]
        val fragment = (mContext as FragmentActivity).supportFragmentManager.findFragmentByTag("userTemp")
        val replaceProfileFrag = (mContext as FragmentActivity).findViewById<FrameLayout>(R.id.replaceProfileFrag)

        holder.userName.text = user.username
        if(holder.userImage == null) {
            // Load default image
            holder.userImage.setImageResource(R.drawable.default_profile)
        }else{
            Picasso.get().load(user.image).into(holder.userImage)
        }

        if (fragment != null) {
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }


        holder.itemView.setOnClickListener(View.OnClickListener {
            val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("profileId", user.uid)
            pref.putString("profileImage", user.image)
            pref.putString("userName", user.username)
            pref.putString("followers", user.followers)
            pref.apply()


            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .add(R.id.replaceProfileFrag, ProfileFragment(), "UserTemp")
                .addToBackStack("")
                .commit()


            replaceProfileFrag?.visibility = View.VISIBLE

            val recyclerView =
                (mContext as FragmentActivity).findViewById<RecyclerView>(R.id.searchRecyclerView)
            recyclerView?.visibility = View.GONE
        })
    }

}
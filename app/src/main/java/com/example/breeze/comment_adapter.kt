package com.example.breeze

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract.Data
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.breeze.MainActivity.Companion.generateUniqueKey
import com.example.breeze.MainActivity.Companion.userId
import com.example.breeze.newAdapter.myViewHolder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.w3c.dom.Text

class comment_adapter(val context: Activity, val arrayList: List<cData>) :
    RecyclerView.Adapter<comment_adapter.myviewHolder>() {

    lateinit var database: DatabaseReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myviewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false)

        return myviewHolder(itemView, context)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }



    override fun onBindViewHolder(holder: myviewHolder, position: Int) {
        val currentItem = arrayList[position]
        holder.user.text = currentItem.name
        holder.comment.text = currentItem.comment

        val timeAgo = getTimeAgo(currentItem.timestamp ?: 0)
        holder.time.text = timeAgo

        holder.delete_identifier(currentItem.position!!)

        holder.delete.setOnClickListener {


            database = FirebaseDatabase.getInstance().getReference("comments")
            database.child(comment_page.SharedData.sharedString!!).child(userId).child(currentItem.position!!).removeValue()
                .addOnSuccessListener {

                    Toast.makeText(context, "Removed successfully!!", Toast.LENGTH_SHORT).show()
                }

            notifyDataSetChanged()
        }


    }


    fun getTimeAgo(timestamp: Long): String {
        val currentTime = System.currentTimeMillis() / 1000
        val difference = currentTime - timestamp

        if (difference < 0) return "In the future"

        return when {
            difference < 60 -> "$difference sec ago"
            difference < 3600 -> "${difference / 60} min ago"
            difference < 86400 -> "${difference / 3600} hr ago"
            difference < 2592000 -> "${difference / 86400} days ago"
            difference < 31104000 -> "${difference / 2592000} months ago"
            else -> "${difference / 31104000} years ago"
        }
    }


    class myviewHolder(itemView: View, context: Activity) : RecyclerView.ViewHolder(itemView) {

        val user = itemView.findViewById<TextView>(R.id.user_name)
        val comment = itemView.findViewById<TextView>(R.id.comment)
        val time = itemView.findViewById<TextView>(R.id.time_stamp)
        val delete = itemView.findViewById<ImageButton>(R.id.delete_btn)


        fun delete_identifier(Id: String) {

            val database = FirebaseDatabase.getInstance().reference
            database.child("comments").child(comment_page.SharedData.sharedString!!).child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                        // Loop through each comment under the user
                        for (commentSnapshot in snapshot.children) {
                            val comment = commentSnapshot.getValue(cData::class.java)
                            if (Id == comment?.position) {
                                    delete.visibility = View.VISIBLE
                            }
                        }

                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error
                }
            })
        }

    }

}



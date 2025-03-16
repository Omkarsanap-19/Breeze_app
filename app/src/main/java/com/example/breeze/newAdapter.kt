package com.example.breeze

import Data
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.breeze.MainActivity.Companion.generateUniqueKey
import com.example.breeze.MainActivity.Companion.userId
import com.example.breeze.myAdapter.onitemclick
import com.example.breeze.myAdapter.onshareclick
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class newAdapter(val context: Activity, var arrayList: List<Data>) :
    RecyclerView.Adapter<newAdapter.myViewHolder>() {

    private var mylistener: onitemclick? = null
    private var mylisten:onshareclick? = null
    private lateinit var database :DatabaseReference

    interface onitemclick {
        fun setitemclicking(position: Int)
    }


    fun setItemclicklistener(listener: onitemclick) {
        mylistener = listener
    }


    interface onshareclick {
        fun shareitemclicking(position: Int)
    }

    fun shareItemclicklistener(listen: onshareclick) {
        mylisten = listen
    }


    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.each_item, parent, false)
        val time_stamp = itemView.findViewById<TextView>(R.id.time_stamp)
        time_stamp.visibility = View.VISIBLE
        return myViewHolder(itemView, mylistener,mylisten,context)
    }

    override fun getItemCount(): Int {
        return arrayList.size

    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {

        val currentItem = arrayList[position]
        val time = getTimeAgo(currentItem.time ?: 0)
        holder.heading.text = currentItem.title
        val excerpt = currentItem.excerpt?.take(200) ?: ""

        val finalExcerpt = if ((currentItem.excerpt?.length ?: 0) > 200) {
            "$excerpt<b>...Read More</b>"
        } else {
            excerpt
        }

        holder.para.text = Html.fromHtml(finalExcerpt, Html.FROM_HTML_MODE_LEGACY)
        holder.time_stamp.text = time
        Glide.with(context).load(currentItem.thumbnail).into(holder.image)

        holder.comment.setOnClickListener {
            val uniqueId = generateUniqueKey(arrayList[position].title, arrayList[position].date)
            val  intent = Intent(context, comment_page::class.java)
            intent.putExtra("itemId",uniqueId)
            context.startActivity(intent)
            val intent02 = Intent(context,comment_adapter::class.java)
            intent02.putExtra("itemId",uniqueId)

        }


    }

    fun getTimeAgo(timestamp: Int): String {
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

    fun deletetem(i: Int) {
        database = FirebaseDatabase.getInstance().getReference("bookmarks")
        val uniqueId = generateUniqueKey(arrayList[i].title,arrayList[i].date)
        database.child(userId).child(uniqueId).removeValue().addOnSuccessListener {
            Toast.makeText(context, "Removed successfully!!", Toast.LENGTH_SHORT).show()
        }


        notifyDataSetChanged()
    }



    class myViewHolder(itemView: View, listener: onitemclick?,listen: onshareclick?,context: Activity) : RecyclerView.ViewHolder(itemView) {

        val heading = itemView.findViewById<TextView>(R.id.headline)
        val image = itemView.findViewById<ImageView>(R.id.head_img)
        val para = itemView.findViewById<TextView>(R.id.summary)
        val share = itemView.findViewById<ImageButton>(R.id.share_btn)
        val comment = itemView.findViewById<ImageButton>(R.id.comment_btn)
        val time_stamp = itemView.findViewById<TextView>(R.id.time_stamp)
        init {
            itemView.setOnClickListener {
                listener?.setitemclicking(adapterPosition)

            }

            comment.setOnClickListener {

                val  intent = Intent(context, comment_page::class.java)
                startActivity(context,intent, Bundle())

            }

            share.setOnClickListener {
                listen?.shareitemclicking(adapterPosition)
            }

        }

    }
}
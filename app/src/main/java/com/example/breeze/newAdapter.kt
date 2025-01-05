package com.example.breeze

import Data
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
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

        return myViewHolder(itemView, mylistener,mylisten)
    }

    override fun getItemCount(): Int {
        return arrayList.size

    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val currentItem = arrayList[position]

        holder.heading.text = currentItem.title
        holder.para.text = currentItem.excerpt
        Glide.with(context).load(currentItem.thumbnail).into(holder.image)



    }

    fun deletetem(i: Int) {
        database = FirebaseDatabase.getInstance().getReference("bookmarks")
        val uniqueId = generateUniqueKey(arrayList[i].title,arrayList[i].date)
        database.child(userId).child(uniqueId).removeValue().addOnSuccessListener {
            Toast.makeText(context, "Removed successfully!!", Toast.LENGTH_SHORT).show()
        }

        notifyDataSetChanged()
    }

    class myViewHolder(itemView: View, listener: onitemclick?,listen: onshareclick?) : RecyclerView.ViewHolder(itemView) {

        val heading = itemView.findViewById<TextView>(R.id.headline)
        val image = itemView.findViewById<ImageView>(R.id.head_img)
        val para = itemView.findViewById<TextView>(R.id.summary)
        val share = itemView.findViewById<ImageButton>(R.id.share_btn)

        init {
            itemView.setOnClickListener {
                listener?.setitemclicking(adapterPosition)
            }

            share.setOnClickListener {
                listen?.shareitemclicking(adapterPosition)
            }

        }

    }
}
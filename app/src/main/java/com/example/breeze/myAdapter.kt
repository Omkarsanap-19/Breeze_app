package com.example.breeze

import Data
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.text.Html
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.breeze.MainActivity.Companion.generateUniqueKey
import com.example.breeze.MainActivity.Companion.userId
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class myAdapter(val context: Activity, val arrayList: List<Data>) :
    RecyclerView.Adapter<myAdapter.myViewHolder>() {

    private lateinit var btnunfill: ImageButton
    private lateinit var bookmark: CheckBox
    lateinit var database: DatabaseReference
    private var mylistene: onitembookmark? = null
    private var mylisten: onshareclick? = null


    interface onitembookmark {
        fun setitemBookmark(position: Int)
    }

    fun setItemBooklistener(listener: onitembookmark) {
        mylistene = listener
    }

    private var mylistener: onitemclick? = null

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
        btnunfill = itemView.findViewById(R.id.book_unfill)
        bookmark = itemView.findViewById(R.id.checkBox)
        bookmark.visibility = View.VISIBLE



        return myViewHolder(
            itemView,
            mylistener,
            mylistene,
            mylisten,
            context,
            arrayList
        )

    }

    override fun getItemCount(): Int {
        return arrayList.size

    }


    override fun onBindViewHolder(holder: myViewHolder, position: Int) {

        val currentItem = arrayList[position]
        holder.heading.text = currentItem.title


        val excerpt = currentItem.excerpt?.take(200) ?: ""

        val finalExcerpt = if ((currentItem.excerpt?.length ?: 0) > 200) {
            "$excerpt<b>...Read More</b>"
        } else {
            excerpt
        }



        holder.para.text = Html.fromHtml(finalExcerpt, Html.FROM_HTML_MODE_LEGACY)
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




    fun bookmark_done(position: Int) {

        database = FirebaseDatabase.getInstance().getReference("bookmarks")

        arrayList[position].time = (System.currentTimeMillis() / 1000).toInt()

        val uniqueId = generateUniqueKey(arrayList[position].title, arrayList[position].date)

        database.child(userId).child(uniqueId).setValue(arrayList[position]).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(
                    context,
                    "Bookmark successfully!!",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                Toast.makeText(
                    context,
                    it.exception?.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    class myViewHolder(
        itemView: View,
        listener: onitemclick?,
        listene: onitembookmark?,
        listen: onshareclick?,
        context: Activity,
        arrayList: List<Data>
    ) : RecyclerView.ViewHolder(itemView) {


        val heading = itemView.findViewById<TextView>(R.id.headline)
        val image = itemView.findViewById<ImageView>(R.id.head_img)
        val para = itemView.findViewById<TextView>(R.id.summary)
        val container = itemView.findViewById<CardView>(R.id.rvContainer)
        val btn = itemView.findViewById<ImageButton>(R.id.book_fill)
        val btnunfill = itemView.findViewById<ImageButton>(R.id.book_unfill)
        val share = itemView.findViewById<ImageButton>(R.id.share_btn)
        val bookmark = itemView.findViewById<CheckBox>(R.id.checkBox)
        val comment = itemView.findViewById<ImageButton>(R.id.comment_btn)
        init {
            itemView.setOnClickListener {

                listener?.setitemclicking(adapterPosition)
            }


            share.setOnClickListener {
                listen?.shareitemclicking(adapterPosition)
            }



            bookmark.setOnClickListener {

                if (bookmark.isChecked) {

                    myAdapter(context, arrayList).bookmark_done(adapterPosition)

                } else {

                    newAdapter(context, arrayList).deletetem(adapterPosition)

                }

            }


        }



    }
}
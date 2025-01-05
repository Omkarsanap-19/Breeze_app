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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class myAdapter(val context: Activity, val arrayList: List<Data>) :
    RecyclerView.Adapter<myAdapter.myViewHolder>() {

        private lateinit var btnunfill:ImageButton

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
        btnunfill.visibility = View.VISIBLE
        return myViewHolder(itemView, mylistener,mylistene,mylisten)

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

    class myViewHolder(itemView: View, listener: onitemclick?,listene: onitembookmark?,listen: onshareclick?) : RecyclerView.ViewHolder(itemView) {


        val heading = itemView.findViewById<TextView>(R.id.headline)
        val image = itemView.findViewById<ImageView>(R.id.head_img)
        val para = itemView.findViewById<TextView>(R.id.summary)
        val btn = itemView.findViewById<ImageButton>(R.id.book_fill)
        val btnunfill = itemView.findViewById<ImageButton>(R.id.book_unfill)
        val share = itemView.findViewById<ImageButton>(R.id.share_btn)
        init {
            itemView.setOnClickListener {
                listener?.setitemclicking(adapterPosition)
            }
            btnunfill.setOnClickListener {
                listene?.setitemBookmark(adapterPosition)
                btnunfill.visibility = View.GONE
                btn.visibility = View.VISIBLE

            }

            share.setOnClickListener {
                listen?.shareitemclicking(adapterPosition)
            }


        }

    }
}
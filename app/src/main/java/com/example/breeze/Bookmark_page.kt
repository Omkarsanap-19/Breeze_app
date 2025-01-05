package com.example.breeze

import Data
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isGone
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.breeze.MainActivity.Companion.userId
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Bookmark_page : Fragment() {
    lateinit var newAdapter: newAdapter
    lateinit var database: DatabaseReference
    lateinit var dataList: List<Data>
    lateinit var recyclerView: RecyclerView
    lateinit var con: Context
    lateinit var act: Activity
    lateinit var loading:ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bookmark_page, container, false)

        val profile_img = view?.findViewById<ImageView>(R.id.profile_img1)

        profile_img?.setOnClickListener {
            val intent = Intent(requireContext(), profileActivity::class.java)
            startActivity(intent)
        }


        if (isAdded) {
            recyclerView = view.findViewById<RecyclerView>(R.id.recycleView)
            loading = view.findViewById(R.id.progressBar)
            dataList = ArrayList()
            database = FirebaseDatabase.getInstance().reference


            if (isAdded) {
                // Fragment is attached, safe to perform operations that require context or activity
                con = requireContext()  // Safe to use this now
                act = requireActivity()
            } else {
                // Handle the case where the fragment is not attached
                Log.e("BookmarkPage", "Fragment is not attached to context.")
            }

            getNewsData()

            val swipeGesture = object : swipeGesture() {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    super.onSwiped(viewHolder, direction)
                    when (direction) {
                        ItemTouchHelper.RIGHT -> {
                            newAdapter.deletetem(viewHolder.adapterPosition)
                        }

                        ItemTouchHelper.LEFT -> {
                            newAdapter.deletetem(viewHolder.adapterPosition)
                        }
                    }
                }
            }

            val touchHelper = ItemTouchHelper(swipeGesture)
            touchHelper.attachToRecyclerView(recyclerView)
            newAdapter = newAdapter(act, dataList)


        }

        return view
    }


    private fun getNewsData() {

        loading.visibility =View.VISIBLE
        val templist = dataList.toMutableList()
        database = FirebaseDatabase.getInstance().getReference("bookmarks")

        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                templist.clear()
                if (snapshot.exists()) {
                    for (newSnap in snapshot.children) {
                        val newsData = newSnap.getValue(Data::class.java)
                        if (newsData != null) {
                            templist.add(newsData)
                        }

                    }

                    dataList = templist
                    recyclerView.layoutManager = LinearLayoutManager(con)
                    newAdapter = newAdapter(act, dataList)
                    recyclerView.adapter = newAdapter

                    newAdapter.setItemclicklistener(object : newAdapter.onitemclick {
                        override fun setitemclicking(position: Int) {
                            val intent = Intent(requireContext(), webView_page::class.java)
                            intent.putExtra("content", dataList[position].url)
                            startActivity(intent)

                        }
                    })

                    newAdapter.shareItemclicklistener(object :newAdapter.onshareclick{
                        override fun shareitemclicking(position: Int) {
                            val intent = Intent(Intent.ACTION_SEND)
                            intent.putExtra(Intent.EXTRA_TEXT, "${dataList[position].title} \n ${dataList[position].url} \n \n By ${dataList[position].publisher?.name} via Breeze")
                            intent.setType("text/plain")

                            if (intent.resolveActivity(requireContext().packageManager)!=null){
                                startActivity(intent)
                            }
                        }

                    })

                } else {
                    dataList = templist

                }

                loading.visibility =View.GONE

                newAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
                loading.visibility =View.GONE
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }

        })

    }






}
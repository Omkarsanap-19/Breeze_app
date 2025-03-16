package com.example.breeze

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.breeze.MainActivity.Companion.userId
import com.example.breeze.databinding.ActivityCommentPageBinding
import com.example.breeze.databinding.ActivitySignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class comment_page : AppCompatActivity() {


    object SharedData {
        var sharedString: String? = null
    }

    lateinit var binding: ActivityCommentPageBinding
    lateinit var dataList: List<cData>
    lateinit var adapter: comment_adapter
    lateinit var nem: String
    private val commentList = mutableListOf<cData>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCommentPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        SharedData.sharedString = intent.getStringExtra("itemId") ?:" "





        dataList = ArrayList()
        binding.arrowBack.setOnClickListener {
            onBackPressed()
        }



        binding.btnSend.setOnClickListener {

            val commentText = binding.inputComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                postComment(commentText,SharedData.sharedString)
                binding.inputComment.text.clear()
            }else{
                Toast.makeText(this,"Please enter comment!!",Toast.LENGTH_SHORT).show()
            }

        }

        loadAllComments(SharedData.sharedString!!)

        binding.recycleComment.layoutManager = LinearLayoutManager(this)
        adapter = comment_adapter(this, commentList)
        binding.recycleComment.adapter = adapter






    }


    private fun postComment(commentText: String,Item:String?) {
        // Generate a unique ID for the comment
        val user = FirebaseAuth.getInstance().currentUser
        val  database = FirebaseDatabase.getInstance().reference
        val commentId = database.push().key ?: return
        if (user?.displayName.isNullOrEmpty()){
             nem = "Anonymous"
        }else{
            nem = user?.displayName!!
        }
        val newComment = cData(
            name = nem  , // Replace with actual username if needed
            comment = commentText,
            position = commentId,
            timestamp = (System.currentTimeMillis() / 1000) // Convert to seconds
        )

        // Save to Firebase Realtime Database
        database.child("comments").child(Item!!).child(userId).child(commentId).setValue(newComment).addOnCompleteListener {

            if (it.isSuccessful) {
                Snackbar.make(binding.main,"Comment Added", Snackbar.LENGTH_SHORT).show()
            }
        }


    }

    private fun loadAllComments(Id:String) {

        binding.progressBar.visibility = View.VISIBLE
        val database = FirebaseDatabase.getInstance().reference
        database.child("comments").child(Id).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()

                // Loop through each user
                for (userSnapshot in snapshot.children) {
                    // Loop through each comment under the user
                    for (commentSnapshot in userSnapshot.children) {
                        val comment = commentSnapshot.getValue(cData::class.java)
                        if (comment != null) {
                            commentList.add(comment)
                        }
                    }
                }

                // Notify the adapter about the updated list
                adapter.notifyDataSetChanged()

                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
                binding.progressBar.visibility = View.GONE
            }
        })
    }





}
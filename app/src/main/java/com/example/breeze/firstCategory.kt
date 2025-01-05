package com.example.breeze

import Data
import NewsResponse
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.breeze.MainActivity.Companion.generateUniqueKey
import com.example.breeze.MainActivity.Companion.userId
import com.example.breeze.databinding.FragmentFirstCategoryBinding
import com.example.breeze.profileActivity.UniversalVariable
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.system.exitProcess


class firstCategory : Fragment() {
    private var _binding: FragmentFirstCategoryBinding? = null
    private val binding get() = _binding!!
    lateinit var adapt:myAdapter
    private var currentPage:Int=1
    lateinit var database: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference("bookmarks")

        if (isAdded)fetchNews()


        binding.pullRefresh.setOnRefreshListener {
            if (currentPage <= 20) {
                currentPage += 1
                fetchNews()
                binding.pullRefresh.isRefreshing = false
            }else{
                currentPage = 3
                Toast.makeText(requireContext(),"No new content available at the moment. Please check back later!",Toast.LENGTH_SHORT).show()
                fetchNews()

            }

        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed(){

                askatoleave()
            }
        })

    }

    private fun askatoleave() {
        val builder= AlertDialog.Builder(requireContext())
        builder.setTitle("Exit App")
        builder.setMessage("Are you sure you want to exit?")
        builder.setIcon(R.drawable.door_exit)
        builder.setPositiveButton("YES",DialogInterface.OnClickListener { dialog, which ->

            requireActivity().finishAffinity() // Close all activities in the task
            exitProcess(0) // Terminate the app process

        })
        builder.setNegativeButton("CANCEL",DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
        })
        builder.show()


    }

    private fun fetchNews() {

        val news = RetrofitInstance.newsInstance.getHeadlines(UniversalVariable.sharedValue,"in","General" , currentPage)
        news.enqueue(object : Callback<NewsResponse?> {


            override fun onResponse(p0: Call<NewsResponse?>, p1: Response<NewsResponse?>) {
                val responseBody = p1.body()
                if (!isAdded)return
                val productList = if (responseBody?.data != null) responseBody.data
                                    else throw NullPointerException("Expression 'responseBody?.data' must not be null")

                binding.recycle?.layoutManager = LinearLayoutManager(requireContext())
                adapt = myAdapter(requireActivity(),productList)
                binding.recycle?.adapter = adapt

                adapt.setItemclicklistener(object : myAdapter.onitemclick {
                    override fun setitemclicking(position: Int) {
                        val intent = Intent(requireContext(), webView_page::class.java)
                        intent.putExtra("content",productList[position].url)
                        startActivity(intent)

                    }
                })


                adapt.shareItemclicklistener(object :myAdapter.onshareclick{
                    override fun shareitemclicking(position: Int) {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.putExtra(Intent.EXTRA_TEXT,"${productList[position].title} \n ${productList[position].url} \n \n By ${productList[position].publisher?.name} via Breeze")
                        intent.setType("text/plain")

                        if (intent.resolveActivity(requireContext().packageManager)!=null){
                            startActivity(intent)
                        }
                    }

                })


                adapt.setItemBooklistener(object : myAdapter.onitembookmark {
                    override fun setitemBookmark(position: Int) {
                        val uniqueId = generateUniqueKey(productList[position].title,productList[position].date)
                        database.child(userId).child(uniqueId)
                            .setValue(productList[position]).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Bookmark successfully!!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        it.exception?.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }


                })

            }

            override fun onFailure(p0: Call<NewsResponse?>, p1: Throwable) {
                if (!isAdded)return
                Log.d("Main Activity","onFailure: " + p1.message)
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding to avoid memory leaks
    }

}
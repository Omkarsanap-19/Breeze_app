package com.example.breeze

import NewsResponse
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.breeze.MainActivity.Companion.generateUniqueKey
import com.example.breeze.MainActivity.Companion.userId
import com.example.breeze.databinding.FragmentFifthCategoryBinding
import com.example.breeze.profileActivity.UniversalVariable
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class fifthCategory : Fragment() {
    private var _binding: FragmentFifthCategoryBinding?= null
    private val binding get() = _binding!!
    lateinit var adapt:myAdapter
    private lateinit var pull : SwipeRefreshLayout
    var currentPage: Int = 1
    lateinit var database : DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFifthCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded){
        fetchNews()
        }

        database = FirebaseDatabase.getInstance().getReference("bookmarks")

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
                (requireActivity() as MainActivity).replaceFramewithFragment(Home_page())
            }
        })








    }

    private fun fetchNews() {


        val news = RetrofitInstance.newsInstance.getHeadlines(UniversalVariable.sharedValue,"in","Technology" ,currentPage )
        news.enqueue(object : Callback<NewsResponse?> {
            override fun onResponse(p0: Call<NewsResponse?>, p1: Response<NewsResponse?>) {
                val responseBody = p1.body()
                if (!isAdded) return
                val productList = if (responseBody?.data != null) responseBody.data
                else { emptyList() }
                binding.recycle.layoutManager = LinearLayoutManager(requireContext())
                adapt = myAdapter(requireActivity(),productList)
                binding.recycle.adapter = adapt

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
                if (!isAdded) return
                Log.d("Main Activity","onFailure: " + p1.message)


            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding to avoid memory leaks
    }

}
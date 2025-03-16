package com.example.breeze

import NewsResponse
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.breeze.MainActivity.Companion.generateUniqueKey
import com.example.breeze.MainActivity.Companion.userId
import com.example.breeze.databinding.FragmentFifthCategoryBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Search_page : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var searchView: SearchView
    lateinit var adapt: myAdapter
    private val handler = Handler(Looper.getMainLooper()) // Handler for debouncing
    private var searchRunnable: Runnable? = null
    lateinit var database : DatabaseReference
    lateinit var loading : ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_page, container, false)

        val profile_img = view?.findViewById<ImageView>(R.id.profile_img2)

        profile_img?.setOnClickListener {
            val intent = Intent(requireContext(), profileActivity::class.java)
            startActivity(intent)
        }
        recyclerView = view.findViewById(R.id.recycle_search)
        searchView = view.findViewById(R.id.search_view)
        database = FirebaseDatabase.getInstance().getReference("bookmarks")
        loading = view.findViewById(R.id.progressBar)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    (requireActivity() as MainActivity).bottomUpdate(1)
                    replaceFramewithFragment(Home_page())
                }
            })



        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    fetchNews(query)
                    // Close the keyboard
                    val imm =
                        requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(searchView.windowToken, 0)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Search query cannot be empty",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                searchRunnable?.let { handler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    if (!newText.isNullOrEmpty()) {
                        loading.visibility = View.VISIBLE
                        newText?.let { fetchNews(it) }
                    } else {
                        loading.visibility = View.GONE
                        adapt = myAdapter(requireActivity(), emptyList())
                        recyclerView.adapter = adapt
                    }
                }

                handler.postDelayed(searchRunnable!!, 200)
                return true
            }


        })


        return view
    }


    private fun fetchNews(query: String?) {

        val news = RetrofitInstance.newsInstance.getSearch(
            query.toString(),
            profileActivity.UniversalVariable.sharedValue
        )

        news.enqueue(object : Callback<NewsResponse?> {
            override fun onResponse(p0: Call<NewsResponse?>, p1: Response<NewsResponse?>) {
                loading.visibility = View.GONE

                val response_body = p1.body()

                val news_items = if (response_body?.data != null) {
                    response_body.data
                } else {
                    emptyList()
                }
                if (response_body?.data != null && response_body.data.isNotEmpty() && news_items.isNotEmpty()) {

                    recyclerView.layoutManager = LinearLayoutManager(context)
                    adapt = myAdapter(requireActivity(), news_items)
                    recyclerView.adapter = adapt


                    adapt.setItemclicklistener(object : myAdapter.onitemclick {
                        override fun setitemclicking(position: Int) {
                            val intent = Intent(requireContext(), webView_page::class.java)
                            intent.putExtra("content", news_items[position].url)
                            startActivity(intent)

                        }
                    })


                    adapt.shareItemclicklistener(object : myAdapter.onshareclick {
                        override fun shareitemclicking(position: Int) {
                            val intent = Intent(Intent.ACTION_SEND)
                            intent.putExtra(
                                Intent.EXTRA_TEXT,
                                "${news_items[position].title} \n ${news_items[position].url} \n \n By ${news_items[position].publisher?.name} via Breeze"
                            )
                            intent.setType("text/plain")

                            if (intent.resolveActivity(requireContext().packageManager) != null) {
                                startActivity(intent)
                            }
                        }

                    })


                    adapt.setItemBooklistener(object : myAdapter.onitembookmark {
                        override fun setitemBookmark(position: Int) {
                            val uniqueId = generateUniqueKey(
                                news_items[position].title,
                                news_items[position].date
                            )
                            database.child(userId).child(uniqueId)
                                .setValue(news_items[position]).addOnCompleteListener {
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
                } else {
                    Toast.makeText(
                        requireContext(),
                        "No results found for $query",
                        Toast.LENGTH_SHORT
                    ).show()
                    adapt = myAdapter(requireActivity(), emptyList())
                    recyclerView.adapter = adapt
                }


            }

            override fun onFailure(p0: Call<NewsResponse?>, p1: Throwable) {
                loading.visibility = View.GONE
                Log.d("Main Activity", "onFailure: " + p1.message)
                Toast.makeText(
                    requireContext(),
                    "Failed to fetch results. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()

            }
        })

    }


    fun replaceFramewithFragment(fragment: Fragment) {

        val fragmanager = fragmentManager
        val transaction = fragmanager?.beginTransaction()
        transaction?.replace(R.id.framelayout, fragment)
        transaction?.addToBackStack(tag)
        transaction?.commit()

    }




}
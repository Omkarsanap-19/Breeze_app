package com.example.breeze

import NewsResponse
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.breeze.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {
    lateinit var adapt: myAdapter

    companion object {
        val userId = FirebaseAuth.getInstance().currentUser?.uid!!
        fun generateUniqueKey(title: String?, date: String?): String {
            // Combine title and date, then hash them to ensure uniqueness
            val combined = "$title$date"
            return hashString(combined)
        }

        fun hashString(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) } // Convert to hex string
        }
    }

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


//    val itemView = LayoutInflater.from(this).inflate(R.layout.fragment_search_page,null)
//        val search = itemView.findViewById<SearchView>(R.id.search_view)

        replaceFramewithFragment(Home_page())

        binding.bottomNavigation.add(
            CurvedBottomNavigation.Model(
                1,
                "Home",
                R.drawable.baseline_home_filled_24
            )
        )
        binding.bottomNavigation.add(
            CurvedBottomNavigation.Model(
                2,
                "Search",
                R.drawable.baseline_search_24
            )
        )
        binding.bottomNavigation.add(
            CurvedBottomNavigation.Model(
                3,
                "Bookmarks",
                R.drawable.baseline_bookmarks_24
            )
        )

        binding.bottomNavigation.show(1,true)


        binding.bottomNavigation.setOnClickMenuListener {

            when(it.id){

                1 -> replaceFramewithFragment(Home_page())
                2 -> {replaceFramewithFragment(Search_page()) }
                3 -> replaceFramewithFragment(Bookmark_page())

            }

        }


    }


    fun replaceFramewithFragment(fragment: Fragment) {

        val fragmanager = supportFragmentManager
        val transaction = fragmanager.beginTransaction()
        transaction.replace(R.id.framelayout, fragment)
        transaction.commit()


    }

    private fun getNews() {
        val recyclerView: RecyclerView? = findViewById<RecyclerView>(R.id.recycle)


        val news = RetrofitInstance.newsInstance.getHeadlines("en", "in", "General")
        news.enqueue(object : Callback<NewsResponse?> {
            override fun onResponse(p0: Call<NewsResponse?>, p1: Response<NewsResponse?>) {
                val responseBody = p1.body()
                val productList = responseBody?.data!!

                adapt = myAdapter(this@MainActivity, productList)

                recyclerView?.adapter = adapt
                recyclerView?.layoutManager = LinearLayoutManager(this@MainActivity)

            }

            override fun onFailure(p0: Call<NewsResponse?>, p1: Throwable) {
                Log.d("Main Activity", "onFailure: " + p1.message)
            }
        })

    }

}
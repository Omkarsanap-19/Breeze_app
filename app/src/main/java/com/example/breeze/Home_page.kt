package com.example.breeze

import android.annotation.SuppressLint
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.breeze.MainActivity
import com.example.breeze.databinding.FragmentHomePageBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.breeze.MainActivity as BreezeMainActivity


class Home_page : Fragment() {

    private lateinit var Tablayout: TabLayout
    private lateinit var Viewpage: ViewPager2
    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: fragmentpageAdapter
        override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profile_img = view.findViewById<ImageView>(R.id.profile_img)

        profile_img.setOnClickListener {
            val intent = Intent(requireContext(), profileActivity::class.java)
            startActivity(intent)
        }

        Tablayout = view.findViewById(R.id.tablayout)
        Viewpage = view.findViewById(R.id.viewpage)


        adapter = fragmentpageAdapter(requireFragmentManager(), lifecycle)

        Tablayout.addTab(Tablayout.newTab().setText("Trending"))
        Tablayout.addTab(Tablayout.newTab().setText("Global"))
        Tablayout.addTab(Tablayout.newTab().setText("Sports"))
        Tablayout.addTab(Tablayout.newTab().setText("Business"))
        Tablayout.addTab(Tablayout.newTab().setText("Technology"))
        Tablayout.addTab(Tablayout.newTab().setText("Politics"))
        Tablayout.addTab(Tablayout.newTab().setText("Entertainment"))


        Viewpage.adapter = adapter

        Tablayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    Viewpage.currentItem = tab.position
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        Viewpage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Tablayout.selectTab(Tablayout.getTabAt(position))
            }
        })














    }




}


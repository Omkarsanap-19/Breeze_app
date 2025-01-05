package com.example.breeze

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class fragmentpageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
): FragmentStateAdapter(fragmentManager,lifecycle) {
    override fun getItemCount(): Int {
        return 7
    }

    override fun createFragment(position: Int): Fragment {
      return  when(position){

            0 -> firstCategory()
            1 -> secondCategory()
            2 -> thirdCategory()
            3 -> fourthCategory()
            4 -> fifthCategory()
            5 -> sixthCategory()



          else -> { seventhCategory()}
      }

    }


}
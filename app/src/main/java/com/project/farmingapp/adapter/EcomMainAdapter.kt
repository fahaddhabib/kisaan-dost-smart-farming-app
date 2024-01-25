package com.project.farmingapp.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.farmingapp.view.ecom.EcomSellFragment
import com.project.farmingapp.view.ecom.EcommBuyFragment


class EcomMainAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2 // Set the number of tabs

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> EcommBuyFragment()
                1 -> EcomSellFragment()
                else -> throw IndexOutOfBoundsException()
            }
        }
    }

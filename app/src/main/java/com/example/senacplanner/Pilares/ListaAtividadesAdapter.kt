package com.example.senacplanner.Pilares


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ListaAtividadesAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PageFragment()
            1 -> PageFragment2()
            2 -> PageFragment3()
            else -> PageFragment()
        }
    }
}
package com.example.senacplanner.Pilares

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.senacplanner.R

class ListaAtividades : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_atividades)

        viewPager = findViewById(R.id.viewPager)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)

        val adapter = ListaAtividadesAdapter(this)
        viewPager.adapter = adapter

        btnPrev.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem = viewPager.currentItem - 1
            }
        }

        btnNext.setOnClickListener {
            if (viewPager.currentItem < adapter.itemCount - 1) {
                viewPager.currentItem = viewPager.currentItem + 1
            }
        }


        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                btnPrev.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
                btnNext.visibility = if (position == adapter.itemCount - 1) View.INVISIBLE else View.VISIBLE
            }
        })
    }
}
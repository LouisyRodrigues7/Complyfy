package com.example.senacplanner.Pilares

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.senacplanner.R

class ListaAtividades : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_atividades)

        viewPager = findViewById(R.id.viewPager)


        val adapter = ListaAtividadesAdapter(this)
        viewPager.adapter = adapter



    }
}
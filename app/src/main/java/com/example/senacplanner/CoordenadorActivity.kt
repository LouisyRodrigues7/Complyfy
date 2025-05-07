package com.example.senacplanner

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.senacplanner.fragmentpilares.MeusPilaresFragment
import com.example.senacplanner.fragmentpilares.TodosPilaresFragment
import com.example.senacplanner.fragmentpilares.ViewPagerAdapter

class CoordenadorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coordenador)

        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: "Usuário"
        findViewById<TextView>(R.id.textViewSaudacao).text = "Olá, $nomeUsuario"

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(MeusPilaresFragment(), "Meus Pilares")
        adapter.addFragment(TodosPilaresFragment(), "Todos os Pilares")

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
    }
}

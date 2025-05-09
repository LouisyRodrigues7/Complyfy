package com.example.senacplanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.example.senacplanner.apoio.MeusPilaresApoioFragment
import com.example.senacplanner.fragmentpilares.TodosPilaresFragment
import com.example.senacplanner.fragmentpilares.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import android.widget.ImageView

class ApoioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apoio)

        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: "Usuário de Apoio"

        val toolbar = findViewById<Toolbar>(R.id.toolbarApoio)
        toolbar.title = "Olá, $nomeUsuario"
        setSupportActionBar(toolbar)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutApoio)
        val viewPager = findViewById<ViewPager2>(R.id.viewPagerApoio)

        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(MeusPilaresApoioFragment(), "Meus Pilares")
        adapter.addFragment(TodosPilaresFragment(), "Todos os Pilares")
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()

        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            viewPager.setCurrentItem(0, true) // Volta para "Meus Pilares"
        }

    }
}
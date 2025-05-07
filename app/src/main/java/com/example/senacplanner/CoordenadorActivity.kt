package com.example.senacplanner

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.example.senacplanner.fragmentpilares.MeusPilaresFragment
import com.example.senacplanner.fragmentpilares.TodosPilaresFragment
import com.example.senacplanner.fragmentpilares.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CoordenadorActivity : AppCompatActivity() {

    private lateinit var caixaCriarPilar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coordenador)

        // Recuperar o nome do coordenador passado no Intent
        val nomeCoordenador = intent.getStringExtra("NOME_USUARIO") ?: "Coordenador"

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Olá, $nomeCoordenador"  // Atualizando o título da Toolbar com o nome do coordenador
        setSupportActionBar(toolbar)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        // Adicionar fragments ao ViewPager
        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(MeusPilaresFragment(), "Meus Pilares")
        adapter.addFragment(TodosPilaresFragment(), "Todos os Pilares")
        viewPager.adapter = adapter

        // Configurar o TabLayout com o ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()

        // Configurar o botão para adicionar um pilar
        val btnAddPilar = findViewById<ImageView>(R.id.btnAddPilar)
        caixaCriarPilar = findViewById(R.id.caixaCriarPilar)

        btnAddPilar.setOnClickListener {
            toggleCaixaCriarPilar()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun toggleCaixaCriarPilar() {
        caixaCriarPilar.visibility =
            if (caixaCriarPilar.visibility == View.GONE) View.VISIBLE else View.GONE
    }
}

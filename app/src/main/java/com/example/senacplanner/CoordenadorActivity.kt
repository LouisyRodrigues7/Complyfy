package com.example.senacplanner

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.example.senacplanner.editarpilar.EditarActivity
import com.example.senacplanner.fragmentpilares.MeusPilaresFragment
import com.example.senacplanner.fragmentpilares.TodosPilaresFragment
import com.example.senacplanner.fragmentpilares.ViewPagerAdapter
import com.example.senacplanner.novopilar.NovoPilarActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CoordenadorActivity : AppCompatActivity() {
    private lateinit var caixaCriarPilar: TextView
    private lateinit var caixaEditarPilar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coordenador)

        // Recuperar nome do coordenador
        val nomeCoordenador = intent.getStringExtra("NOME_USUARIO") ?: "Coordenador"
        //val sharedPref = getSharedPreferences("usuario_pref", MODE_PRIVATE)
        //val nome = sharedPref.getString("nome_usuario", "Usuário")
        //val id = sharedPref.getInt("id_usuario", -1)
        val idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Olá, $nomeCoordenador"
        setSupportActionBar(toolbar)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(MeusPilaresFragment(), "Meus Pilares")
        adapter.addFragment(TodosPilaresFragment(), "Todos os Pilares")
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()

        // Botão e ações para "Criar Pilar"
        val btnAddPilar = findViewById<ImageView>(R.id.btnAddPilar)
        caixaCriarPilar = findViewById(R.id.caixaCriarPilar)
        caixaEditarPilar = findViewById(R.id.caixaEditarPilar)

        btnAddPilar.setOnClickListener {
            toggleCaixaCriarPilar()
            toggleCaixaEditarPilar()
        }

        caixaCriarPilar.setOnClickListener {
            val intent = Intent(this, NovoPilarActivity::class.java)
            startActivity(intent)
        }

        caixaEditarPilar.setOnClickListener {
            val intent = Intent(this, EditarActivity::class.java)
            startActivity(intent)
        }

        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            viewPager.setCurrentItem(0, true) // Volta para "Meus Pilares"
        }

        // 🔔 Ação para botão de notificações
        val btnNotificacoes = findViewById<ImageView>(R.id.btnNotificacoes)
        btnNotificacoes.setOnClickListener {
            val intent = Intent(this, NotificacoesActivity::class.java)
            intent.putExtra("ID_USUARIO", idUsuario)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        caixaCriarPilar.visibility = View.GONE
        caixaEditarPilar.visibility = View.GONE
    }

    private fun toggleCaixaCriarPilar() {
        caixaCriarPilar.visibility =
            if (caixaCriarPilar.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    private fun toggleCaixaEditarPilar() {
        caixaEditarPilar.visibility =
            if (caixaEditarPilar.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}

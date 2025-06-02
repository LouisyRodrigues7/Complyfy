package com.example.senacplanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import java.util.Calendar
import java.util.concurrent.TimeUnit

class CoordenadorActivity : AppCompatActivity() {
    private lateinit var caixaCriarPilar: TextView
    private lateinit var caixaEditarPilar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coordenador)

        val db = DatabaseHelper(this)
        db.verificarPilaresProximosDaConclusao()
        db.verificarAtividadesProximasDaConclusao()

        // val db = DatabaseHelper(this)
        // db.verificarNotificacoesDePilaresProximos()

        // Recuperar nome do usuário
        val nomeUsuario = intent.getStringExtra("NOME_USUARIO")
        val idUsuario = intent.getIntExtra("ID_USUARIO", -1)
        val tipoUsuario = intent.getStringExtra("TIPO_USUARIO")

        Log.d("DASHBOARD", "tipoUsuario=$tipoUsuario, idUsuario=$idUsuario, nomeUsuario=$nomeUsuario")



        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Olá, $nomeUsuario"
        setSupportActionBar(toolbar)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(MeusPilaresFragment(), "Meus Pilares")
        adapter.addFragment(TodosPilaresFragment(), "Todos os Pilares")
        viewPager.adapter = adapter

        val paginaHome = intent.getIntExtra("PAGINA_HOME", 0)
        viewPager.setCurrentItem(paginaHome, false)


        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()

        val btnAddPilar = findViewById<ImageView>(R.id.btnAddPilar)
        caixaCriarPilar = findViewById(R.id.caixaCriarPilar)
        caixaEditarPilar = findViewById(R.id.caixaEditarPilar)

        if (tipoUsuario == "Apoio") {
            btnAddPilar.visibility = View.GONE
        }

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

        val btnGraficos = findViewById<ImageView>(R.id.btnGraficos)
        btnGraficos.setOnClickListener {
            if (tipoUsuario != null && nomeUsuario != null && idUsuario != -1) {
                val intent = Intent(this, DashboardGraficoActivity::class.java).apply {
                    putExtra("TIPO_USUARIO", tipoUsuario)
                    putExtra("ID_USUARIO", idUsuario)
                    putExtra("NOME_USUARIO", nomeUsuario)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Dados do usuário ausentes. Não foi possível abrir os gráficos.", Toast.LENGTH_LONG).show()
            }
        }

        val btnNotificacoes = findViewById<ImageView>(R.id.btnNotificacoes)
        btnNotificacoes.setOnClickListener {
            val intent = Intent(this, NotificacoesActivity::class.java).apply {
                putExtra("TIPO_USUARIO", tipoUsuario)
                putExtra("ID_USUARIO", idUsuario)
                putExtra("NOME_USUARIO", nomeUsuario)
            }
            startActivity(intent)
        }

        val btnLogout = findViewById<ImageView>(R.id.btnAcoes)
        btnLogout.setOnClickListener {
            realizarLogout()
        }

        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            com.example.senacplanner.util.NavigationUtils.irParaTelaHome(
                this,
                tipoUsuario,
                idUsuario,
                nomeUsuario
            )
        }

    }

    private fun realizarLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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

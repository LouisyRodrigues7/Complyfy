package com.example.pivot.ui

import android.content.Intent
import android.graphics.Typeface
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
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.pivot.*
import com.example.pivot.data.DatabaseHelper
import com.example.pivot.editarpilar.EditarActivity
import com.example.pivot.fragmentpilares.MeusPilaresFragment
import com.example.pivot.fragmentpilares.TodosPilaresFragment
import com.example.pivot.fragmentpilares.ViewPagerAdapter
import com.example.pivot.novopilar.NovoPilarActivity
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import android.view.ViewGroup


/**
 * Tela principal exibida para usuários do tipo "Coordenador e Apoio".
 *
 * Essa activity gerencia o painel com abas ("Meus Pilares", "Todos os Pilares"),
 * botões de criação, edição, gráficos e notificações, além de um tour interativo.
 * Os pilares representam elementos do planejamento estratégico, com ações e atividades vinculadas.
 */
class CoordenadorActivity : AppCompatActivity() {

    /** Caixa de texto para criação de novo pilar (tooltip visual) */
    private lateinit var caixaCriarPilar: TextView

    /** Caixa de texto para edição de pilar existente (tooltip visual) */
    private lateinit var caixaEditarPilar: TextView

    /**
     * Inicializa os elementos da tela, configura o ViewPager, Toolbar e ações dos botões.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coordenador)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val btnAjuda = findViewById<ImageView>(R.id.btnAjuda)
        val btnAddPilar = findViewById<ImageView?>(R.id.btnAddPilar)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        val nomeUsuario = intent.getStringExtra("NOME_USUARIO")
        val idUsuario = intent.getIntExtra("ID_USUARIO", -1)
        val tipoUsuario = intent.getStringExtra("TIPO_USUARIO")

        toolbar.title = "Olá, $nomeUsuario"
        setSupportActionBar(toolbar)

        val adapter = ViewPagerAdapter(this).apply {
            addFragment(MeusPilaresFragment(), "Meus Pilares")
            addFragment(TodosPilaresFragment(), "Todos os Pilares")
        }
        viewPager.adapter = adapter

        val paginaHome = intent.getIntExtra("PAGINA_HOME", 0)
        viewPager.setCurrentItem(paginaHome, false)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()

        // Inicia sequência de tour guiado explicando funcionalidades visuais
        btnAjuda.setOnClickListener {
            val tab0View = tabLayout.getTabAt(0)?.view
            val tab1View = tabLayout.getTabAt(1)?.view

            val targets = mutableListOf<TapTarget>()

            // Só adiciona se btnAddPilar estiver visível (não Apoio)
            if (btnAddPilar.visibility == View.VISIBLE) {
                targets.add(
                    TapTarget.forView(btnAddPilar, "Criar/Editar Pilar", "Toque aqui para criar ou editar pilares do seu planejamento.")
                        .outerCircleColorInt(ContextCompat.getColor(this, R.color.purple_500))
                        .targetCircleColorInt(ContextCompat.getColor(this, android.R.color.white))
                        .titleTextColorInt(ContextCompat.getColor(this, android.R.color.white))
                        .descriptionTextColorInt(ContextCompat.getColor(this, android.R.color.white))
                        .textTypeface(Typeface.SANS_SERIF)
                        .dimColorInt(ContextCompat.getColor(this, android.R.color.black))
                        .drawShadow(true)
                        .cancelable(true)
                )
            }

            if (tab0View != null) {
                targets.add(
                    TapTarget.forView(tab0View, "Meus Pilares", "Mostra só os pilares que você tem atividades como responsável.")
                        .outerCircleColorInt(ContextCompat.getColor(this, R.color.teal_200))
                        .targetCircleColorInt(ContextCompat.getColor(this, android.R.color.white))
                        .titleTextColorInt(ContextCompat.getColor(this, android.R.color.white))
                        .descriptionTextColorInt(ContextCompat.getColor(this, android.R.color.white))
                        .drawShadow(true)
                        .cancelable(true)
                )
            }

            if (tab1View != null) {
                targets.add(
                    TapTarget.forView(tab1View, "Todos os Pilares", "Lista todos os pilares do sistema com suas ações e atividades.")
                        .outerCircleColorInt(ContextCompat.getColor(this, R.color.teal_700))
                        .targetCircleColorInt(ContextCompat.getColor(this, android.R.color.white))
                        .titleTextColorInt(ContextCompat.getColor(this, android.R.color.white))
                        .descriptionTextColorInt(ContextCompat.getColor(this, android.R.color.white))
                        .drawShadow(true)
                        .cancelable(true)
                )
            }

            targets.add(
                TapTarget.forView(viewPager, "Pilares e Atividades", "Aqui estão os pilares com ações e atividades do seu planejamento.")
                    .outerCircleColorInt(ContextCompat.getColor(this, R.color.teal_800))
                    .targetCircleColorInt(ContextCompat.getColor(this, android.R.color.white))
                    .titleTextColorInt(ContextCompat.getColor(this, android.R.color.white))
                    .descriptionTextColorInt(ContextCompat.getColor(this, android.R.color.white))
                    .drawShadow(true)
                    .cancelable(true)
            )

            TapTargetSequence(this)
                .targets(targets)
                .listener(object : TapTargetSequence.Listener {
                    override fun onSequenceFinish() {
                        Toast.makeText(this@CoordenadorActivity, "Tour finalizado 🎉", Toast.LENGTH_SHORT).show()
                    }

                    override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}

                    override fun onSequenceCanceled(lastTarget: TapTarget) {
                        Toast.makeText(this@CoordenadorActivity, "Tour cancelado ❌", Toast.LENGTH_SHORT).show()
                    }
                })
                .start()
        }



        caixaCriarPilar = findViewById(R.id.caixaCriarPilar)
        caixaEditarPilar = findViewById(R.id.caixaEditarPilar)

        // Se o usuário for "Apoio", ele não pode adicionar pilares
        if (tipoUsuario == "Apoio") {
            btnAddPilar.visibility = View.GONE
        }

        // Alterna visibilidade das opções "Criar" e "Editar" ao clicar no botão "+"
        btnAddPilar.setOnClickListener {
            toggleCaixaCriarPilar()
            toggleCaixaEditarPilar()
        }

        caixaCriarPilar.setOnClickListener {
            startActivity(Intent(this, NovoPilarActivity::class.java))
        }

        caixaEditarPilar.setOnClickListener {
            startActivity(Intent(this, EditarActivity::class.java))
        }

        val btnGraficos = findViewById<ImageView>(R.id.btnGraficos)
        btnGraficos.setOnClickListener {
            if (tipoUsuario != null && nomeUsuario != null && idUsuario != -1) {
                Intent(this, GraficosActivity::class.java).apply {
                    putExtra("TIPO_USUARIO", tipoUsuario)
                    putExtra("ID_USUARIO", idUsuario)
                    putExtra("NOME_USUARIO", nomeUsuario)
                    startActivity(this)
                }
            } else {
                Toast.makeText(this, "Dados do usuário ausentes. Não foi possível abrir os gráficos.", Toast.LENGTH_LONG).show()
            }
        }

        val btnNotificacoes = findViewById<ImageView>(R.id.btnNotificacoes)
        btnNotificacoes.setOnClickListener {
            Intent(this, NotificacoesActivity::class.java).apply {
                putExtra("TIPO_USUARIO", tipoUsuario)
                putExtra("ID_USUARIO", idUsuario)
                putExtra("NOME_USUARIO", nomeUsuario)
                startActivity(this)
            }
        }

        val btnLogout = findViewById<ImageView>(R.id.btnAcoes)
        btnLogout.setOnClickListener {
            realizarLogout()
        }

        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            com.example.pivot.utils.NavigationUtils.irParaTelaHome(
                this,
                tipoUsuario,
                idUsuario,
                nomeUsuario
            )
        }

        // Executa verificações automáticas de status dos pilares e atividades
        val db = DatabaseHelper(this)
        db.verificarPilaresProximosDaConclusao()
        db.verificarAtividadesProximasDaConclusao()
        db.verificarAtividadesAtrasadas()

        val testeatrasadas = db.buscarAtividadePorStatus()
        Log.d("atrasadas", testeatrasadas.toString())
    }

    /**
     * Retorna o usuário à tela de login e limpa a pilha de activities.
     */
    private fun realizarLogout() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    /**
     * Sempre que a activity é retomada, as caixas de ação são ocultadas.
     */
    override fun onResume() {
        super.onResume()
        caixaCriarPilar.visibility = View.GONE
        caixaEditarPilar.visibility = View.GONE
    }

    /** Alterna visibilidade da caixa de criação de pilar */
    private fun toggleCaixaCriarPilar() {
        caixaCriarPilar.visibility =
            if (caixaCriarPilar.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    /** Alterna visibilidade da caixa de edição de pilar */
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

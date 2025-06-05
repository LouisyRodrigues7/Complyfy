package com.example.senacplanner.Acoes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.senacplanner.Acoes.Type.AcaoComAtividades
import com.example.senacplanner.NotificacoesActivity
import com.example.senacplanner.data.DatabaseHelper
import com.example.senacplanner.R
import com.example.senacplanner.ui.CoordenadorActivity
import com.example.senacplanner.ui.GraficosActivity
import com.example.senacplanner.ui.LoginActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.content.ContextCompat

class ListaAtividades : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var databaseHelper: DatabaseHelper
    private var pilarId: Int = -1
    private var pilarNumero: Int = -1
    private var pilarNome: String? = null
    private var idUsuario: Int = -1
    private var idAcao: Int = -1
    private var acoes: List<AcaoComAtividades> = emptyList()
    private var visualizacaoGeral: Boolean = false
    private var usuarioTipo: String? = ""
    private var usuarioNome: String = ""
    private lateinit var fabAdicionar: FloatingActionButton

    private fun atualizarListaDeAcoes() {
        acoes = if (visualizacaoGeral) {
            databaseHelper.buscarAcoesEAtividadesPorPilar(pilarId)
        } else {
            databaseHelper.buscarAcoesEAtividadesDoUsuarioPorPilar(pilarId, idUsuario)
        }
        viewPager.adapter = AcoesPagerAdapter(this, acoes, pilarNome, idUsuario, idAcao)
        if (acoes.isNotEmpty()) {
           val ultimaPosicao = acoes.size - 1
           viewPager.setCurrentItem(ultimaPosicao, false)
           idAcao = acoes[ultimaPosicao].acao.id
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_atividades)
        Log.d("ListaAtividades", "onCreate: pilarId=$pilarId, idUsuario=$idUsuario, visualizacaoGeral=$visualizacaoGeral")

        val btnGraficos = findViewById<ImageView>(R.id.btnGraficos)
        val btnHome = findViewById<ImageView>(R.id.btnHome)
        val btnNotificacoes = findViewById<ImageView>(R.id.btnNotificacoes)
        val btnAcoes = findViewById<ImageView>(R.id.btnAcoes)

        val fab = findViewById<FloatingActionButton>(R.id.fabAdicionar)
        fab.setColorFilter(ContextCompat.getColor(this, android.R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)

        pilarId = intent.getIntExtra("PILAR_ID", -1)
        pilarNumero = intent.getIntExtra("PILAR_NUMERO", -1)
        pilarNome = intent.getStringExtra("PILAR_NOME")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)
        usuarioTipo = intent.getStringExtra("TIPO_USUARIO").toString()
        visualizacaoGeral = intent.getBooleanExtra("VISUALIZACAO_GERAL", false)
        usuarioNome = intent.getStringExtra("NOME_USUARIO").toString()

        btnGraficos.setOnClickListener {
            val intent = Intent(this, GraficosActivity::class.java)
            intent.putExtra("NOME_USUARIO", usuarioNome)
            intent.putExtra("ID_USUARIO", idUsuario)
            intent.putExtra("TIPO_USUARIO", usuarioTipo)
            startActivity(intent)
        }

        btnHome.setOnClickListener {
           finish()
        }

        btnNotificacoes.setOnClickListener {
            val intent = Intent(this, NotificacoesActivity::class.java)
            intent.putExtra("ID_USUARIO", idUsuario)
            intent.putExtra("NOME_USUARIO", usuarioNome)
            intent.putExtra("TIPO_USUARIO", usuarioTipo)
            startActivity(intent)
        }

        btnAcoes.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        if (pilarId == -1) return

        databaseHelper = DatabaseHelper(this)
        viewPager = findViewById(R.id.viewPager)
        fabAdicionar = findViewById(R.id.fabAdicionar)

        fabAdicionar.setOnClickListener {
            Log.d("ListaAtividades", "Botão adicionar clicado. idAcao = $idAcao, idUsuario = $idUsuario")

            if (idUsuario == -1) {
                Toast.makeText(this, "Erro ao obter o contexto do usuário", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val options = mutableListOf<String>()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Escolha uma opção")

            if (idAcao != -1) {
                options.add("Criar Atividade")
            }
            options.add("Criar Ação")

            builder.setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Criar Atividade" -> {
                        Log.d("ListaAtividades", "Abrindo CriarAtividadeActivity com ACAO_ID=$idAcao e USUARIO_ID=$idUsuario")
                        val intent = Intent(this, CriarAtividadeActivity::class.java)
                        intent.putExtra("ACAO_ID", idAcao)
                        intent.putExtra("ID_USUARIO", idUsuario)
                        intent.putExtra("TIPO_USUARIO", usuarioTipo)
                        startActivity(intent)
                    }
                    "Criar Ação" -> {
                        val intent = Intent(this, CriarAcaoActivity::class.java)
                        intent.putExtra("PILAR_ID", pilarId)
                        intent.putExtra("ID_USUARIO", idUsuario)
                        intent.putExtra("TIPO_USUARIO", usuarioTipo)
                        startActivityForResult(intent, 100)
                    }
                }
            }
            builder.show()
        }

        // Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.title = "Pilar $pilarNumero - $pilarNome"
        toolbar.setNavigationOnClickListener {
            finish()
        }

        carregarDados()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            atualizarListaDeAcoes() // função que recarrega os dados do banco
        }
    }

    override fun onResume() {
        super.onResume()
        carregarDados()
    }

    private fun carregarDados() {
        acoes = if (visualizacaoGeral) {
            Log.d("ListaAtividades", "carregarDados: ações carregadas = ${acoes.size}")
            databaseHelper.buscarAcoesEAtividadesPorPilar(pilarId)
        } else {
            databaseHelper.buscarAcoesEAtividadesDoUsuarioPorPilar(pilarId, idUsuario)
        }

        val currentItem = if (::viewPager.isInitialized) {
            val pos = viewPager.currentItem
            if (pos >= acoes.size) acoes.size - 1 else pos
        } else {
            0
        }
        viewPager.adapter = AcoesPagerAdapter(this, acoes, pilarNome, idUsuario, idAcao)
        viewPager.setCurrentItem(currentItem, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position < acoes.size) {
                    idAcao = acoes[position].acao.id
                    Log.d("ListaAtividades", "ID da ação atual (pelo swipe): $idAcao")
                }
            }
        })

        if (acoes.isNotEmpty()) {
            val acaoAtual = acoes[currentItem]
            idAcao = acaoAtual.acao.id
        } else {
            idAcao = -1
        }


    }
}

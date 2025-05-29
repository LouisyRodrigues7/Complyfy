package com.example.senacplanner.Acoes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.senacplanner.Acoes.Type.AcaoComAtividades
import com.example.senacplanner.DatabaseHelper
import com.example.senacplanner.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListaAtividades : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var databaseHelper: DatabaseHelper
    private var pilarId: Int = -1
    private var pilarNumero: Int = -1
    private var pilarNome: String? = null
    private var usuarioTipo: String? = ""
    private var idUsuario: Int = -1
    private var idAcao: Int = -1
    private var acoes: List<AcaoComAtividades> = emptyList()
    private var visualizacaoGeral: Boolean = false
    private var usuarioTipo: String? = ""

    private lateinit var fabAdicionar: FloatingActionButton

    private fun atualizarListaDeAcoes() {
        acoes = if (visualizacaoGeral) {
            databaseHelper.buscarAcoesEAtividadesPorPilar(pilarId)
        } else {
            databaseHelper.buscarAcoesEAtividadesDoUsuarioPorPilar(pilarId, idUsuario)
        }
        viewPager.adapter = AcoesPagerAdapter(this, acoes, pilarNome, idUsuario)
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


        // Recuperar dados da intent
        pilarId = intent.getIntExtra("PILAR_ID", -1)
        pilarNumero = intent.getIntExtra("PILAR_NUMERO", -1)
        pilarNome = intent.getStringExtra("PILAR_NOME")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)
        usuarioTipo = intent.getStringExtra("TIPO_USUARIO").toString()
        visualizacaoGeral = intent.getBooleanExtra("VISUALIZACAO_GERAL", false)

        if (pilarId == -1) return

        databaseHelper = DatabaseHelper(this)
        viewPager = findViewById(R.id.viewPager)
        fabAdicionar = findViewById(R.id.fabAdicionar)

        fabAdicionar.setOnClickListener {
            Log.d("ListaAtividades", "Botão adicionar clicado. idAcao = $idAcao, idUsuario = $idUsuario")
            if (idAcao == -1 || idUsuario == -1) {
                Toast.makeText(this, "Erro ao obter o contexto da ação ou usuário", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val options = arrayOf("Criar Atividade", "Criar Ação")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Escolha uma opção")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        Log.d("ListaAtividades", "Abrindo CriarAtividadeActivity com ACAO_ID=$idAcao e USUARIO_ID=$idUsuario")
                        val intent = Intent(this, CriarAtividadeActivity::class.java)
                        intent.putExtra("ACAO_ID", idAcao)
                        intent.putExtra("ID_USUARIO", idUsuario)
                        intent.putExtra("TIPO_USUARIO", usuarioTipo)
                        startActivity(intent)
                    }
                    1 -> {
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

        val currentItem = if (::viewPager.isInitialized) viewPager.currentItem else 0
        viewPager.adapter = AcoesPagerAdapter(this, acoes, pilarNome, idUsuario)
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
            Log.d("ListaAtividades", "ID da ação atual (inicial): $idAcao")
        }
    }
}

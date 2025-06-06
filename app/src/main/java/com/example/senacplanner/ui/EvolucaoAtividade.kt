package com.example.senacplanner.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.NotificacoesActivity
import com.example.senacplanner.R
import com.example.senacplanner.data.DatabaseHelper
import com.example.senacplanner.model.AcaoEstrategica
import com.example.senacplanner.model.Atividadespinner
import com.example.senacplanner.model.Pilarspinner

class EvolucaoAtividade : AppCompatActivity() {

    private lateinit var spinnerPilar: Spinner
    private lateinit var spinnerAcao: Spinner
    private lateinit var spinnerAtividade: Spinner
    private lateinit var btnConfirmar: Button

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var btnHome: ImageView
    private lateinit var btnGraficos: ImageView
    private lateinit var btnNotificacoes: ImageView
    private lateinit var btnLogout: ImageView

    private var tipoUsuario: String? = null
    private var nomeUsuario: String? = null
    private var idUsuario: Int = -1

    private var listaPilares: List<Pilarspinner> = emptyList()
    private var listaAcoes: List<AcaoEstrategica> = emptyList()
    private var listaAtividades: List<Atividadespinner> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evolucao_atividade)

        nomeUsuario = intent.getStringExtra("NOME_USUARIO")
        tipoUsuario = intent.getStringExtra("TIPO_USUARIO")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        btnHome = findViewById(R.id.btnHome)
        btnGraficos = findViewById(R.id.btnGraficos)
        btnNotificacoes = findViewById(R.id.btnNotificacoes)
        btnLogout = findViewById(R.id.btnAcoes)

        val btnGraficos = findViewById<ImageView>(R.id.btnGraficos)
        btnGraficos.setOnClickListener {
            if (tipoUsuario != null && nomeUsuario != null && idUsuario != -1) {
                val intent = Intent(this, GraficosActivity::class.java).apply {
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
            com.example.senacplanner.utils.NavigationUtils.irParaTelaHome(
                this,
                tipoUsuario,
                idUsuario,
                nomeUsuario
            )
        }


        dbHelper = DatabaseHelper(this)

        spinnerPilar = findViewById(R.id.spinnerPilar)
        spinnerAcao = findViewById(R.id.spinnerAcao)
        spinnerAtividade = findViewById(R.id.spinnerAtividade)
        btnConfirmar = findViewById(R.id.btnBuscar)

        configurarSpinners()
        configurarBotoes()
    }


    private fun configurarSpinners() {
        carregarPilaresParaSelecao()

        spinnerPilar.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val pilarSelecionado = listaPilares[position]
                carregarAcoesPorPilarParaSelecao(pilarSelecionado.id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerAcao.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val acaoSelecionada = listaAcoes[position]
                carregarAtividadesPorAcaoParaSelecao(acaoSelecionada.id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun carregarPilaresParaSelecao() {
        listaPilares = dbHelper.buscarPilaresParaSelecao()
        val nomesPilares = listaPilares.map { it.nome }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nomesPilares)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPilar.adapter = adapter
    }

    private fun carregarAcoesPorPilarParaSelecao(pilarId: Int) {
        listaAcoes = dbHelper.buscarAcoesPorPilarParaSelecao(pilarId)
        val nomesAcoes = listaAcoes.map { it.nome }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nomesAcoes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAcao.adapter = adapter

        spinnerAtividade.adapter = null
    }

    private fun carregarAtividadesPorAcaoParaSelecao(acaoId: Int) {
        listaAtividades = dbHelper.buscarAtividadesPorAcaoParaSelecao(acaoId)

        val nomesAtividades = mutableListOf("Todas as atividades")
        nomesAtividades.addAll(listaAtividades.map { it.nome })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nomesAtividades)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAtividade.adapter = adapter
    }

    private fun configurarBotoes() {
        btnConfirmar.setOnClickListener {
            val pilarSelecionado = listaPilares.getOrNull(spinnerPilar.selectedItemPosition)
            val acaoSelecionada = listaAcoes.getOrNull(spinnerAcao.selectedItemPosition)
            val atividadePos = spinnerAtividade.selectedItemPosition

            val atividadeSelecionada = if (atividadePos == 0) {
                null // "Todas as atividades"
            } else {
                listaAtividades.getOrNull(atividadePos - 1)
            }

            if (pilarSelecionado == null || acaoSelecionada == null) {
                Toast.makeText(this, "Selecione Pilar e Ação", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Intent para ir para GraficoEvolucaoAtividade
            val intent = Intent(this, GraficoEvolucaoAtividade::class.java)
            intent.putExtra("ACAO_ID", acaoSelecionada.id)

            // Se atividade for selecionada, também envia ID da atividade e do responsável
            atividadeSelecionada?.let {
                intent.putExtra("ATIVIDADE_ID", it.id)
                it.responsavelId?.let { responsavelId ->
                    intent.putExtra("RESPONSAVEL_ID", responsavelId)
                }
            }

            startActivity(intent)
        }

    }
    private fun realizarLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
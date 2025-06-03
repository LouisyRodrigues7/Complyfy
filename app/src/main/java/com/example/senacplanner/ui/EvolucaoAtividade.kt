package com.example.senacplanner.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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

    private var listaPilares: List<Pilarspinner> = emptyList()
    private var listaAcoes: List<AcaoEstrategica> = emptyList()
    private var listaAtividades: List<Atividadespinner> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evolucao_atividade)

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

            val mensagem = if (atividadeSelecionada == null) {
                "Pilar: ${pilarSelecionado.nome}\nAção: ${acaoSelecionada.nome}\nAtividade: Todas"
            } else {
                "Pilar: ${pilarSelecionado.nome}\nAção: ${acaoSelecionada.nome}\nAtividade: ${atividadeSelecionada.nome}"
            }

            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()

            // Redirecionar ou processar conforme necessário
        }
    }
}

package com.example.senacplanner.ui

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.R
import com.example.senacplanner.data.RelatorioDatabaseHelper
import com.example.senacplanner.model.RelatorioPilar
import com.example.senacplanner.model.RelatorioPeriodo
import com.example.senacplanner.utils.RelatorioGenerator

class GerarRelatorio : AppCompatActivity() {

    private lateinit var spinnerPilar: Spinner
    private lateinit var spinnerPeriodo: Spinner
    private lateinit var btnConfirmar: Button
    private lateinit var dbHelper: RelatorioDatabaseHelper

    private var pilaresList = listOf<RelatorioPilar>()
    private var periodosList = listOf<RelatorioPeriodo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gerar_relatorio)

        spinnerPilar = findViewById(R.id.spinnerPilar)
        spinnerPeriodo = findViewById(R.id.spinnerPeriodo)
        btnConfirmar = findViewById(R.id.btnConfirmar)

        // Instância do Database Helper
        dbHelper = RelatorioDatabaseHelper(this)
        val db: SQLiteDatabase = dbHelper.readableDatabase

        // Carregar spinners
        carregarSpinnerPilares(db)
        carregarSpinnerPeriodos()

        // Ação do botão
        btnConfirmar.setOnClickListener {
            val posPilar = spinnerPilar.selectedItemPosition
            val posPeriodo = spinnerPeriodo.selectedItemPosition

            if (posPilar >= 0 && posPeriodo >= 0) {
                val pilarSelecionado = pilaresList[posPilar]
                val periodoSelecionado = periodosList[posPeriodo]

                val listaDePilares = if (pilarSelecionado.id == -1) {
                    dbHelper.buscarPilaresParaRelatorio(db, periodoSelecionado.id)
                } else {
                    dbHelper.buscarPilarPorIdParaRelatorio(db, pilarSelecionado.id, periodoSelecionado.id)
                }

                if (listaDePilares.isEmpty()) {
                    Toast.makeText(this, "Nenhum dado encontrado para gerar o relatório", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val relatorioGenerator = RelatorioGenerator()
                relatorioGenerator.gerarRelatorioPDF(
                    context = this,
                    pilares = listaDePilares,
                    nomeArquivo = "relatorio_compliance"
                )

            } else {
                Toast.makeText(this, "Por favor, selecione Pilar e Período", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun carregarSpinnerPilares(db: SQLiteDatabase) {
        pilaresList = dbHelper.buscarPilares(db)
        val nomesPilares = pilaresList.map { it.nome }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nomesPilares)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPilar.adapter = adapter
    }

    private fun carregarSpinnerPeriodos() {
        periodosList = dbHelper.buscarPeriodosFixos()
        val descricoes = periodosList.map { it.descricao }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, descricoes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPeriodo.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}

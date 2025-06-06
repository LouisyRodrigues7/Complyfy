package com.example.senacplanner.ui

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
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

    private lateinit var layoutBotoesPDF: LinearLayout
    private lateinit var btnAbrirPDF: Button
    private lateinit var btnCompartilharPDF: Button
    private var ultimoArquivoPDFUri: Uri? = null

    private lateinit var dbHelper: RelatorioDatabaseHelper
    private var pilaresList = listOf<RelatorioPilar>()
    private var periodosList = listOf<RelatorioPeriodo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gerar_relatorio)

        // Referência dos componentes da interface
        spinnerPilar = findViewById(R.id.spinnerPilar)
        spinnerPeriodo = findViewById(R.id.spinnerPeriodo)
        btnConfirmar = findViewById(R.id.btnConfirmar)
        layoutBotoesPDF = findViewById(R.id.layoutBotoesPDF)
        btnAbrirPDF = findViewById(R.id.btnAbrirPDF)
        btnCompartilharPDF = findViewById(R.id.btnCompartilharPDF)

        // Inicialmente esconde os botões de ação do PDF
        layoutBotoesPDF.visibility = View.GONE

        // Instancia banco
        dbHelper = RelatorioDatabaseHelper(this)
        val db: SQLiteDatabase = dbHelper.readableDatabase

        // Preenche os spinners
        carregarSpinnerPilares(db)
        carregarSpinnerPeriodos()

        // Configura botão de confirmar (gera o PDF)
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

                // Gera o PDF
                val relatorioGenerator = RelatorioGenerator()
                relatorioGenerator.gerarRelatorioPDF(
                    context = this,
                    pilares = listaDePilares,
                    nomeArquivo = "relatorio_compliance"
                ) { uri ->
                    if (uri != null) {
                        ultimoArquivoPDFUri = uri
                        layoutBotoesPDF.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this, "Erro ao gerar o PDF", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "Por favor, selecione Pilar e Período", Toast.LENGTH_SHORT).show()
            }
        }

        // Botão abrir PDF
        btnAbrirPDF.setOnClickListener {
            ultimoArquivoPDFUri?.let { uri ->
                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(Intent.createChooser(openIntent, "Abrir PDF com..."))
            }
        }

        // Botão compartilhar PDF
        btnCompartilharPDF.setOnClickListener {
            ultimoArquivoPDFUri?.let { uri ->
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                startActivity(Intent.createChooser(shareIntent, "Compartilhar PDF via"))
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

package com.example.senacplanner.ui

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.senacplanner.NotificacoesActivity
import com.example.senacplanner.R
import com.example.senacplanner.data.RelatorioDatabaseHelper
import com.example.senacplanner.model.RelatorioPilar
import com.example.senacplanner.model.RelatorioPeriodo
import com.example.senacplanner.model.HistoricoRelatorio
import com.example.senacplanner.utils.HistoricoRelatorioManager
import com.example.senacplanner.utils.RelatorioGenerator
import com.example.senacplanner.utils.NavigationUtils

class GerarRelatorio : AppCompatActivity() {

    private lateinit var spinnerPilar: Spinner
    private lateinit var spinnerPeriodo: Spinner
    private lateinit var btnConfirmar: Button

    private var tipoUsuario: String? = null
    private var nomeUsuario: String? = null
    private var idUsuario: Int = -1

    private lateinit var layoutBotoesPDF: LinearLayout
    private lateinit var btnAbrirPDF: Button
    private lateinit var btnCompartilharPDF: Button
    private var ultimoArquivoPDFUri: Uri? = null

    private lateinit var textRecentes: TextView
    private lateinit var listaRecentes: LinearLayout

    private lateinit var dbHelper: RelatorioDatabaseHelper
    private var pilaresList = listOf<RelatorioPilar>()
    private var periodosList = listOf<RelatorioPeriodo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gerar_relatorio)

        // Inicializa os elementos da UI
        textRecentes = findViewById(R.id.textRecentes)
        listaRecentes = findViewById(R.id.listaRecentes)
        spinnerPilar = findViewById(R.id.spinnerPilar)
        spinnerPeriodo = findViewById(R.id.spinnerPeriodo)
        btnConfirmar = findViewById(R.id.btnConfirmar)
        layoutBotoesPDF = findViewById(R.id.layoutBotoesPDF)
        btnAbrirPDF = findViewById(R.id.btnAbrirPDF)
        btnCompartilharPDF = findViewById(R.id.btnCompartilharPDF)

        nomeUsuario = intent.getStringExtra("NOME_USUARIO")
        tipoUsuario = intent.getStringExtra("TIPO_USUARIO")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        layoutBotoesPDF.visibility = View.GONE

        dbHelper = RelatorioDatabaseHelper(this)
        val db: SQLiteDatabase = dbHelper.readableDatabase

        carregarSpinnerPilares(db)
        carregarSpinnerPeriodos()

        mostrarHistorico()

        findViewById<ImageView>(R.id.btnGraficos).setOnClickListener {
            if (tipoUsuario != null && nomeUsuario != null && idUsuario != -1) {
                val intent = Intent(this, GraficosActivity::class.java).apply {
                    putExtra("TIPO_USUARIO", tipoUsuario)
                    putExtra("ID_USUARIO", idUsuario)
                    putExtra("NOME_USUARIO", nomeUsuario)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Dados do usuário ausentes.", Toast.LENGTH_LONG).show()
            }
        }

        findViewById<ImageView>(R.id.btnNotificacoes).setOnClickListener {
            val intent = Intent(this, NotificacoesActivity::class.java).apply {
                putExtra("TIPO_USUARIO", tipoUsuario)
                putExtra("ID_USUARIO", idUsuario)
                putExtra("NOME_USUARIO", nomeUsuario)
            }
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.btnAcoes).setOnClickListener {
            realizarLogout()
        }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            NavigationUtils.irParaTelaHome(this, tipoUsuario, idUsuario, nomeUsuario)
        }

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
                    Toast.makeText(this, "Nenhum dado encontrado.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val relatorioGenerator = RelatorioGenerator()
                relatorioGenerator.gerarRelatorioPDF(
                    context = this,
                    pilares = listaDePilares,
                    nomeArquivo = "relatorio_compliance"
                ) { uri ->
                    if (uri != null) {
                        HistoricoRelatorioManager.salvar(
                            this,
                            HistoricoRelatorio("relatorio_compliance", uri.toString(), System.currentTimeMillis())
                        )
                        mostrarHistorico()
                        ultimoArquivoPDFUri = uri
                        layoutBotoesPDF.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this, "Erro ao gerar PDF", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "Selecione Pilar e Período", Toast.LENGTH_SHORT).show()
            }
        }

        btnAbrirPDF.setOnClickListener {
            ultimoArquivoPDFUri?.let { uri ->
                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(Intent.createChooser(openIntent, "Abrir PDF com..."))
            }
        }

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

    private fun realizarLogout() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun mostrarHistorico() {
        val historico = HistoricoRelatorioManager.carregar(this)
        if (historico.isEmpty()) {
            textRecentes.visibility = View.GONE
            listaRecentes.visibility = View.GONE
            return
        }

        textRecentes.visibility = View.VISIBLE
        listaRecentes.visibility = View.VISIBLE
        listaRecentes.removeAllViews()

        historico.forEach { item ->
            val btn = Button(this).apply {
                text = "${item.nome} (${android.text.format.DateFormat.format("dd/MM/yyyy", item.data)})"
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                setTextColor(Color.WHITE)
                setOnClickListener {
                    try {
                        val openIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(Uri.parse(item.uri), "application/pdf")
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(Intent.createChooser(openIntent, "Abrir PDF com..."))
                    } catch (e: Exception) {
                        Toast.makeText(this@GerarRelatorio, "Erro ao abrir PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            listaRecentes.addView(btn)
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}

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

/**
 * Tela responsável pela geração de relatórios em PDF,
 * com filtros por pilar e período, e visualização do histórico de relatórios gerados.
 */
class GerarRelatorio : AppCompatActivity() {

    /** Spinner para selecionar um pilar estratégico */
    private lateinit var spinnerPilar: Spinner

    /** Spinner para selecionar o período desejado */
    private lateinit var spinnerPeriodo: Spinner

    /** Botão de confirmação para gerar o relatório */
    private lateinit var btnConfirmar: Button

    private var tipoUsuario: String? = null
    private var nomeUsuario: String? = null
    private var idUsuario: Int = -1

    /** Layout que contém os botões de abrir e compartilhar PDF */
    private lateinit var layoutBotoesPDF: LinearLayout

    /** Botão que permite abrir o PDF recém-gerado */
    private lateinit var btnAbrirPDF: Button

    /** Botão que permite compartilhar o PDF recém-gerado */
    private lateinit var btnCompartilharPDF: Button

    /** URI do último arquivo PDF gerado */
    private var ultimoArquivoPDFUri: Uri? = null

    /** Título da seção de histórico */
    private lateinit var textRecentes: TextView

    /** Container visual para os botões de histórico */
    private lateinit var listaRecentes: LinearLayout

    /** Helper para banco de dados de relatórios */
    private lateinit var dbHelper: RelatorioDatabaseHelper

    /** Lista de pilares disponíveis para filtro */
    private var pilaresList = listOf<RelatorioPilar>()

    /** Lista de períodos disponíveis para filtro */
    private var periodosList = listOf<RelatorioPeriodo>()

    /**
     * Inicializa a interface, listeners e popula os spinners.
     */
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

        // Navegação padrão (já documentada em KTs anteriores)
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

        val layoutTituloRecentes = findViewById<LinearLayout>(R.id.layoutTituloRecentes)
        val layoutRecentes = findViewById<LinearLayout>(R.id.layoutRecentes)
        val listaRecentes = findViewById<LinearLayout>(R.id.listaRecentes)
        val setaExpandir = findViewById<ImageView>(R.id.setaExpandir)

// Expansão e recolhimento da seção de históricos
        layoutTituloRecentes.setOnClickListener {
            val expandido = listaRecentes.visibility == View.VISIBLE
            listaRecentes.visibility = if (expandido) View.GONE else View.VISIBLE
            setaExpandir.setImageResource(
                if (expandido) R.drawable.ic_arrow_drop_down else R.drawable.ic_arrow_drop_up
            )
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

    /**
     * Preenche o spinner de pilares disponíveis para relatório.
     */
    private fun carregarSpinnerPilares(db: SQLiteDatabase) {
        pilaresList = dbHelper.buscarPilares(db)
        val nomesPilares = pilaresList.map { it.nome }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nomesPilares)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPilar.adapter = adapter
    }

    /**
     * Preenche o spinner com os períodos disponíveis.
     */
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

    /**
     * Exibe os relatórios gerados recentemente na interface.
     * Permite reabrir qualquer PDF salvo no histórico local.
     */
    private fun mostrarHistorico() {
        val historico = HistoricoRelatorioManager.carregar(this)
        val layoutRecentes = findViewById<LinearLayout>(R.id.layoutRecentes)
        val layoutTituloRecentes = findViewById<LinearLayout>(R.id.layoutTituloRecentes)
        val listaRecentes = findViewById<LinearLayout>(R.id.listaRecentes)
        val textRecentes = findViewById<TextView>(R.id.textRecentes)

        if (historico.isEmpty()) {
            layoutRecentes.visibility = View.GONE
            return
        }

        layoutRecentes.visibility = View.VISIBLE
        layoutTituloRecentes.visibility = View.VISIBLE
        textRecentes.visibility = View.VISIBLE
        listaRecentes.removeAllViews()

        historico.forEach { item ->
            // Nome amigável (mascarado)
            val nomeAmigavel = when (item.nome) {
                "relatorio_compliance" -> "Relatório de Compliance"
                "relatorio_financeiro" -> "Relatório Financeiro"
                else -> "Relatório"
            }

            // Container visual para cada item
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 12, 16, 12)
                background = ContextCompat.getDrawable(context, R.drawable.bg_item_relatorio)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 12)
                }
                elevation = 2f
            }

            val titulo = TextView(this).apply {
                text = nomeAmigavel
                setTextColor(Color.WHITE)
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val data = TextView(this).apply {
                text = "Data: ${android.text.format.DateFormat.format("dd/MM/yyyy", item.data)}"
                setTextColor(Color.parseColor("#DDDDDD"))
                textSize = 13f
                setPadding(0, 4, 0, 4)
            }

            val btnAbrir = Button(this).apply {
                text = "Visualizar"
                textSize = 13f
                setBackgroundColor(Color.parseColor("#2980B9"))
                setTextColor(Color.WHITE)
                setPadding(0, 4, 0, 4)
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

            itemLayout.addView(titulo)
            itemLayout.addView(data)
            itemLayout.addView(btnAbrir)

            listaRecentes.addView(itemLayout)
        }
    }


    /**
     * Garante que o banco de dados seja fechado ao destruir a activity.
     */
    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}

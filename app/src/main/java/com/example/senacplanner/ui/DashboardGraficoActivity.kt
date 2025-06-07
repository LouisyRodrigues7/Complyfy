package com.example.senacplanner.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.model.AcaoComProgresso
import com.example.senacplanner.model.PilarComProgresso
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.components.Legend
import android.widget.ImageView
import android.content.Intent
import android.widget.Toast
import com.example.senacplanner.data.DatabaseHelper
import com.example.senacplanner.NotificacoesActivity
import com.example.senacplanner.R
import android.widget.ScrollView

/**
 * Tela de gráficos que exibe visualizações do progresso dos pilares ou das ações,
 * dependendo se foi informado um ID de pilar via intent.
 */
class DashboardGraficoActivity : AppCompatActivity() {

    /** Gráfico de pizza que mostra progresso geral de todos os pilares */
    private lateinit var pieChart: PieChart

    /** Gráfico de barras que mostra status das ações de um pilar específico */
    private lateinit var barChart: BarChart

    /** Instância de acesso ao banco de dados local */
    private lateinit var db: DatabaseHelper

    /** Container que exibe a legenda explicativa do gráfico de barras */
    private lateinit var legendaContainer: LinearLayout

    /** Scroll da legenda do gráfico de barras */
    private lateinit var legendaScroll: ScrollView

    /** Layout que contém barra de progresso total dos pilares */
    private lateinit var progressoContainer: LinearLayout

    /** Componente visual da barra de progresso total */
    private lateinit var progressBarTotal: ProgressBar

    /** Texto que mostra o percentual e contagem total de progresso */
    private lateinit var tvProgressoTotal: TextView

    /** ID do pilar selecionado (ou -1 para exibir todos os pilares) */
    private var pilarId: Int = -1

    /** Tipo do usuário logado */
    private var tipoUsuario: String? = null

    /** Nome do usuário logado */
    private var nomeUsuario: String? = null

    /** ID do usuário logado */
    private var idUsuario: Int = -1

    /**
     * Inicializa os componentes da tela e determina se deve exibir gráfico de pizza ou de barras,
     * com base no ID de pilar recebido via intent.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_grafico)

        nomeUsuario = intent.getStringExtra("NOME_USUARIO")
        tipoUsuario = intent.getStringExtra("TIPO_USUARIO")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        pieChart = findViewById(R.id.pieChart)
        progressoContainer = findViewById(R.id.progressoContainer)
        progressBarTotal = findViewById(R.id.progressBarTotal)
        tvProgressoTotal = findViewById(R.id.tvProgressoTotal)
        barChart = findViewById(R.id.barChart)
        legendaContainer = findViewById(R.id.barChartLegendContainer)
        legendaScroll = findViewById(R.id.barChartLegendScroll) as ScrollView

        db = DatabaseHelper(this)
        pilarId = intent.getIntExtra("pilar_id", -1)

        if (pilarId == -1) {
            // Mostrar gráfico de pizza
            val lista = db.getProgressoTodosPilares()
            pieChart.visibility = View.VISIBLE
            barChart.visibility = View.GONE
            legendaScroll.visibility = View.GONE
            mostrarGraficoPilares(lista)
        } else {
            // Mostrar gráfico de barras
            val lista = db.getAcoesComAtrasoDoPilar(pilarId)
            pieChart.visibility = View.GONE
            barChart.visibility = View.VISIBLE
            mostrarGraficoAcoesComAtraso(lista)
            progressoContainer.visibility = View.GONE
        }

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
                Toast.makeText(
                    this,
                    "Dados do usuário ausentes. Não foi possível abrir os gráficos.",
                    Toast.LENGTH_LONG
                ).show()
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

    }

    /**
     * Redireciona para a tela de login e finaliza a sessão atual.
     */
    private fun realizarLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Exibe um gráfico de pizza com o progresso geral de todos os pilares.
     * @param lista Lista de pilares com contagem de atividades concluídas e totais.
     */
    private fun mostrarGraficoPilares(lista: List<PilarComProgresso>) {
        val totalConcluidasGeral = lista.sumOf { it.concluidas }
        val totalAtividades = lista.sumOf { it.total }

        // Esconde a barra de progresso se não houver atividades
        if (totalAtividades == 0) {
            pieChart.clear()
            pieChart.setNoDataText("Nenhuma atividade encontrada")
            progressoContainer.visibility = View.GONE
            return
        }

        // Mostra e atualiza a barra de progresso
        val percentual = (totalConcluidasGeral * 100f / totalAtividades).toInt()
        progressoContainer.visibility = View.VISIBLE
        progressBarTotal.progress = percentual
        tvProgressoTotal.text =
            "Progresso Total: $totalConcluidasGeral de $totalAtividades atividades concluídas ($percentual%)"

        val entries = mutableListOf<PieEntry>()

        for (pilar in lista) {
            if (pilar.concluidas > 0) {
                val label = "${pilar.nome} (${pilar.concluidas}/${pilar.total})"
                entries.add(PieEntry(pilar.concluidas.toFloat(), label))
            }
        }

        val dataSet = PieDataSet(entries, "Atividades Concluídas por Pilar").apply {
            colors = ColorTemplate.COLORFUL_COLORS.toList()
            sliceSpace = 3f
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }

        pieChart.apply {
            data = PieData(dataSet)
            setUsePercentValues(false)
            setEntryLabelColor(Color.DKGRAY)
            setEntryLabelTextSize(12f)
            description.isEnabled = false

            legend.apply {
                isEnabled = true
                textSize = 14f
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                xEntrySpace = 7f
                yEntrySpace = 5f
            }

            invalidate()
        }
    }

    /**
     * Exibe gráfico de barras empilhadas com dados de progresso das ações de um pilar.
     * @param lista Lista de ações com dados de conclusão, andamento e atraso.
     */
    private fun mostrarGraficoAcoesComAtraso(lista: List<AcaoComProgresso>) {
        val entries = ArrayList<BarEntry>()
        val siglas = ArrayList<String>()
        val legendaMap = mutableMapOf<String, String>()

        lista.forEachIndexed { index, acao ->
            val sigla = gerarSigla(acao.nome, legendaMap)

            val concluido = acao.concluidas.toFloat()
            val andamento = acao.andamento.toFloat()
            val atrasado = acao.atrasadas.toFloat()

            entries.add(BarEntry(index.toFloat(), floatArrayOf(concluido, andamento, atrasado)))
            siglas.add(sigla)
            legendaMap[sigla] = acao.nome
        }

        if (entries.isEmpty()) {
            barChart.clear()
            barChart.setNoDataText("Nenhuma ação encontrada")
            legendaScroll.visibility = View.GONE
            return
        }

        val dataSet = BarDataSet(entries, "Progresso por Ação").apply {
            colors = listOf(
                Color.parseColor("#4CAF50"), // Concluído
                Color.parseColor("#2196F3"), // Em andamento
                Color.parseColor("#F44336")  // Atrasado
            )
            setStackLabels(arrayOf("Concluído", "Em andamento", "Atrasado"))
            valueTextColor = Color.BLACK
            valueTextSize = 11f
        }

        barChart.apply {
            data = BarData(dataSet).apply { barWidth = 0.7f }
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(siglas)
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = siglas.size
                textSize = 12f
            }

            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            legend.isEnabled = true
            legend.textSize = 12f
            legend.form = Legend.LegendForm.SQUARE
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER

            description.isEnabled = false
            setTouchEnabled(true)
            setFitBars(true)
            invalidate()
        }

        // Atualiza a legenda
        legendaContainer.removeAllViews()

        val legendaHeader = TextView(this).apply {
            text = "Cores: 🟩 Concluído  🟦 Em andamento  🟥 Atrasado"
            setPadding(24, 24, 24, 8)
            setTextColor(Color.BLACK)
            textSize = 16f
            setBackgroundColor(Color.LTGRAY)
        }
        legendaContainer.addView(legendaHeader)

        val legendaTitle = TextView(this).apply {
            text = "\nSigla ➜ Nome da Ação"
            setPadding(24, 8, 24, 8)
            setTextColor(Color.BLACK)
            textSize = 16f
        }
        legendaContainer.addView(legendaTitle)

        legendaMap.forEach { (sigla, nomeCompleto) ->
            val legendaItem = TextView(this).apply {
                text = "$sigla ➜ $nomeCompleto"
                setPadding(24, 16, 24, 16)
                setTextColor(Color.BLACK)
                textSize = 16f
            }
            legendaContainer.addView(legendaItem)
        }

        legendaScroll.visibility = View.VISIBLE
    }

    /**
     * Gera uma sigla única a partir do nome de uma ação para ser usada no eixo X do gráfico.
     * @param nome Nome completo da ação
     * @param mapa Mapa de siglas já utilizadas para garantir unicidade
     * @return Sigla resultante
     */
    private fun gerarSigla(nome: String, mapa: Map<String, String>): String {
        val palavras = nome.split(" ")
            .filter { it.isNotBlank() }
            .map { it.trim().take(1).uppercase() }

        var sigla = palavras.take(3).joinToString("")
        if (sigla.isEmpty()) sigla = nome.take(3).uppercase()

        var contador = 1
        var siglaFinal = sigla
        while (mapa.containsKey(siglaFinal)) {
            siglaFinal = "$sigla$contador"
            contador++
        }
        return siglaFinal
    }
}

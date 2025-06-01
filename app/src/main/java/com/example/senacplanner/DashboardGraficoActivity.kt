package com.example.senacplanner

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.HorizontalScrollView
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

class DashboardGraficoActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var db: DatabaseHelper
    private lateinit var legendaContainer: LinearLayout
    private lateinit var legendaScroll: HorizontalScrollView
    private lateinit var progressoContainer: LinearLayout
    private lateinit var progressBarTotal: ProgressBar
    private lateinit var tvProgressoTotal: TextView
    private var pilarId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_grafico)

        pieChart = findViewById(R.id.pieChart)
        progressoContainer = findViewById(R.id.progressoContainer)
        progressBarTotal = findViewById(R.id.progressBarTotal)
        tvProgressoTotal = findViewById(R.id.tvProgressoTotal)
        barChart = findViewById(R.id.barChart)
        legendaContainer = findViewById(R.id.barChartLegendContainer)
        legendaScroll = findViewById(R.id.barChartLegendScroll)

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
            val lista = db.getAcoesComProgressoDoPilar(pilarId)
            pieChart.visibility = View.GONE
            barChart.visibility = View.VISIBLE
            mostrarGraficoAcoesBarras(lista)
            progressoContainer.visibility = View.GONE
        }
        val tipoUsuario = intent.getStringExtra("TIPO_USUARIO")
        val idUsuario = intent.getIntExtra("ID_USUARIO", -1)
        val nomeUsuario = intent.getStringExtra("NOME_USUARIO")

        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            com.example.senacplanner.util.NavigationUtils.irParaTelaHome(
                this,
                tipoUsuario,
                idUsuario,
                nomeUsuario
            )
        }
        val btnGraficos = findViewById<ImageView>(R.id.btnGraficos)
        btnGraficos.setOnClickListener {
            Toast.makeText(this, "Você já está nos Gráficos!", Toast.LENGTH_SHORT).show()
        }

        val btnNotificacoes = findViewById<ImageView>(R.id.btnNotificacoes)
        btnNotificacoes.setOnClickListener {
            val intent = Intent(this, NotificacoesActivity::class.java)
            intent.putExtra("ID_USUARIO", idUsuario)
            startActivity(intent)
        }

        val btnLogout = findViewById<ImageView>(R.id.btnAcoes)
        btnLogout.setOnClickListener {
            realizarLogout()
        }

    }

    private fun realizarLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

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
        tvProgressoTotal.text = "Progresso Total: $totalConcluidasGeral de $totalAtividades atividades concluídas ($percentual%)"

        val entries = mutableListOf<PieEntry>()

        for (pilar in lista) {
            if (pilar.concluidas > 0) {
                // Exibe o nome do pilar com o progresso (ex: "Educação (5/10)")
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
            setUsePercentValues(false) // mostra valores absolutos (quantidade de atividades)
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




    private fun mostrarGraficoAcoesBarras(lista: List<AcaoComProgresso>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        for ((index, acao) in lista.withIndex()) {
            val progresso = if (acao.total > 0) acao.concluidas * 100f / acao.total else 0f
            entries.add(BarEntry(index.toFloat(), progresso))
            labels.add(acao.nome)
        }

        if (entries.isEmpty()) {
            barChart.clear()
            barChart.setNoDataText("Nenhuma ação encontrada")
            legendaScroll.visibility = View.GONE
            return
        }

        val dataSet = BarDataSet(entries, "Progresso das Ações").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }

        val data = BarData(dataSet).apply {
            barWidth = 0.9f
        }

        barChart.apply {
            this.data = data

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = labels.size
                labelRotationAngle = 0f
                textSize = 11f
            }

            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 100f
            }

            axisRight.isEnabled = false

            legend.isEnabled = false

            setTouchEnabled(true)
            setScaleEnabled(false)
            description.isEnabled = false
            setFitBars(true)
            invalidate()
        }

        legendaContainer.removeAllViews()

        labels.forEachIndexed { index, nome ->
            val legendaItem = TextView(this).apply {
                text = nome
                setPadding(24, 12, 24, 12)
                setTextColor(Color.WHITE)
                setBackgroundColor(ColorTemplate.MATERIAL_COLORS[index % ColorTemplate.MATERIAL_COLORS.size])
                textSize = 12f
            }
            legendaContainer.addView(legendaItem)
        }

        legendaScroll.visibility = View.VISIBLE
    }
}

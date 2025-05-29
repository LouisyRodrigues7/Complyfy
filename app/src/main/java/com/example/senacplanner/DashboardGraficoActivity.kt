package com.example.senacplanner

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.model.AcaoComProgresso
import com.example.senacplanner.model.PilarComProgresso
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class DashboardGraficoActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var db: DatabaseHelper
    private var pilarId: Int = -1  // <- enviado por intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_grafico)

        pieChart = findViewById(R.id.pieChart)
        db = DatabaseHelper(this)
        pilarId = intent.getIntExtra("pilar_id", -1)

        if (pilarId == -1) {
            val lista = db.getProgressoTodosPilares()
            mostrarGraficoPilares(lista)
        } else {
            val lista = db.getAcoesComProgressoDoPilar(pilarId)
            mostrarGraficoAcoes(lista)
        }
    }

    private fun mostrarGraficoAcoes(lista: List<AcaoComProgresso>) {
        val entries = mutableListOf<PieEntry>()

        for (acao in lista) {
            if (acao.total > 0) {
                val progresso = acao.concluidas * 100f / acao.total
                entries.add(PieEntry(progresso, acao.nome))
            }
        }

        val dataSet = PieDataSet(entries, "Progresso por Ação")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate()
    }

    private fun mostrarGraficoPilares(lista: List<PilarComProgresso>) {
        val entries = mutableListOf<PieEntry>()

        for (pilar in lista) {
            if (pilar.total > 0) {
                val progresso = pilar.concluidas * 100f / pilar.total
                entries.add(PieEntry(progresso, pilar.nome))
            }
        }

        val dataSet = PieDataSet(entries, "Progresso por Pilar")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate()
    }
}

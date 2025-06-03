package com.example.senacplanner.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.R
import com.example.senacplanner.data.DatabaseHelper
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class GraficoEvolucaoAtividade : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var tvResponsavel: TextView
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grafico_evolucao_atividade)

        pieChart = findViewById(R.id.pieChartEvolucao)
        tvResponsavel = findViewById(R.id.tvResponsavel)
        db = DatabaseHelper(this)

        val acaoId = intent.getIntExtra("ACAO_ID", -1)
        val atividadeId = intent.getIntExtra("ATIVIDADE_ID", -1)

        if (atividadeId != -1) {
            mostrarGraficoAtividade(atividadeId)
        } else if (acaoId != -1) {
            mostrarGraficoAtividadesDaAcao(acaoId)
        } else {
            pieChart.setNoDataText("Nenhuma informação encontrada.")
        }
    }

    private fun mostrarGraficoAtividadesDaAcao(acaoId: Int) {
        val atividades = db.buscarAtividadesPorAcaoParaSelecao(acaoId)

        val emAndamento = atividades.count { it.status != "Finalizada" }
        val concluidas = atividades.count { it.status == "Finalizada" }

        val entries = listOf(
            PieEntry(emAndamento.toFloat(), "Em andamento"),
            PieEntry(concluidas.toFloat(), "Concluídas")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(Color.parseColor("#2196F3"), Color.parseColor("#4CAF50"))
            valueTextSize = 14f
            valueTextColor = Color.BLACK
        }

        pieChart.apply {
            data = PieData(dataSet)
            centerText = "Atividades da Ação"
            setCenterTextSize(18f)
            setEntryLabelColor(Color.DKGRAY)
            setEntryLabelTextSize(14f)
            description.isEnabled = false
            legend.isEnabled = true
            invalidate()
        }
    }




    private fun mostrarGraficoAtividade(atividadeId: Int) {
        val atividade = db.getAtividadePorId(atividadeId)

        atividade?.let { // só executa esse bloco se atividade NÃO for nula
            val estaConcluida = it.status == "Finalizada"
            val statusLabel = if (estaConcluida) "Concluída" else "Em andamento"
            val color = if (estaConcluida) "#4CAF50" else "#2196F3"

            val entries = listOf(
                PieEntry(1f, statusLabel)
            )

            val dataSet = PieDataSet(entries, "").apply {
                colors = listOf(Color.parseColor(color))
                valueTextSize = 16f
                valueTextColor = Color.BLACK
            }

            pieChart.apply {
                data = PieData(dataSet)
                centerText = "Status da Atividade"
                setCenterTextSize(18f)
                setEntryLabelColor(Color.DKGRAY)
                description.isEnabled = false
                legend.isEnabled = false
                invalidate()
            }

            tvResponsavel.apply {
                val nomeResponsavel = it.responsavelId?.let { id -> db.getNomeDoResponsavel(id) } ?: "Não informado"
                text = "Responsável: $nomeResponsavel"
                visibility = View.VISIBLE
            }
        } ?: run {
            // Caso atividade seja null, você pode tratar aqui (ex: esconder views ou mostrar mensagem)
            tvResponsavel.text = "Atividade não encontrada"
            tvResponsavel.visibility = View.VISIBLE
            pieChart.clear() // limpa gráfico
        }
    }


}

package com.example.senacplanner.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.NotificacoesActivity
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

    private lateinit var btnHome: ImageView
    private lateinit var btnGraficos: ImageView
    private lateinit var btnNotificacoes: ImageView
    private lateinit var btnLogout: ImageView

    private var tipoUsuario: String? = null
    private var nomeUsuario: String? = null
    private var idUsuario: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grafico_evolucao_atividade)

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

    private fun realizarLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


}

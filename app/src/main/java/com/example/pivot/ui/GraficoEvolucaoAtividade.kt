package com.example.pivot.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pivot.NotificacoesActivity
import com.example.pivot.R
import com.example.pivot.data.DatabaseHelper
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

/**
 * Tela que exibe um gráfico de pizza com a evolução de atividades,
 * podendo representar uma única atividade ou o conjunto de uma ação estratégica.
 */
class GraficoEvolucaoAtividade : AppCompatActivity() {

    /** Componente gráfico de pizza para visualização do status */
    private lateinit var pieChart: PieChart

    /** Texto que exibe o nome do responsável pela atividade */
    private lateinit var tvResponsavel: TextView

    /** Instância de acesso ao banco de dados */
    private lateinit var db: DatabaseHelper

    private lateinit var btnHome: ImageView
    private lateinit var btnGraficos: ImageView
    private lateinit var btnNotificacoes: ImageView
    private lateinit var btnLogout: ImageView

    private var tipoUsuario: String? = null
    private var nomeUsuario: String? = null
    private var idUsuario: Int = -1

    /**
     * Inicializa a interface e define qual gráfico será exibido
     * com base nos dados recebidos via `Intent`.
     */
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
            com.example.pivot.utils.NavigationUtils.irParaTelaHome(
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


    /**
     * Exibe um gráfico de pizza com o total de atividades concluídas e em andamento
     * de uma determinada ação.
     * @param acaoId ID da ação estratégica
     */
    private fun mostrarGraficoAtividadesDaAcao(acaoId: Int) {
        val resumo = db.getResumoDeAtividadesPorAcao(acaoId)

        val emAndamento = resumo["Em andamento"] ?: 0
        val concluidas = resumo["Finalizadas"] ?: 0
        val emAtraso = resumo["Em atraso"] ?: 0

        val entries = listOf(
            PieEntry(emAndamento.toFloat(), "Em andamento"),
            PieEntry(concluidas.toFloat(), "Concluídas"),
            PieEntry(emAtraso.toFloat(), "Em atraso")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#2196F3"), // azul - andamento
                Color.parseColor("#4CAF50"), // verde - concluídas
                Color.parseColor("#F44336")  // vermelho - em atraso
            )
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


    /**
     * Exibe um gráfico de pizza com o status de uma única atividade.
     * Também mostra o nome do responsável, se disponível.
     * @param atividadeId ID da atividade selecionada
     */
    private fun mostrarGraficoAtividade(atividadeId: Int) {
        val atividade = db.getAtividadePorId(atividadeId)

        atividade?.let {
            val estaConcluida = it.status == "Finalizada"
            val statusLabel = if (estaConcluida) "Concluída" else "Em andamento"
            val color = if (estaConcluida) "#4CAF50" else "#2196F3"

            val entries = listOf(PieEntry(1f, statusLabel))

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
            tvResponsavel.text = "Atividade não encontrada"
            tvResponsavel.visibility = View.VISIBLE
            pieChart.clear()
        }
    }


    private fun realizarLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

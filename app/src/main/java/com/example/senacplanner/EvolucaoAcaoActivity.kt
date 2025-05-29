package com.example.senacplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EvolucaoAcaoActivity : AppCompatActivity() {

    private lateinit var tvNomeAcao: TextView
    private lateinit var progressBarAcao: ProgressBar
    private lateinit var tvPorcentagemAcao: TextView
    private lateinit var containerAtividades: LinearLayout
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evolucao_acao)

        val acaoId = intent.getIntExtra("ACAO_ID", -1)
        if (acaoId == -1) finish()

        tvNomeAcao = findViewById(R.id.tvNomeAcao)
        progressBarAcao = findViewById(R.id.progressBarAcao)
        tvPorcentagemAcao = findViewById(R.id.tvPorcentagemAcao)
        containerAtividades = findViewById(R.id.containerAtividades)

        dbHelper = DatabaseHelper(this)
        carregarDetalhesAcao(acaoId)
    }

    private fun carregarDetalhesAcao(acaoId: Int) {
        val nomeAcao = dbHelper.getNomeAcaoById(acaoId)
        tvNomeAcao.text = nomeAcao

        val atividades = dbHelper.getAtividadesByAcaoId(acaoId)
        val concluidas = atividades.count { it.status == "Finalizada" }
        val progresso = if (atividades.isNotEmpty()) (concluidas * 100) / atividades.size else 0

        progressBarAcao.progress = progresso
        tvPorcentagemAcao.text = "$progresso% concluído"

        containerAtividades.removeAllViews()

        atividades.forEach { atividade ->
            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_atividade_acao, containerAtividades, false)

            itemView.findViewById<TextView>(R.id.tvResponsavel).text = "👤 ${atividade.nomeResponsavel}"
            itemView.findViewById<TextView>(R.id.tvNomeAtividade).text = atividade.nome
            itemView.findViewById<TextView>(R.id.tvDataInicio).text = "📅 Início: ${atividade.dataInicio}"
            itemView.findViewById<TextView>(R.id.tvDataConclusao).text = "📅 Fim: ${atividade.dataConclusao ?: "Não definida"}"

            val statusView = itemView.findViewById<TextView>(R.id.tvStatusAtividade)
            statusView.text = if (atividade.status == "Finalizada") "concluída" else "em andamento"
            statusView.setBackgroundResource(
                if (atividade.status == "Finalizada") R.drawable.bg_status_green else R.drawable.bg_status_yellow
            )

            containerAtividades.addView(itemView)
        }
    }
}

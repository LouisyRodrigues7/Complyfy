package com.example.senacplanner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class EvolucaoAcaoActivity : AppCompatActivity() {

    private lateinit var tvNomeAcao: TextView
    private lateinit var progressBarAcao: ProgressBar
    private lateinit var tvPorcentagemAcao: TextView
    private lateinit var containerAtividades: LinearLayout
    private lateinit var dbHelper: DatabaseHelper

    private var tipoUsuario: String? = null
    private var nomeUsuario: String? = null
    private var idUsuario: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evolucao_acao)

        // Dados do usuário
        nomeUsuario = intent.getStringExtra("NOME_USUARIO")
        tipoUsuario = intent.getStringExtra("TIPO_USUARIO")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        val acaoId = intent.getIntExtra("ACAO_ID", -1)
        if (acaoId == -1) {
            Toast.makeText(this, "ID da Ação inválido.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inicializa views
        tvNomeAcao = findViewById(R.id.tvNomeAcao)
        progressBarAcao = findViewById(R.id.progressBarAcao)
        tvPorcentagemAcao = findViewById(R.id.tvPorcentagemAcao)
        containerAtividades = findViewById(R.id.containerAtividades)

        dbHelper = DatabaseHelper(this)
        carregarDetalhesAcao(acaoId)

        // Botão Home
        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            com.example.senacplanner.util.NavigationUtils.irParaTelaHome(
                this,
                tipoUsuario,
                idUsuario,
                nomeUsuario
            )
        }

        // Botão Gráficos
        findViewById<ImageView>(R.id.btnGraficos).setOnClickListener {
            if (tipoUsuario != null && nomeUsuario != null && idUsuario != -1) {
                val intent = Intent(this, DashboardGraficoActivity::class.java).apply {
                    putExtra("TIPO_USUARIO", tipoUsuario)
                    putExtra("ID_USUARIO", idUsuario)
                    putExtra("NOME_USUARIO", nomeUsuario)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Dados do usuário ausentes. Não foi possível abrir os gráficos.", Toast.LENGTH_LONG).show()
            }
        }

        // Botão Notificações
        findViewById<ImageView>(R.id.btnNotificacoes).setOnClickListener {
            val intent = Intent(this, NotificacoesActivity::class.java).apply {
                putExtra("TIPO_USUARIO", tipoUsuario)
                putExtra("ID_USUARIO", idUsuario)
                putExtra("NOME_USUARIO", nomeUsuario)
            }
            startActivity(intent)
        }

        //  Botão Logout
        findViewById<ImageView>(R.id.btnAcoes).setOnClickListener {
            realizarLogout()
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
            com.example.senacplanner.util.NavigationUtils.irParaTelaHome(
                this,
                tipoUsuario,
                idUsuario,
                nomeUsuario
            )
        }
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

    private fun realizarLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

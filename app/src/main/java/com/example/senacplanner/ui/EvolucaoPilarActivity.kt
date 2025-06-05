package com.example.senacplanner.ui

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.Acoes.Type.Acao
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.widget.ImageView
import com.example.senacplanner.data.DatabaseHelper
import com.example.senacplanner.NotificacoesActivity
import com.example.senacplanner.R

class EvolucaoPilarActivity : AppCompatActivity() {

    private lateinit var tvTitulo: TextView
    private lateinit var tvNomePilar: TextView
    private lateinit var tvDataInicio: TextView
    private lateinit var tvDataConclusao: TextView
    private lateinit var tvStatusPilar: TextView
    private lateinit var containerAcoes: LinearLayout


    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgressoPorcentagem: TextView
    private var tipoUsuario: String? = null
    private var nomeUsuario: String? = null
    private var idUsuario: Int = -1
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evolucao_pilar)

        val pilarId = intent.getIntExtra("PILAR_ID", -1)
        tipoUsuario = intent.getStringExtra("TIPO_USUARIO")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)
        nomeUsuario = intent.getStringExtra("NOME_USUARIO")


        val textViewTituloHeader = findViewById<TextView>(R.id.textViewTitulo)
        textViewTituloHeader.text = "Olá, ${nomeUsuario ?: "Usuário"}"


        if (pilarId == -1) {
            finish()
            return
        }

        // Inicializa elementos da tela
        tvTitulo = findViewById(R.id.tvTituloPilar)
        tvNomePilar = findViewById(R.id.tvNomePilar)
        tvDataInicio = findViewById(R.id.tvDataInicio)
        tvDataConclusao = findViewById(R.id.tvDataConclusao)
        tvStatusPilar = findViewById(R.id.tvStatusPilar)
        containerAcoes = findViewById(R.id.containerAcoes)

        progressBar = findViewById(R.id.progressBarPilar)
        tvProgressoPorcentagem = findViewById(R.id.tvProgressoPorcentagem)

        dbHelper = DatabaseHelper(this)

        tvTitulo.text = "Evolução do Pilar"

        carregarDetalhesPilar(pilarId)
    }


    private fun carregarDetalhesPilar(pilarId: Int) {
        val dados = dbHelper.getDatasPilarById(pilarId)

        if (dados != null) {
            val (nome, dataInicio, dataConclusao) = dados

            tvNomePilar.text = nome
            tvDataInicio.text = dataInicio
            tvDataConclusao.text = dataConclusao

            val status = verificarStatusPilar(dataConclusao)
            tvStatusPilar.text = status

            carregarAcoes(pilarId)
            atualizarProgressoPilar(pilarId)
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
        val btnGraficos = findViewById<ImageView>(R.id.btnGraficos)
        btnGraficos.setOnClickListener {
            val intent = Intent(this, GraficosActivity::class.java)
            intent.putExtra("TIPO_USUARIO", tipoUsuario)
            intent.putExtra("ID_USUARIO", idUsuario)
            intent.putExtra("NOME_USUARIO", nomeUsuario)
            startActivity(intent)
        }
        val btnNotificacoes = findViewById<ImageView>(R.id.btnNotificacoes)
        btnNotificacoes.setOnClickListener {
            val intent = Intent(this, NotificacoesActivity::class.java).apply {
                putExtra("ID_USUARIO", idUsuario)
                putExtra("NOME_USUARIO", nomeUsuario)
                putExtra("TIPO_USUARIO", tipoUsuario)
            }
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


    private fun verificarStatusPilar(dataConclusao: String): String {
        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dataFinal = formato.parse(dataConclusao)
            val hoje = Date()

            if (hoje.after(dataFinal)) {
                "Finalizado"
            } else {
                "Em andamento"
            }
        } catch (e: Exception) {
            "Data inválida"
        }
    }

    private fun carregarAcoes(pilarId: Int) {
        val listaAcoes: List<Acao> = dbHelper.getAcoesByPilarId(pilarId)

        containerAcoes.removeAllViews()

        if (listaAcoes.isEmpty()) {
            val texto = TextView(this)
            texto.text = "Nenhuma ação cadastrada."
            texto.setTextColor(resources.getColor(android.R.color.white))
            texto.setPadding(16, 16, 16, 16)
            containerAcoes.addView(texto)
        } else {
            listaAcoes.forEach { acao ->
                val box = TextView(this)
                box.text = acao.nome
                box.setPadding(20, 20, 20, 20)
                box.setBackgroundResource(R.drawable.bg_box_acao)
                box.setTextColor(resources.getColor(android.R.color.white))
                box.textSize = 16f

                box.minHeight = 70.dpToPx()

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 24)
                box.layoutParams = params
                
                box.setOnClickListener {
                    val intent = Intent(this, EvolucaoAcaoActivity::class.java)
                    intent.putExtra("ACAO_ID", acao.id)
                    startActivity(intent)
                }

                containerAcoes.addView(box)
            }
        }
    }


    private fun atualizarProgressoPilar(pilarId: Int) {
        val progresso = dbHelper.calcularProgressoPilar(pilarId)

        progressBar.progress = progresso
        tvProgressoPorcentagem.text = "$progresso% concluído"
    }

    // 🔥 Extensão para dp → px
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}

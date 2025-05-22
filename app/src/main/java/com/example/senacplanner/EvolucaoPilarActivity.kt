package com.example.senacplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.Acoes.Type.Acao
import java.text.SimpleDateFormat
import java.util.*

class EvolucaoPilarActivity : AppCompatActivity() {

    private lateinit var tvTitulo: TextView
    private lateinit var tvDataInicio: TextView
    private lateinit var tvDataConclusao: TextView
    private lateinit var tvStatusPilar: TextView
    private lateinit var containerAcoes: LinearLayout

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evolucao_pilar)

        val pilarId = intent.getIntExtra("PILAR_ID", -1)

        if (pilarId == -1) {
            finish()
        }

        tvTitulo = findViewById(R.id.tvTituloPilar)
        tvDataInicio = findViewById(R.id.tvDataInicio)
        tvDataConclusao = findViewById(R.id.tvDataConclusao)
        tvStatusPilar = findViewById(R.id.tvStatusPilar)
        containerAcoes = findViewById(R.id.containerAcoes)

        dbHelper = DatabaseHelper(this)

        carregarDetalhesPilar(pilarId)
    }

    private fun carregarDetalhesPilar(pilarId: Int) {
        val dados = dbHelper.getDatasPilarById(pilarId)

        if (dados != null) {
            val (nome, dataInicio, dataConclusao) = dados

            tvTitulo.text = nome
            tvDataInicio.text = dataInicio
            tvDataConclusao.text = dataConclusao

            val status = verificarStatusPilar(dataConclusao)
            tvStatusPilar.text = status

            carregarAcoes(pilarId)
        }
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
            containerAcoes.addView(texto)
        } else {
            val inflater = LayoutInflater.from(this)
            listaAcoes.forEach { acao ->
                val box = TextView(this)
                box.text = acao.nome
                box.setPadding(12, 12, 12, 12)
                box.setBackgroundResource(R.drawable.bg_box_acao)
                box.setTextColor(resources.getColor(android.R.color.white))

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 16)
                box.layoutParams = params

                containerAcoes.addView(box)
            }
        }
    }
}

package com.example.senacplanner.novopilar

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.R

class NovoPilarActivity : AppCompatActivity() {

    private lateinit var btnNovaAcao: Button
    private lateinit var etResponsavel: EditText
    private lateinit var tvDataInicio: TextView
    private lateinit var tvDataFim: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnConcluido: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.novo_pilar) //

        // Pegando os elementos da tela
        btnNovaAcao = findViewById(R.id.btnNovaAcao)
        etResponsavel = findViewById(R.id.etResponsavel)
        tvDataInicio = findViewById(R.id.tvDataInicio)
        tvDataFim = findViewById(R.id.tvDataFim)
        tvStatus = findViewById(R.id.tvStatus)
        btnConcluido = findViewById(R.id.btnConcluido)

        // Exemplo: ação ao clicar no botão "Nova Ação"
        btnNovaAcao.setOnClickListener {

        }

        // Exemplo: ação ao clicar no botão "Concluído"
        btnConcluido.setOnClickListener {

            tvStatus.setText(R.string.status_concluido)
            tvStatus.setTextColor(getColor(R.color.teal_200)) // muda a cor para dar destaque
        }
    }
}

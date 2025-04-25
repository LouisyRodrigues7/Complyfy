package com.example.senacplanner

import android.os.Bundle
import android.widget.TextView // Essa importação é a correta
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity

class CoordenadorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coordenador)

        // Personalizando a saudação com o nome do usuário
        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: "Usuário"
        val saudacao = findViewById<TextView>(R.id.textViewSaudacao)
        saudacao.text = "Olá, $nomeUsuario"

        // Acessando o GridLayout onde estão os pilares
        val gridLayout = findViewById<GridLayout>(R.id.gridMinhasAtividades)

        // Percorrendo todos os CardViews no GridLayout
        for (i in 0 until gridLayout.childCount) {
            // Pegando o CardView (pilar)
            val cardView = gridLayout.getChildAt(i) as androidx.cardview.widget.CardView

            // Encontrando a TextView dentro do CardView que exibe o número
            val numeroPilar = cardView.findViewById<TextView>(R.id.numeroPilar)

            // Atribuindo o número dinâmico para cada pilar
            numeroPilar.text = (i + 1).toString() // Isso vai enumerar os pilares de 1 em diante
        }
    }
}

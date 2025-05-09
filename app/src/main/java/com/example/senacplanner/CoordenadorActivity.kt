package com.example.senacplanner

import android.os.Bundle
import android.widget.TextView
import android.widget.GridLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class CoordenadorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coordenador)

        // Personalizando a saudação
        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: "Usuário"
        val saudacao = findViewById<TextView>(R.id.textViewSaudacao)
        saudacao.text = "Olá, $nomeUsuario"

        // Listas separadas
        val nomesPilaresPequenos = listOf(
            "Avaliação de riscos",
            "Controles internos",
            "Código de conduta e ética"
        )

        val nomesPilaresGrandes = listOf(
            "Avaliação de riscos",
            "Controles internos",
            "Código de conduta e ética",
            "Gestão de Riscos",
            "Transparência"
        )

        // "Minhas Atividades" (pequenos)
        val gridLayout = findViewById<GridLayout>(R.id.gridMinhasAtividades)
        for (i in 0 until gridLayout.childCount) {
            val cardView = gridLayout.getChildAt(i) as CardView
            val numero = cardView.findViewById<TextView>(R.id.numeroPilar)
            val texto = cardView.findViewById<TextView>(R.id.textoPilar)

            numero.text = (i + 1).toString()
            if (i < nomesPilaresPequenos.size) {
                texto.text = nomesPilaresPequenos[i]
            } else {
                texto.text = "Pilar ${i + 1}"
            }
        }

        //  "Pilares" (grandes)
        val layoutPilaresGrandes = findViewById<LinearLayout>(R.id.layoutPilaresGrandes)
        var numeroPilarGrande = 1 // Contador para garantir numeração sequencial
        val totalPilaresGrandes = nomesPilaresGrandes.size // Define o total de pilares que teremos

        for (i in 0 until layoutPilaresGrandes.childCount) {
            val view = layoutPilaresGrandes.getChildAt(i)
            if (view is CardView) {
                val numeroGrande = view.findViewById<TextView>(R.id.numeroPilarGrande)
                val textoGrande = view.findViewById<TextView>(R.id.textoPilarGrande)

                numeroGrande.text = numeroPilarGrande.toString() // Garantindo a numeração correta

                // Preenchendo os nomes dos pilares grandes (os de baixo)
                if (numeroPilarGrande <= nomesPilaresGrandes.size) {
                    textoGrande.text = nomesPilaresGrandes[numeroPilarGrande - 1] // Nome do Pilar Específico
                } else {
                    // Se não houver mais nomes na lista, coloca um nome genérico
                    textoGrande.text = "Pilar Grande $numeroPilarGrande"
                }

                numeroPilarGrande++ // Incrementa o contador para o próximo número
            }
        }
    }
}

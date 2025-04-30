package com.example.senacplanner

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.GridLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.senacplanner.Pilares.ListaAtividades

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

            // Clique no Pilar 1
            if (i == 0) {
                cardView.setOnClickListener {
                    val intent = Intent(this, ListaAtividades::class.java)
                    intent.putExtra("PILAR_ID", 1) // opcional: passa o ID do pilar
                    startActivity(intent)
                }
            }
        }

        // "Pilares" (grandes)
        val layoutPilaresGrandes = findViewById<LinearLayout>(R.id.layoutPilaresGrandes)
        var numeroPilarGrande = 1
        val totalPilaresGrandes = nomesPilaresGrandes.size

        for (i in 0 until layoutPilaresGrandes.childCount) {
            val view = layoutPilaresGrandes.getChildAt(i)
            if (view is CardView) {
                val numeroGrande = view.findViewById<TextView>(R.id.numeroPilarGrande)
                val textoGrande = view.findViewById<TextView>(R.id.textoPilarGrande)

                numeroGrande.text = numeroPilarGrande.toString()
                if (numeroPilarGrande <= nomesPilaresGrandes.size) {
                    textoGrande.text = nomesPilaresGrandes[numeroPilarGrande - 1]
                } else {
                    textoGrande.text = "Pilar Grande $numeroPilarGrande"
                }

                numeroPilarGrande++
            }
        }
    }
}

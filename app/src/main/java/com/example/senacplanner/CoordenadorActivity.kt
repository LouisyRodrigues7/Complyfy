package com.example.senacplanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.GridLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.senacplanner.Pilares.ListaAtividades

class CoordenadorActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coordenador)

        // Personalizando a saudação
        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: "Usuário"
        val saudacao = findViewById<TextView>(R.id.textViewSaudacao)
        saudacao.text = "Olá, $nomeUsuario"

        databaseHelper = DatabaseHelper(this)


        // "Minhas Atividades" (pequenos)
        val gridLayout = findViewById<GridLayout>(R.id.gridMinhasAtividades)
        val pilares = databaseHelper.getAllPilares()
        val nomesPilaresPequenos = pilares.take(3)

        gridLayout.removeAllViews()

        nomesPilaresPequenos.forEachIndexed { index, pilar ->
            val cardView = layoutInflater.inflate(R.layout.item_pilar, gridLayout, false) as CardView

            val numero = cardView.findViewById<TextView>(R.id.numeroPilar)
            val texto = cardView.findViewById<TextView>(R.id.textoPilar)

            numero.text = (index + 1).toString()
            texto.text = pilar.titulo

            cardView.setOnClickListener {
                val intent = Intent(this, ListaAtividades::class.java)
                intent.putExtra("PILAR_ID", pilar.id)
                startActivity(intent)
            }

            gridLayout.addView(cardView)
        }

        // "Pilares" (grandes)
        val layoutPilaresGrandes = findViewById<LinearLayout>(R.id.layoutPilaresGrandes)
        layoutPilaresGrandes.removeAllViews()
        pilares.forEachIndexed { index, pilar ->
            val view = layoutInflater.inflate(R.layout.item_pilar_grande, layoutPilaresGrandes, false)

            val numero = view.findViewById<TextView>(R.id.numeroPilarGrande)
            val titulo = view.findViewById<TextView>(R.id.textoPilarGrande)

            numero.text = (index + 1).toString()
            titulo.text = pilar.titulo // ou "${pilar.titulo} - ${pilar.descricao}" se quiser os dois

            view.setOnClickListener {
                val intent = Intent(this, ListaAtividades::class.java)
                intent.putExtra("PILAR_ID", pilar.id)
                startActivity(intent)
            }

            layoutPilaresGrandes.addView(view)
        }

    }
}

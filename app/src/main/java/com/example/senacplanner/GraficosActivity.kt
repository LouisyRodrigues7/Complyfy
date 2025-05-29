package com.example.senacplanner

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class GraficosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graficos) // ou o layout que tem os cards

        // Referência ao CardView
        val cardProgressoPilares = findViewById<CardView>(R.id.cardProgressoPilares)

        // Clique para abrir a nova Activity
        cardProgressoPilares.setOnClickListener {
            val intent = Intent(this, ProgressoPilaresActivity::class.java)
            startActivity(intent)
        }
    }
}

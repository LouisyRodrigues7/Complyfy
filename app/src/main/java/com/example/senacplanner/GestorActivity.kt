package com.example.senacplanner

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class GestorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestor)

        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: "Usuário"
        val saudacao = findViewById<TextView>(R.id.textViewSaudacao)
        saudacao.text = "Olá, $nomeUsuario"

        val cardConsultar = findViewById<CardView>(R.id.cardConsultar)
        cardConsultar.setOnClickListener {
            val intent = Intent(this, PilarAcaoActivity::class.java)
            startActivity(intent)
        }
    }
}

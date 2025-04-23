package com.example.senacplanner

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CoordenadorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coordenador)

        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: "Usuário"
        val saudacao = findViewById<TextView>(R.id.textViewSaudacao)
        saudacao.text = "Olá, $nomeUsuario"
    }
}

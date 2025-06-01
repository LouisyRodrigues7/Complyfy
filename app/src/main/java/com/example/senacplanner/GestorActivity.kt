package com.example.senacplanner

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class GestorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestor)

        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: "Usuário"
        val tipoUsuario = intent.getStringExtra("TIPO_USUARIO")
        val idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        val saudacao = findViewById<TextView>(R.id.textViewSaudacao)
        saudacao.text = "Olá, $nomeUsuario"

        val cardConsultar = findViewById<CardView>(R.id.cardConsultar)
        cardConsultar.setOnClickListener {
            val intent = Intent(this, PilarAcaoActivity::class.java)
            startActivity(intent)
        }

        // Botão Gráficos
        val btnGraficos = findViewById<ImageView>(R.id.btnGraficos)
        btnGraficos.setOnClickListener {
            val intent = Intent(this, DashboardGraficoActivity::class.java)
            intent.putExtra("TIPO_USUARIO", tipoUsuario)
            intent.putExtra("ID_USUARIO", idUsuario)
            intent.putExtra("NOME_USUARIO", nomeUsuario)
            startActivity(intent)
        }

        // Botão Notificações
        val btnNotificacoes = findViewById<ImageView>(R.id.btnNotificacoes)
        btnNotificacoes.setOnClickListener {
            val intent = Intent(this, NotificacoesActivity::class.java)
            intent.putExtra("ID_USUARIO", idUsuario)
            startActivity(intent)
        }

        // Botão Logout (btnAcoes)
        val btnLogout = findViewById<ImageView>(R.id.btnAcoes)
        btnLogout.setOnClickListener {
            realizarLogout()
        }

        // Botão Home
        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
        }
    }

    private fun realizarLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

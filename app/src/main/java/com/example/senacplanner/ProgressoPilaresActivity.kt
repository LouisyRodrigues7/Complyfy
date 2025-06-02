package com.example.senacplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.model.PilarItem
import android.widget.ImageView

class ProgressoPilaresActivity : AppCompatActivity() {

    private lateinit var spinnerPilares: Spinner

    private var tipoUsuario: String? = null
    private var nomeUsuario: String? = null
    private var idUsuario: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progresso_pilares)

        // Recuperar dados do usuário
        tipoUsuario = intent.getStringExtra("TIPO_USUARIO")
        nomeUsuario = intent.getStringExtra("NOME_USUARIO")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        // Botões
        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            com.example.senacplanner.util.NavigationUtils.irParaTelaHome(
                this,
                tipoUsuario,
                idUsuario,
                nomeUsuario
            )
        }

        val btnGraficos = findViewById<ImageView>(R.id.btnGraficos)
        btnGraficos.setOnClickListener {
            val intent = Intent(this, DashboardGraficoActivity::class.java).apply {
                putExtra("TIPO_USUARIO", tipoUsuario)
                putExtra("ID_USUARIO", idUsuario)
                putExtra("NOME_USUARIO", nomeUsuario)
            }
            startActivity(intent)
        }

        val btnNotificacoes = findViewById<ImageView>(R.id.btnNotificacoes)
        btnNotificacoes.setOnClickListener {
            val intent = Intent(this, NotificacoesActivity::class.java).apply {
                putExtra("ID_USUARIO", idUsuario)
                putExtra("NOME_USUARIO", nomeUsuario)
                putExtra("TIPO_USUARIO", tipoUsuario)
            }
            startActivity(intent)
        }

        val btnLogout = findViewById<ImageView>(R.id.btnAcoes)
        btnLogout.setOnClickListener {
            realizarLogout()
        }

        // Spinner
        spinnerPilares = findViewById(R.id.spinnerPilares)

        val db = DatabaseHelper(this)
        val pilaresDoBanco = db.getTodosPilares().toMutableList()
        pilaresDoBanco.add(0, PilarItem(id = -1, numero = 0, nome = "Todos os pilares"))

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            pilaresDoBanco
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPilares.adapter = adapter

        // Botão Confirmar
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        btnConfirmar.setOnClickListener {
            val itemSelecionado = spinnerPilares.selectedItem as PilarItem
            val intent = Intent(this, DashboardGraficoActivity::class.java).apply {
                putExtra("pilar_id", itemSelecionado.id)
                putExtra("TIPO_USUARIO", tipoUsuario)
                putExtra("ID_USUARIO", idUsuario)
                putExtra("NOME_USUARIO", nomeUsuario)
            }
            startActivity(intent)
        }
    }

    private fun realizarLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

package com.example.pivot.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.pivot.model.PilarItem
import android.widget.ImageView
import com.example.pivot.data.DatabaseHelper
import com.example.pivot.NotificacoesActivity
import com.example.pivot.R

/**
 * Tela que permite ao usuário selecionar um pilar específico (ou todos)
 * para visualizar seus dados em gráficos de progresso.
 */
class ProgressoPilaresActivity : AppCompatActivity() {

    /** Spinner que lista os pilares disponíveis no banco */
    private lateinit var spinnerPilares: Spinner

    private var tipoUsuario: String? = null
    private var nomeUsuario: String? = null
    private var idUsuario: Int = -1

    /**
     * Inicializa a tela com os botões de navegação e spinner com os pilares cadastrados.
     * Permite selecionar e ir para a visualização gráfica do progresso dos pilares.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progresso_pilares)

        // Recuperar dados do usuário
        tipoUsuario = intent.getStringExtra("TIPO_USUARIO")
        nomeUsuario = intent.getStringExtra("NOME_USUARIO")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        // Botões de navegação (documentados em outros arquivos)
        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            com.example.pivot.utils.NavigationUtils.irParaTelaHome(
                this,
                tipoUsuario,
                idUsuario,
                nomeUsuario
            )
        }

        val btnGraficos = findViewById<ImageView>(R.id.btnGraficos)
        btnGraficos.setOnClickListener {
            val intent = Intent(this, GraficosActivity::class.java).apply {
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

        // Spinner de pilares
        spinnerPilares = findViewById(R.id.spinnerPilares)

        val db = DatabaseHelper(this)
        val pilaresDoBanco = db.getTodosPilares().toMutableList()

        // Adiciona uma opção genérica ao topo: "Todos os pilares"
        pilaresDoBanco.add(0, PilarItem(id = -1, numero = 0, nome = "Todos os pilares"))

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            pilaresDoBanco
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPilares.adapter = adapter

        /**
         * Ao confirmar, abre a tela `DashboardGraficoActivity` passando o ID do pilar selecionado.
         */
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

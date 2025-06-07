package com.example.pivot.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pivot.Acoes.Type.PilarType
import android.widget.ImageView
import android.widget.TextView
import com.example.pivot.data.DatabaseHelper
import com.example.pivot.NotificacoesActivity
import com.example.pivot.R

/**
 * Tela que permite ao usuário selecionar um pilar e visualizar sua evolução.
 * Utilizada principalmente por perfis como Gestor ou Coordenador.
 */
class PilarAcaoActivity : AppCompatActivity() {

    /** Spinner com os pilares cadastrados */
    private lateinit var spinnerPilar: Spinner

    /** Botão que confirma a seleção e exibe a evolução do pilar */
    private lateinit var btnBuscar: Button

    private lateinit var dbHelper: DatabaseHelper

    private var tipoUsuario: String? = null
    private var nomeUsuario: String? = null
    private var idUsuario: Int = -1

    /** Lista local com os dados dos pilares */
    private var listaPilares: List<PilarType> = emptyList()

    /**
     * Inicializa os componentes da interface e configura os eventos da tela.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilar_acao)

        // Recupera os dados do usuário
        nomeUsuario = intent.getStringExtra("NOME_USUARIO")
        tipoUsuario = intent.getStringExtra("TIPO_USUARIO")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        // Atualiza o cabeçalho com o nome do usuário
        val tvTitulo = findViewById<TextView>(R.id.textViewTitulo)
        tvTitulo.text = "Olá, ${nomeUsuario ?: "Usuário"}"

        // Inicializa componentes
        spinnerPilar = findViewById(R.id.spinnerPilar)
        btnBuscar = findViewById(R.id.btnBuscar)
        dbHelper = DatabaseHelper(this)

        // Carrega pilares no spinner
        carregarPilares()

        // Botões padrão (já explicados em arquivos anteriores)
        val btnGraficos = findViewById<ImageView>(R.id.btnGraficos)
        btnGraficos.setOnClickListener {
            if (tipoUsuario != null && nomeUsuario != null && idUsuario != -1) {
                val intent = Intent(this, GraficosActivity::class.java).apply {
                    putExtra("TIPO_USUARIO", tipoUsuario)
                    putExtra("ID_USUARIO", idUsuario)
                    putExtra("NOME_USUARIO", nomeUsuario)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Dados do usuário ausentes. Não foi possível abrir os gráficos.", Toast.LENGTH_LONG).show()
            }
        }

        val btnNotificacoes = findViewById<ImageView>(R.id.btnNotificacoes)
        btnNotificacoes.setOnClickListener {
            val intent = Intent(this, NotificacoesActivity::class.java).apply {
                putExtra("TIPO_USUARIO", tipoUsuario)
                putExtra("ID_USUARIO", idUsuario)
                putExtra("NOME_USUARIO", nomeUsuario)
            }
            startActivity(intent)
        }

        val btnLogout = findViewById<ImageView>(R.id.btnAcoes)
        btnLogout.setOnClickListener {
            realizarLogout()
        }

        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            com.example.pivot.utils.NavigationUtils.irParaTelaHome(
                this,
                tipoUsuario,
                idUsuario,
                nomeUsuario
            )
        }


        btnBuscar.setOnClickListener {
            val posicaoSelecionada = spinnerPilar.selectedItemPosition

            if (posicaoSelecionada >= 0 && listaPilares.isNotEmpty()) {
                val pilarSelecionado = listaPilares[posicaoSelecionada]

                val intent = Intent(this, EvolucaoPilarActivity::class.java).apply {
                    putExtra("PILAR_ID", pilarSelecionado.id)
                    putExtra("NOME_USUARIO", nomeUsuario)
                    putExtra("ID_USUARIO", idUsuario)
                    putExtra("TIPO_USUARIO", tipoUsuario)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Selecione um Pilar!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Carrega a lista de pilares do banco e popula o spinner.
     */
    private fun carregarPilares() {
        listaPilares = dbHelper.getAllPilares()

        if (listaPilares.isEmpty()) {
            Toast.makeText(this, "Nenhum Pilar encontrado", Toast.LENGTH_SHORT).show()
        }

        val nomesPilares = listaPilares.map { it.nome }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nomesPilares
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPilar.adapter = adapter
    }


    private fun realizarLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

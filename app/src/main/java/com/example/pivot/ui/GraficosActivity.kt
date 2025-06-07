package com.example.pivot.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.widget.ImageView
import android.widget.Toast
import com.example.pivot.NotificacoesActivity
import com.example.pivot.R

/**
 * Tela que apresenta opções de navegação para gráficos de progresso,
 * como atividades e pilares. Também permite acesso a notificações e logout.
 */
class GraficosActivity : AppCompatActivity() {


    private var tipoUsuario: String? = null
    private var nomeUsuario: String? = null
    private var idUsuario: Int = -1

    /**
     * Inicializa a interface com os cards de gráficos e botões de navegação.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graficos)

        nomeUsuario = intent.getStringExtra("NOME_USUARIO")
        tipoUsuario = intent.getStringExtra("TIPO_USUARIO")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Atualiza o título da toolbar com saudação
        if (!nomeUsuario.isNullOrEmpty()) {
            supportActionBar?.title = "Olá, $nomeUsuario"
        } else {
            supportActionBar?.title = "Olá, usuário"
        }

        /**
         * Abre o gráfico de progresso por atividades.
         */
        val cardProgressoAtividades = findViewById<CardView>(R.id.cardProgressoAtividades)
        cardProgressoAtividades.setOnClickListener {
            val intent = Intent(this, EvolucaoAtividade::class.java)
            startActivity(intent)
        }

        // Botões padrão já documentados em outros arquivos
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
                Toast.makeText(
                    this,
                    "Dados do usuário ausentes. Não foi possível abrir os gráficos.",
                    Toast.LENGTH_LONG
                ).show()
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

        /**
         * Abre o gráfico de progresso por pilares.
         */
        val cardProgressoPilares = findViewById<CardView>(R.id.cardProgressoPilares)
        cardProgressoPilares.setOnClickListener {
            val intent = Intent(this, ProgressoPilaresActivity::class.java).apply {
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

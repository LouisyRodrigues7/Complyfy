package com.example.senacplanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.senacplanner.adapter.NotificacaoAdapter
import com.example.senacplanner.adapter.TipoNotificacao
import com.example.senacplanner.model.Notificacao
import android.content.Intent
import android.widget.ImageView
import android.widget.Toast
import com.example.senacplanner.data.DatabaseHelper
import com.example.senacplanner.ui.GraficosActivity
import com.example.senacplanner.ui.LoginActivity

class NotificacoesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificacaoAdapter
    private lateinit var db: DatabaseHelper
    private var tipoUsuario: String? = null
    private var nomeUsuario: String? = null
    private var idUsuario: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificacoes)

        // Recuperando os dados do usuário
        nomeUsuario = intent.getStringExtra("NOME_USUARIO")
        tipoUsuario = intent.getStringExtra("TIPO_USUARIO")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        // para checar problema
        if (nomeUsuario.isNullOrBlank() || tipoUsuario.isNullOrBlank() || idUsuario == -1) {
            Toast.makeText(this, "Erro: dados do usuário ausentes.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        db = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerViewNotificacoes)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (!nomeUsuario.isNullOrEmpty()) {
            supportActionBar?.title = "Olá, $nomeUsuario"
        } else {
            supportActionBar?.title = "Olá, usuário"
        }

        val notificacoes = buscarNotificacoesDoUsuario(idUsuario).toMutableList()
        adapter = NotificacaoAdapter(notificacoes)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

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
            Toast.makeText(this, "Você já está nas notificações!", Toast.LENGTH_SHORT).show()
        }

        val btnLogout = findViewById<ImageView>(R.id.btnAcoes)
        btnLogout.setOnClickListener {
            realizarLogout()
        }

        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            com.example.senacplanner.utils.NavigationUtils.irParaTelaHome(
                this,
                tipoUsuario,
                idUsuario,
                nomeUsuario
            )
        }
    }


    private fun realizarLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    private fun buscarNotificacoesDoUsuario(usuarioId: Int): List<Notificacao> {
        val lista = mutableListOf<Notificacao>()
        val db = db.getDatabase()
        val cursor = db.rawQuery(
            """
        SELECT id, mensagem, data, lida, atividade_id, tipo_notificacao 
        FROM Notificacao 
        WHERE usuario_id = ?
        ORDER BY data DESC
        """.trimIndent(),
            arrayOf(usuarioId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val mensagem = cursor.getString(cursor.getColumnIndexOrThrow("mensagem"))
                val data = cursor.getString(cursor.getColumnIndexOrThrow("data")).toLongOrNull() ?: 0L
                val lida = cursor.getInt(cursor.getColumnIndexOrThrow("lida")) == 1
                val atividadeIdIndex = cursor.getColumnIndex("atividade_id")
                val atividadeId = if (atividadeIdIndex >= 0 && !cursor.isNull(atividadeIdIndex)) {
                    cursor.getInt(atividadeIdIndex)
                } else null

                val tipoString = cursor.getString(cursor.getColumnIndexOrThrow("tipo_notificacao"))
                val tipo = try {
                    TipoNotificacao.valueOf(tipoString)
                } catch (e: IllegalArgumentException) {
                    TipoNotificacao.GERAL
                }

                lista.add(Notificacao(id, mensagem, data, lida, atividadeId, tipo))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return lista
    }
}

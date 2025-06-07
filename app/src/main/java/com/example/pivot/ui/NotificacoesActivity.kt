package com.example.pivot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pivot.adapter.NotificacaoAdapter
import com.example.pivot.adapter.TipoNotificacao
import com.example.pivot.model.Notificacao
import android.content.Intent
import android.widget.ImageView
import android.widget.Toast
import com.example.pivot.data.DatabaseHelper
import com.example.pivot.ui.GraficosActivity
import com.example.pivot.ui.LoginActivity

/**
 * Tela que exibe as notificações recebidas por um usuário específico.
 * As notificações são exibidas em ordem decrescente de data.
 */
class NotificacoesActivity : AppCompatActivity() {

    /** RecyclerView que lista as notificações */
    private lateinit var recyclerView: RecyclerView

    /** Adapter responsável por popular a RecyclerView */
    private lateinit var adapter: NotificacaoAdapter

    /** Instância do helper para acessar banco de dados */
    private lateinit var db: DatabaseHelper

    private var tipoUsuario: String? = null
    private var nomeUsuario: String? = null
    private var idUsuario: Int = -1

    /**
     * Inicializa a interface, valida os dados do usuário e popula a lista de notificações.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificacoes)

        // Recuperando os dados do usuário
        nomeUsuario = intent.getStringExtra("NOME_USUARIO")
        tipoUsuario = intent.getStringExtra("TIPO_USUARIO")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        // Checagem de integridade dos dados
        if (nomeUsuario.isNullOrBlank() || tipoUsuario.isNullOrBlank() || idUsuario == -1) {
            Toast.makeText(this, "Erro: dados do usuário ausentes.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        db = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerViewNotificacoes)

        // Toolbar com saudação
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Olá, ${nomeUsuario ?: "usuário"}"

        // Busca as notificações do usuário e configura o adapter
        val notificacoes = buscarNotificacoesDoUsuario(idUsuario).toMutableList()
        adapter = NotificacaoAdapter(notificacoes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Navegação padrão (já explicada em outros arquivos)
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
            com.example.pivot.utils.NavigationUtils.irParaTelaHome(
                this,
                tipoUsuario,
                idUsuario,
                nomeUsuario
            )
        }
    }

    // 🔐 Já comentado em arquivos anteriores
    private fun realizarLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Realiza a busca das notificações do usuário informado.
     * @param usuarioId ID do usuário para o qual as notificações serão buscadas
     * @return Lista de notificações ordenadas pela data (mais recentes primeiro)
     */
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

                // Pode ser nula se for notificação geral
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

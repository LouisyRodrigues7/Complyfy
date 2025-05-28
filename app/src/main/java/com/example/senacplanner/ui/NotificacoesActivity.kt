package com.example.senacplanner

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.senacplanner.adapter.NotificacaoAdapter
import com.example.senacplanner.adapter.TipoNotificacao
import com.example.senacplanner.model.Notificacao

class NotificacoesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificacaoAdapter
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificacoes)

        db = DatabaseHelper(this)

        recyclerView = findViewById(R.id.recyclerViewNotificacoes)

        val idUsuario = intent.getIntExtra("ID_USUARIO", -1)
        val notificacoes = buscarNotificacoesDoUsuario(idUsuario).toMutableList()
        adapter = NotificacaoAdapter(notificacoes)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

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

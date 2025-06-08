package com.example.pivot.utils

import android.content.Context
import android.util.Base64
import com.example.pivot.model.HistoricoRelatorio

/**
 * Utilitário responsável por armazenar e recuperar o histórico de relatórios gerados em PDF.
 * Os dados são persistidos localmente usando `SharedPreferences`, serializados como string Base64.
 */
object HistoricoRelatorioManager {

    private const val PREF_NAME = "historico_relatorios"
    private const val KEY_HISTORICO = "pdfs"

    /**
     * Salva um novo item no histórico de relatórios.
     * Garante que não haja duplicatas, mantém os registros mais recentes no topo
     * e limita o histórico a no máximo 5 entradas.
     *
     * @param context Contexto da aplicação
     * @param novo Novo relatório a ser salvo
     */
    fun salvar(context: Context, novo: HistoricoRelatorio) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val listaAtual = carregar(context).toMutableList()

        // Remove duplicatas
        listaAtual.removeAll { it.uri == novo.uri }

        // Adiciona novo no topo
        listaAtual.add(0, novo)

        // Limita a 5 registros
        if (listaAtual.size > 5) listaAtual.removeAt(listaAtual.lastIndex)

        // Serializa lista como texto
        val stringList = listaAtual.joinToString("|") { "${it.nome}::${it.uri}::${it.data}" }
        val encoded = Base64.encodeToString(stringList.toByteArray(), Base64.NO_WRAP)

        prefs.edit().putString(KEY_HISTORICO, encoded).apply()
    }

    /**
     * Recupera a lista de relatórios armazenados localmente no dispositivo.
     * A lista é desserializada de uma string Base64 codificada e delimitada por '|'.
     *
     * @param context Contexto da aplicação
     * @return Lista de relatórios recuperados, ou vazia em caso de falha
     */
    fun carregar(context: Context): List<HistoricoRelatorio> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val encoded = prefs.getString(KEY_HISTORICO, null) ?: return emptyList()

        return try {
            val decoded = String(Base64.decode(encoded, Base64.NO_WRAP))
            decoded.split("|").mapNotNull {
                val parts = it.split("::")
                if (parts.size == 3) {
                    HistoricoRelatorio(parts[0], parts[1], parts[2].toLong())
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

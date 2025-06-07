package com.example.senacplanner.utils

import android.content.Context
import android.util.Base64
import com.example.senacplanner.model.HistoricoRelatorio

object HistoricoRelatorioManager {

    private const val PREF_NAME = "historico_relatorios"
    private const val KEY_HISTORICO = "pdfs"

    fun salvar(context: Context, novo: HistoricoRelatorio) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val listaAtual = carregar(context).toMutableList()

        // Remove duplicatas
        listaAtual.removeAll { it.uri == novo.uri }

        // Adiciona novo no topo
        listaAtual.add(0, novo)

        // Limita a 5 registros
        if (listaAtual.size > 5) listaAtual.removeLast()

        // Serializa lista como texto
        val stringList = listaAtual.joinToString("|") { "${it.nome}::${it.uri}::${it.data}" }
        val encoded = Base64.encodeToString(stringList.toByteArray(), Base64.NO_WRAP)

        prefs.edit().putString(KEY_HISTORICO, encoded).apply()
    }

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

package com.example.senacplanner.util

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.example.senacplanner.ui.CoordenadorActivity
import com.example.senacplanner.ui.GestorActivity

object NavigationUtils {

    fun irParaTelaHome(
        activity: Activity,
        tipoUsuario: String?,
        idUsuario: Int,
        nomeUsuario: String?
    ) {
        // 🚨 Verificação de segurança
        if (tipoUsuario.isNullOrBlank() || nomeUsuario.isNullOrBlank() || idUsuario == -1) {
            Log.e("NAVIGATION", "Dados insuficientes para navegar para a Home.")
            return
        }

        Log.d("NAVIGATION", "Navegando para a home de $tipoUsuario - ID: $idUsuario, Nome: $nomeUsuario")

        // ✅ Roteamento baseado no tipo de usuário
        when (tipoUsuario) {
            "Coordenador", "Apoio" -> {
                // Evita recriar a activity se já estiver nela
                if (activity !is CoordenadorActivity) {
                    val intent = Intent(activity, CoordenadorActivity::class.java).apply {
                        putExtra("ID_USUARIO", idUsuario)
                        putExtra("NOME_USUARIO", nomeUsuario)
                        putExtra("TIPO_USUARIO", tipoUsuario)
                        putExtra("PAGINA_HOME", 0) // define a aba inicial
                    }
                    activity.startActivity(intent)
                    activity.finish()
                }
            }

            "Gestor" -> {
                if (activity !is GestorActivity) {
                    val intent = Intent(activity, GestorActivity::class.java).apply {
                        putExtra("ID_USUARIO", idUsuario)
                        putExtra("NOME_USUARIO", nomeUsuario)
                        putExtra("TIPO_USUARIO", tipoUsuario)
                    }
                    activity.startActivity(intent)
                    activity.finish()
                }
            }

            else -> {
                Log.e("NAVIGATION", "Tipo de usuário desconhecido: $tipoUsuario")
            }
        }
    }
}

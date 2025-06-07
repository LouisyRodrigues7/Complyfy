package com.example.senacplanner.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.example.senacplanner.ui.CoordenadorActivity
import com.example.senacplanner.ui.GestorActivity

/**
 * Utilitário responsável por redirecionar o usuário para a tela inicial correta,
 * com base no seu tipo (Coordenador, Apoio ou Gestor).
 */
object NavigationUtils {

    /**
     * Redireciona para a tela inicial correspondente ao perfil do usuário.
     *
     * @param activity Contexto da activity atual
     * @param tipoUsuario Tipo do usuário logado (ex: "Coordenador", "Gestor", "Apoio")
     * @param idUsuario ID do usuário logado
     * @param nomeUsuario Nome do usuário logado
     *
     * A navegação é interrompida caso os dados estejam incompletos.
     * Evita também recriar a tela se já estiver nela.
     */
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

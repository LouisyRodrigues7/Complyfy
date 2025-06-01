package com.example.senacplanner.util

import android.app.Activity
import android.content.Intent
import com.example.senacplanner.CoordenadorActivity
import com.example.senacplanner.GestorActivity
import android.util.Log

object NavigationUtils {

    fun irParaTelaHome(
        activity: Activity,
        tipoUsuario: String?,
        idUsuario: Int,
        nomeUsuario: String?
    ) {
        if (tipoUsuario.isNullOrBlank() || nomeUsuario.isNullOrBlank() || idUsuario == -1) {
            Log.e("NAVIGATION", "Dados insuficientes para navegar para a Home.")
            return
        }

        when (tipoUsuario) {
            "Coordenador", "Apoio" -> {
                if (activity !is CoordenadorActivity) {
                    val intent = Intent(activity, CoordenadorActivity::class.java).apply {
                        putExtra("ID_USUARIO", idUsuario)
                        putExtra("NOME_USUARIO", nomeUsuario)
                        putExtra("TIPO_USUARIO", tipoUsuario)
                        putExtra("PAGINA_HOME", 0)
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

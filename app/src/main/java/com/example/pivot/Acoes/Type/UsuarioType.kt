package com.example.pivot.Acoes.Type

/**
 * Representa um usuário do sistema com informações básicas.
 *
 * @property id Identificador único do usuário.
 * @property nome Nome completo do usuário.
 * @property tipo Tipo ou perfil do usuário (ex: Coordenador, Apoio, Gestor).
 */
data class Usuario(
    val id: Int,
    val nome: String,
    val tipo: String
) {

    /**
     * Representa o usuário como uma string no formato "Nome (Tipo)".
     *
     * Isso facilita exibir informações resumidas em listas ou logs.
     */
    override fun toString(): String {
        return "$nome ($tipo)"
    }
}
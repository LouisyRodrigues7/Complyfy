package com.example.pivot.model

import com.example.pivot.adapter.TipoNotificacao

/**
 * Representa uma notificação associada a um usuário.
 *
 * Esse modelo é utilizado para exibir alertas sobre prazos, atrasos, atualizações de atividades,
 * e outras informações relevantes no sistema.
 *
 * Pode estar ligada diretamente a uma atividade específica, mas também aceita notificações gerais.
 *
 * @property id Identificador único da notificação.
 * @property mensagem Texto da notificação exibida ao usuário.
 * @property data Timestamp de criação da notificação (em milissegundos desde epoch).
 * @property lida Indica se a notificação já foi visualizada pelo usuário.
 * @property atividadeId ID da atividade associada (pode ser null para notificações gerais).
 * @property tipo_notificacao Tipo da notificação (definido por enum [TipoNotificacao]).
 */
data class Notificacao(
    val id: Int,
    val mensagem: String,
    val data: Long,
    val lida: Boolean,
    val atividadeId: Int?,
    val tipo_notificacao: TipoNotificacao
)

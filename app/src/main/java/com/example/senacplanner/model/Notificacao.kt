package com.example.senacplanner.model

import com.example.senacplanner.adapter.TipoNotificacao

data class Notificacao(
    val id: Int,
    val mensagem: String,
    val data: Long,
    val lida: Boolean,
    val atividadeId: Int?,
    val tipo_notificacao: TipoNotificacao
)
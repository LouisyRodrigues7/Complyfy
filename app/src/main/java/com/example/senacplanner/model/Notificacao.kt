package com.example.senacplanner.model

data class Notificacao(
    val id: Int,
    val mensagem: String,
    val data: Long,
    val lida: Boolean
)
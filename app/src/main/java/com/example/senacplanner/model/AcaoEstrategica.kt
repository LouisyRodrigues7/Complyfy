package com.example.senacplanner.model

data class AcaoEstrategica(
    val id: Int,
    val pilarId: Int,
    val nome: String,
    val descricao: String? = null,
    val criadoPor: Int,
    val aprovado: Boolean = false
)
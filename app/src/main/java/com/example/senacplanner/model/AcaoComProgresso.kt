package com.example.senacplanner.model

data class AcaoComProgresso(
    val nome: String,
    val total: Int,
    val concluidas: Int,
    val atrasadas: Int,
    val andamento: Int = 0
)


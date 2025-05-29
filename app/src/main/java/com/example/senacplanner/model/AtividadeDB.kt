package com.example.senacplanner.model

data class AtividadeDB(
    val id: Int,
    val nome: String,
    val status: String,
    val dataInicio: String,
    val dataConclusao: String,
    val responsavelId: Int,
    val criadoPor: Int,
    val acaoId: Int
)

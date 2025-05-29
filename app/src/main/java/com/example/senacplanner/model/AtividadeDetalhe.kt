package com.example.senacplanner.model

data class AtividadeDetalhe(
    val nome: String,
    val nomeResponsavel: String,
    val dataInicio: String,
    val dataConclusao: String?,
    val status: String
)


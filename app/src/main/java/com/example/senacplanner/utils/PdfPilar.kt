package com.example.senacplanner.utils

data class PdfPilar(
    val id: Int,
    val numero: Int,
    val nome: String,
    val descricao: String?,
    val dataInicio: String,
    val dataConclusao: String?,
    val acoes: List<PdfAcao>
)
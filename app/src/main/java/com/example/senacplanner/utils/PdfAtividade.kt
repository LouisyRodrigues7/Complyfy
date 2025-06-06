package com.example.senacplanner.utils

data class PdfAtividade(
    val id: Int,
    val nome: String,
    val descricao: String?,
    val status: String,
    val dataInicio: String,
    val dataConclusao: String?,
    val responsavel: PdfUsuario?
)
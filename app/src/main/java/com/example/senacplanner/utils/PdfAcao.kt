package com.example.senacplanner.utils

data class PdfAcao(
    val id: Int,
    val nome: String,
    val descricao: String?,
    val atividades: List<PdfAtividade>
)
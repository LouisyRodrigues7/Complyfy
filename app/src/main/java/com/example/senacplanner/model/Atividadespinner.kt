package com.example.senacplanner.model

enum class StatusAtividade(val status: String) {
    EM_ANDAMENTO("Em andamento"),
    FINALIZADA("Finalizada")
}

data class Atividadespinner(
    val id: Int,
    val acaoId: Int,
    val nome: String,
    val descricao: String? = null,
    val status: StatusAtividade = StatusAtividade.EM_ANDAMENTO,
    val dataInicio: String,
    val dataConclusao: String? = null,
    val criadoPor: Int,
    val aprovado: Boolean = false,
    val responsavelId: Int? = null
)
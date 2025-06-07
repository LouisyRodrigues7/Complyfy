package com.example.pivot.model

/**
 * Representa os detalhes de uma atividade para exibição no app.
 *
 * Essa classe é usada principalmente para exibir informações amigáveis ao usuário,
 * como nome do responsável e datas formatadas, sem incluir IDs técnicos.
 *
 * @property nome Nome da atividade.
 * @property nomeResponsavel Nome do usuário responsável por executá-la.
 * @property dataInicio Data de início da atividade (exibida no formato dd/MM/yyyy).
 * @property dataConclusao Data de conclusão (se houver), ou nula caso ainda não finalizada.
 * @property status Situação atual da atividade (ex: "Em andamento", "Finalizada").
 */
data class AtividadeDetalhe(
    val nome: String,
    val nomeResponsavel: String,
    val dataInicio: String,
    val dataConclusao: String?,
    val status: String
)

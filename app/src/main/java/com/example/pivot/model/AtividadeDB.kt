package com.example.pivot.model

/**
 * Representa uma Atividade registrada no banco de dados.
 *
 * Esta classe modela as informações essenciais de uma atividade pertencente a uma ação estratégica,
 * incluindo status, datas e vínculos com usuários e ações.
 *
 * @property id Identificador único da atividade.
 * @property nome Nome ou título da atividade.
 * @property status Situação atual da atividade (ex: "Em andamento", "Finalizada").
 * @property dataInicio Data prevista ou real de início.
 * @property dataConclusao Data prevista ou real de término.
 * @property responsavelId ID do usuário designado para executar a atividade.
 * @property criadoPor ID do usuário que criou a atividade.
 * @property acaoId ID da ação estratégica à qual esta atividade pertence.
 */
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

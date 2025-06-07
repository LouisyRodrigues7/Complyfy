package com.example.senacplanner.editarAtividade

/**
 * Representa uma atividade que pode ser editada no sistema.
 *
 * Essa classe é usada principalmente para transporte de dados (DTO) entre camadas,
 * mantendo as informações relevantes para a edição de uma atividade existente.
 *
 * @property id Identificador único da atividade.
 * @property nome Nome da atividade, usado para identificação e exibição.
 * @property status Estado atual da atividade (ex: "pendente", "concluída").
 * @property data_inicio Data prevista ou real de início da atividade.
 * @property data_conclusao Data prevista ou real de término da atividade.
 * @property responsavel_id ID do usuário responsável por executar essa atividade.
 * @property criado_por ID do usuário que criou a atividade originalmente.
 * @property acao_id Identificador da ação à qual essa atividade está vinculada.
 */
data class AtividadeEdit(
    val id: Int,
    val nome: String,
    val status: String,
    val data_inicio: String,
    val data_conclusao: String,
    val responsavel_id: Int,
    val criado_por: Int,
    val acao_id: Int
)

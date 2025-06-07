package com.example.pivot.model

/**
 * Representa uma Ação Estratégica vinculada a um Pilar.
 *
 * Essa classe é utilizada para manipular e exibir informações sobre ações criadas no sistema,
 * incluindo sua associação com um pilar, quem criou e se está aprovada ou não.
 *
 * @property id Identificador único da ação no banco de dados.
 * @property pilarId ID do pilar ao qual a ação está associada.
 * @property nome Nome descritivo da ação estratégica.
 * @property descricao Texto opcional com detalhes complementares da ação.
 * @property criadoPor ID do usuário responsável pela criação da ação.
 * @property aprovado Indica se a ação foi aprovada por um coordenador ou gestor.
 */
data class AcaoEstrategica(
    val id: Int,
    val pilarId: Int,
    val nome: String,
    val descricao: String? = null,
    val criadoPor: Int,
    val aprovado: Boolean = false
)

package com.example.senacplanner.model

/**
 * Representa um período de tempo fixo usado para filtrar dados em relatórios de compliance.
 *
 * Esse modelo é utilizado no contexto de geração de relatórios, permitindo ao usuário
 * selecionar períodos como "1º semestre", "2º semestre", "Ano todo", etc.
 *
 * @property id Identificador único do período no banco de dados.
 * @property descricao Texto descritivo do período (ex: "Janeiro a Junho de 2025").
 */
data class RelatorioPeriodo(
    val id: Int,
    val descricao: String
)

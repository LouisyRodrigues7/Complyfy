package com.example.senacplanner.Acoes.Type

/**
 * Representa um Pilar, que é uma categoria ou eixo estratégico no sistema.
 *
 * @property id Identificador único do pilar.
 * @property numero Número sequencial do pilar.
 * @property nome Nome do pilar.
 * @property descricao Descrição opcional do pilar.
 */
data class PilarType(
    val id: Int,
    val numero: Int,
    val nome: String,
    val descricao: String?,
)
package com.example.senacplanner.utils

/**
 * Representa um pilar estratégico no contexto de geração de relatórios em PDF.
 *
 * Cada pilar pode conter diversas ações estratégicas e seus respectivos dados de planejamento.
 *
 * @property id Identificador único do pilar
 * @property numero Número sequencial ou identificador visual do pilar
 * @property nome Nome do pilar (ex: "Educação", "Sustentabilidade")
 * @property descricao Descrição opcional com mais detalhes sobre o pilar
 * @property dataInicio Data de início do pilar (formato string)
 * @property dataConclusao Data de término prevista, se houver
 * @property acoes Lista de ações associadas a esse pilar
 */
data class PdfPilar(
    val id: Int,
    val numero: Int,
    val nome: String,
    val descricao: String?,
    val dataInicio: String,
    val dataConclusao: String?,
    val acoes: List<PdfAcao>
)

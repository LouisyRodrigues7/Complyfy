package com.example.senacplanner.utils

/**
 * Representa uma ação estratégica dentro de um relatório em PDF.
 *
 * Cada ação pode conter uma lista de atividades associadas.
 *
 * @property id Identificador único da ação
 * @property nome Nome da ação
 * @property descricao Descrição opcional da ação
 * @property atividades Lista de atividades vinculadas à ação
 */
data class PdfAcao(
    val id: Int,
    val nome: String,
    val descricao: String?,
    val atividades: List<PdfAtividade>
)

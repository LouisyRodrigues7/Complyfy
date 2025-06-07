package com.example.pivot.utils

/**
 * Representa um usuário de forma simplificada para fins de geração de relatório em PDF.
 *
 * Essa classe é usada apenas na estruturação de dados do relatório, não como modelo persistente de banco.
 *
 * @property id Identificador único do usuário
 * @property nome Nome completo ou identificador textual do usuário
 */
data class PdfUsuario(
    val id: Int,
    val nome: String
)

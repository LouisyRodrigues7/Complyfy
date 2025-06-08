package com.example.pivot.model

/**
 * Representa um Pilar de forma simplificada para uso em seletores (como spinners).
 *
 * Essa estrutura é usada quando não é necessário carregar todas as informações do Pilar,
 * mas apenas o seu ID e nome — por exemplo, ao montar listas de seleção em telas de filtro ou consulta.
 *
 * @property id Identificador único do Pilar no banco de dados.
 * @property nome Nome do Pilar exibido no UI.
 */
data class Pilarspinner(
    val id: Int,
    val nome: String
)

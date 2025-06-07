package com.example.pivot.model

/**
 * Representa um pilar estratégico utilizado na geração de relatórios de compliance.
 *
 * Essa classe é usada para compor a seleção de pilares disponíveis para geração de relatórios.
 * Não contém dados completos do pilar, apenas o essencial para identificá-lo visualmente.
 *
 * @property id Identificador único do pilar.
 * @property nome Nome do pilar exibido ao usuário.
 */
data class RelatorioPilar(
    val id: Int,
    val nome: String
)

package com.example.pivot.model

/**
 * Representa um Pilar com dados agregados de progresso de atividades.
 *
 * Essa estrutura é usada para visualizações gráficas e dashboards, permitindo
 * entender quantas atividades estão concluídas em relação ao total previsto.
 *
 * @property nome Nome do pilar (ex: "Gestão", "Transparência").
 * @property total Quantidade total de atividades associadas ao pilar.
 * @property concluidas Quantidade de atividades marcadas como concluídas.
 */
data class PilarComProgresso(
    val nome: String,
    val total: Int,
    val concluidas: Int
)

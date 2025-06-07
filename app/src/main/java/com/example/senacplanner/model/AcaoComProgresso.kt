package com.example.senacplanner.model

/**
 * Representa uma Ação Estratégica com o progresso detalhado de suas atividades.
 *
 * Usado principalmente para visualizações gráficas (ex: gráficos de barras),
 * mostrando a quantidade total de atividades, quantas foram concluídas,
 * quantas estão em atraso e quantas ainda estão em andamento.
 *
 * @property nome Nome da ação estratégica.
 * @property total Total de atividades vinculadas à ação.
 * @property concluidas Quantidade de atividades finalizadas.
 * @property atrasadas Quantidade de atividades em atraso.
 * @property andamento Quantidade de atividades em andamento (não finalizadas e dentro do prazo).
 */
data class AcaoComProgresso(
    val nome: String,
    val total: Int,
    val concluidas: Int,
    val atrasadas: Int,
    val andamento: Int = 0
)

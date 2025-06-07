package com.example.pivot.adapter

/**
 * Enumeração que representa os tipos possíveis de notificações no sistema.
 *
 * Tipos disponíveis:
 * - [GERAL]: Notificação geral, sem categoria específica.
 * - [APROVACAO_ATIVIDADE]: Notificação relacionada à aprovação de atividades.
 * - [APROVACAO_ACAO]: Notificação relacionada à aprovação de ações.
 * - [CONCLUSAO_STATUS]: Notificação sobre a conclusão de status.
 * - [IMPORTANTE]: Notificação classificada como importante.
 * - [ALERTA]: Notificação de alerta, indicando urgência ou atenção.
 */
enum class TipoNotificacao {
    GERAL,
    APROVACAO_ATIVIDADE,
    APROVACAO_ACAO,
    CONCLUSAO_STATUS,
    IMPORTANTE,
    ALERTA
}
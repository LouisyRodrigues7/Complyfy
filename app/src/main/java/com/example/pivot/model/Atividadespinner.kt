package com.example.pivot.model

/**
 * Enum representando os possíveis estados de uma atividade dentro do sistema.
 *
 * Apesar de o sistema também usar `status` como `String` em alguns pontos (como no banco de dados),
 * este enum serve como referência centralizada para valores padronizados.
 *
 * @property status Representação legível do status da atividade.
 */
enum class StatusAtividade(val status: String) {
    EM_ANDAMENTO("Em andamento"),
    FINALIZADA("Finalizada")
}

/**
 * Representa uma atividade para exibição em um Spinner (dropdown) de seleção.
 *
 * Usada especialmente em telas onde o usuário precisa escolher uma atividade vinculada
 * a uma ação estratégica. É uma versão simplificada da entidade completa.
 *
 * @property id Identificador único da atividade.
 * @property acaoId ID da ação à qual a atividade está vinculada.
 * @property nome Nome da atividade (exibido ao usuário).
 * @property descricao Descrição opcional da atividade.
 * @property status Status atual da atividade (salvo como String).
 * @property dataInicio Data de início da atividade (no formato yyyy-MM-dd).
 * @property dataConclusao Data de conclusão, se houver.
 * @property criadoPor ID do usuário que criou a atividade.
 * @property aprovado Indica se a atividade foi aprovada ou não.
 * @property responsavelId ID do usuário responsável pela execução, se definido.
 */
data class Atividadespinner(
    val id: Int,
    val acaoId: Int,
    val nome: String,
    val descricao: String? = null,
    val status: String,
    val dataInicio: String,
    val dataConclusao: String? = null,
    val criadoPor: Int,
    val aprovado: Boolean = false,
    val responsavelId: Int? = null
)

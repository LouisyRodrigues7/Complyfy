package com.example.senacplanner.utils

/**
 * Representa uma atividade dentro de uma ação estratégica, usada na geração de relatórios PDF.
 *
 * Essa estrutura é aninhada dentro de [PdfAcao], e inclui dados detalhados da atividade.
 *
 * @property id Identificador único da atividade
 * @property nome Nome da atividade
 * @property descricao Texto opcional com detalhes adicionais da atividade
 * @property status Estado atual da atividade (ex: "Em andamento", "Finalizada")
 * @property dataInicio Data de início da atividade (formato string)
 * @property dataConclusao Data de conclusão, se houver
 * @property responsavel Representa o usuário responsável pela atividade
 */
data class PdfAtividade(
    val id: Int,
    val nome: String,
    val descricao: String?,
    val status: String,
    val dataInicio: String,
    val dataConclusao: String?,
    val responsavel: PdfUsuario?
)

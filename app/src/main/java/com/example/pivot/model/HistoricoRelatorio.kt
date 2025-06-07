package com.example.pivot.model

/**
 * Representa um item de histórico de geração de relatório em PDF.
 *
 * Esse modelo é usado para armazenar metadados dos relatórios gerados,
 * como o nome do arquivo, URI para acesso e a data de criação.
 *
 * Essa estrutura é utilizada, por exemplo, pelo [com.example.pivot.utils.HistoricoRelatorioManager]
 * para salvar e recuperar o histórico de relatórios.
 *
 * @property nome Nome do relatório (ex: "relatorio_compliance").
 * @property uri URI onde o arquivo PDF foi salvo (geralmente um caminho no armazenamento externo).
 * @property data Timestamp (em milissegundos) da geração do relatório.
 */
data class HistoricoRelatorio(
    val nome: String,
    val uri: String,
    val data: Long
)

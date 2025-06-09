package com.example.pivot.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pivot.utils.PdfAcao
import com.example.pivot.utils.PdfAtividade
import com.example.pivot.utils.PdfPilar
import com.example.pivot.utils.PdfUsuario
import com.example.pivot.model.RelatorioPilar
import com.example.pivot.model.RelatorioPeriodo
import java.io.File
import java.io.FileOutputStream

/**
 * Helper para gerenciar acesso ao banco de dados pré-carregado usado na geração de relatórios.
 * Essa classe garante que o banco de dados esteja copiado da pasta assets para o local interno do app
 * e oferece métodos para consulta estruturada dos pilares, ações e atividades para montar relatórios.
 */
class RelatorioDatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, "novobanco_.db", null, 1) {


    /**
     * Caminho absoluto do arquivo do banco de dados na pasta interna do app.
     */
    private val dbPath: String
        get() = context.getDatabasePath("novobanco_.db").path

    /**
     * Retorna o banco aberto para leitura e escrita, copiando o arquivo do assets caso necessário.
     * Isso garante que o banco pré-carregado esteja disponível para consultas.
     */
    override fun getReadableDatabase(): SQLiteDatabase {
        copiarBancoSeNecessario()
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
    }


    override fun onCreate(db: SQLiteDatabase?) {
        // Ignorado: banco é pré-carregado
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Ignorado: banco é pré-carregado
    }

    /**
     * Copia o arquivo do banco de dados da pasta assets para a pasta interna do app,
     * somente se o arquivo ainda não existir, para evitar sobrescrever dados.
     */
    private fun copiarBancoSeNecessario() {
        val dbFile = File(dbPath)
        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            context.assets.open("RelatorioDB.db").use { inputStream ->
                FileOutputStream(dbFile).use { output ->
                    inputStream.copyTo(output) // copia o conteúdo do asset para o arquivo interno
                }
            }
        }
    }

    /**
     * Busca todos os pilares no banco e monta objetos PdfPilar completos com suas ações e atividades,
     * filtrando as atividades pelo período passado (em meses).
     *
     * @param db Instância do banco para realizar consultas.
     * @param periodoMeses Quantidade de meses para filtrar as atividades do relatório.
     * @return Lista de pilares com dados completos para geração do relatório PDF.
     */
    fun buscarPilaresParaRelatorio(db: SQLiteDatabase, periodoMeses: Int): List<PdfPilar> {
        val cursor = db.rawQuery("SELECT * FROM Pilar", null)
        val pilares = mutableListOf<PdfPilar>()
        if (cursor.moveToFirst()) {
            do {
                val pilar = montarPilarCompletoParaRelatorio(
                    db,
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    periodoMeses
                )
                pilar?.let { pilares.add(it) }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return pilares
    }

    /**
     * Busca um pilar específico por seu ID e monta o objeto PdfPilar completo para relatório,
     * incluindo suas ações e atividades filtradas pelo período.
     *
     * @param db Instância do banco para consulta.
     * @param pilarId ID do pilar a ser buscado.
     * @param periodoMeses Quantidade de meses para filtrar as atividades.
     * @return Lista contendo o pilar encontrado ou vazia caso não exista.
     */
    fun buscarPilarPorIdParaRelatorio(db: SQLiteDatabase, pilarId: Int, periodoMeses: Int): List<PdfPilar> {
        val pilar = montarPilarCompletoParaRelatorio(db, pilarId, periodoMeses)
        return if (pilar != null) listOf(pilar) else emptyList()
    }

    /**
     * Monta um objeto PdfPilar completo a partir do banco, carregando seus dados básicos,
     * e suas ações e atividades relacionadas, filtradas pelo período.
     *
     * @param db Banco para consulta.
     * @param pilarId ID do pilar.
     * @param periodoMeses Quantidade de meses para filtrar atividades.
     * @return Objeto PdfPilar completo ou null caso pilar não seja encontrado.
     */
    private fun montarPilarCompletoParaRelatorio(db: SQLiteDatabase, pilarId: Int, periodoMeses: Int): PdfPilar? {
        val cursor = db.rawQuery("SELECT * FROM Pilar WHERE id = ?", arrayOf(pilarId.toString()))
        var pilar: PdfPilar? = null

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val numero = cursor.getInt(cursor.getColumnIndexOrThrow("numero"))
            val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
            val descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao"))
            val dataInicio = cursor.getString(cursor.getColumnIndexOrThrow("data_inicio"))
            val dataConclusao = cursor.getString(cursor.getColumnIndexOrThrow("data_conclusao"))

            val acoes = buscarAcoesDoPilarParaRelatorio(db, id, periodoMeses)

            pilar = PdfPilar(
                id = id,
                numero = numero,
                nome = nome,
                descricao = descricao,
                dataInicio = dataInicio,
                dataConclusao = dataConclusao,
                acoes = acoes
            )
        }
        cursor.close()
        return pilar
    }

    /**
     * Busca todos os pilares do banco para popular componentes de UI como spinner,
     * adicionando a opção "Todos" para seleção geral.
     *
     * @param db Instância do banco para consulta.
     * @return Lista de objetos RelatorioPilar para uso em filtro ou seleção.
     */
    fun buscarPilares(db: SQLiteDatabase): List<RelatorioPilar> {
        val pilares = mutableListOf<RelatorioPilar>()
        pilares.add(RelatorioPilar(id = -1, nome = "Todos"))

        val cursor = db.rawQuery("SELECT id, nome FROM Pilar", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                pilares.add(RelatorioPilar(id = id, nome = nome))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return pilares
    }

    /**
     * Retorna uma lista fixa de períodos disponíveis para seleção em relatórios,
     * representando meses anteriores para filtro temporal.
     *
     * @return Lista de períodos pré-definidos (3, 6 e 12 meses).
     */
    fun buscarPeriodosFixos(): List<RelatorioPeriodo> {
        return listOf(
            RelatorioPeriodo(id = 3, descricao = "Últimos 3 meses"),
            RelatorioPeriodo(id = 6, descricao = "Últimos 6 meses"),
            RelatorioPeriodo(id = 12, descricao = "Últimos 12 meses")
        )
    }

    /**
     * Busca todas as ações vinculadas a um pilar, carregando também as atividades
     * associadas a cada ação, filtradas pelo período.
     *
     * @param db Banco para consulta.
     * @param pilarId ID do pilar cujas ações serão buscadas.
     * @param periodoMeses Filtro temporal para atividades relacionadas.
     * @return Lista de objetos PdfAcao com suas atividades associadas.
     */
    private fun buscarAcoesDoPilarParaRelatorio(db: SQLiteDatabase, pilarId: Int, periodoMeses: Int): List<PdfAcao> {
        val cursor = db.rawQuery("SELECT * FROM Acao WHERE pilar_id = ?", arrayOf(pilarId.toString()))
        val acoes = mutableListOf<PdfAcao>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                val descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao"))

                val atividades = buscarAtividadesDaAcaoParaRelatorio(db, id, periodoMeses)

                acoes.add(
                    PdfAcao(
                        id = id,
                        nome = nome,
                        descricao = descricao,
                        atividades = atividades
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return acoes
    }

    /**
     * Normaliza uma string de status para facilitar comparações e filtragens,
     * removendo acentos, convertendo para minúsculas e eliminando espaços em branco.
     *
     * @param status Texto original do status.
     * @return Status normalizado ou string vazia se nulo ou em branco.
     */
    fun normalizarStatus(status: String?): String {
        if (status.isNullOrBlank()) return ""
        return java.text.Normalizer.normalize(status, java.text.Normalizer.Form.NFD)
            .replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
            .lowercase()
            .trim()
    }


    /**
     * Busca as atividades vinculadas a uma ação, filtrando pela data de início ou conclusão
     * dentro do período passado. Isso garante que só atividades recentes sejam consideradas.
     *
     * @param db Banco para consulta.
     * @param acaoId ID da ação para buscar as atividades.
     * @param periodoMeses Quantidade de meses para filtro temporal.
     * @return Lista de atividades no formato PdfAtividade.
     */
    private fun buscarAtividadesDaAcaoParaRelatorio(db: SQLiteDatabase, acaoId: Int, periodoMeses: Int): List<PdfAtividade> {
        val atividades = mutableListOf<PdfAtividade>()

        // Query filtra atividades por data de início ou conclusão dentro do período desejado
        val query = """
    SELECT * FROM Atividade
    WHERE acao_id = ?
      AND (
        date(data_inicio) >= date('now', '-$periodoMeses months')
        OR (data_conclusao IS NOT NULL AND date(data_conclusao) >= date('now', '-$periodoMeses months'))
      )
""".trimIndent()



        val cursor = db.rawQuery(query, arrayOf(acaoId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                val descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao"))
                val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
                val dataInicio = cursor.getString(cursor.getColumnIndexOrThrow("data_inicio"))
                val dataConclusao = cursor.getString(cursor.getColumnIndexOrThrow("data_conclusao"))
                val responsavelId = cursor.getInt(cursor.getColumnIndexOrThrow("responsavel_id"))

                val responsavel = buscarUsuarioPorIdParaRelatorio(db, responsavelId)

                atividades.add(
                    PdfAtividade(
                        id = id,
                        nome = nome,
                        descricao = descricao,
                        status = status,
                        dataInicio = dataInicio,
                        dataConclusao = dataConclusao,
                        responsavel = responsavel
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()

        return atividades
    }

    /**
     * Busca dados básicos de um usuário pelo ID para associar às atividades nos relatórios.
     * Retorna null caso o ID seja zero (indicando ausência de responsável).
     *
     * @param db Banco para consulta.
     * @param usuarioId ID do usuário buscado.
     * @return Objeto PdfUsuario com dados do usuário ou null.
     */
    private fun buscarUsuarioPorIdParaRelatorio(db: SQLiteDatabase, usuarioId: Int): PdfUsuario? {
        if (usuarioId == 0) return null

        val cursor = db.rawQuery("SELECT id, nome FROM Usuario WHERE id = ?", arrayOf(usuarioId.toString()))
        var usuario: PdfUsuario? = null

        if (cursor.moveToFirst()) {
            usuario = PdfUsuario(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
            )
        }
        cursor.close()
        return usuario
    }

}

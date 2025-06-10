package com.example.pivot.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pivot.Acoes.Type.Acao
import com.example.pivot.Acoes.Type.AcaoComAtividades
import com.example.pivot.Acoes.Type.Atividade
import com.example.pivot.Acoes.Type.PilarType
import com.example.pivot.Acoes.Type.Usuario
import com.example.pivot.adapter.TipoNotificacao
import com.example.pivot.editarAtividade.AtividadeEdit
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import java.util.Locale
import android.util.Log
import com.example.pivot.model.AtividadeDetalhe
import com.example.pivot.model.PilarItem
import com.example.pivot.model.AcaoComProgresso
import com.example.pivot.model.PilarComProgresso
import com.example.pivot.model.Pilarspinner
import com.example.pivot.model.AcaoEstrategica
import com.example.pivot.model.Atividadespinner
import android.database.Cursor


/**
 * Classe responsável por gerenciar o banco de dados SQLite da aplicação.
 *
 * Estende `SQLiteOpenHelper` para lidar com a criação, acesso, cópia e upgrade do banco.
 * Contém também métodos utilitários para realizar operações de leitura e escrita específicas.
 *
 * @param context Contexto da aplicação usado para acessar recursos e diretórios.
 */
class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "bancov2.db"
        private const val DB_VERSION = 1
    }

    private val dbPath: String = context.getDatabasePath(DB_NAME).path
    private var myDatabase: SQLiteDatabase? = null

    init {
        createDatabase()
    }

    /**
     * Verifica se o banco já existe e, caso contrário, realiza a cópia do banco de dados da pasta assets.
     */
    private fun createDatabase() {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) {
            this.readableDatabase.close()
            try {
                copyDatabase()
            } catch (e: IOException) {
                throw RuntimeException("Erro ao copiar banco de dados", e)
            }
        }
    }

    /**
     * Copia o banco de dados da pasta assets para o diretório de bancos da aplicação.
     *
     * Usado apenas na primeira execução ou se o banco for apagado.
     */
    private fun copyDatabase() {
        val inputStream = context.assets.open(DB_NAME)
        val outputStream = FileOutputStream(dbPath)

        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }

    override fun onCreate(db: SQLiteDatabase?) { } // Implementação não necessária pois o banco já vem pronto dos assets.

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {} // Lógica de upgrade pode ser implementada futuramente se necessário.

    /**
     * Abre uma instância do banco de dados em modo leitura/escrita.
     */
    fun openDatabase() {
        myDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
    }

    /**
     * Fecha a instância atual do banco de dados, se estiver aberta.
     */
    override fun close() {
        myDatabase?.close()
        super.close()
    }

    /**
     * Garante que o banco esteja aberto e o retorna para uso nas operações.
     *
     * @return Instância de `SQLiteDatabase` pronta para uso.
     */
    fun getDatabase(): SQLiteDatabase {
        if (myDatabase == null || !myDatabase!!.isOpen) {
            openDatabase()
        }
        return myDatabase!!
    }


    /**
     * Retorna todos os pilares disponíveis no banco.
     *
     * @return Lista de objetos `PilarType` representando cada pilar cadastrado.
     */
    fun getAllPilares(): List<PilarType> {
        val pilares = mutableListOf<PilarType>()
        val db = getDatabase()
        val cursor = db.rawQuery("SELECT id, numero, nome, descricao FROM Pilar", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val numero = cursor.getInt(cursor.getColumnIndexOrThrow("numero"))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                val descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao"))
                pilares.add(PilarType(id, numero, nome, descricao))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return pilares
    }

    /**
     * Retorna os pilares associados a um determinado usuário.
     *
     * Utilizado para exibir apenas os pilares vinculados ao perfil logado.
     *
     * @param usuarioId ID do usuário.
     * @return Lista de `PilarType` associados ao usuário.
     */
    fun getPilaresByUsuarioId(usuarioId: Int): List<PilarType> {
        val pilares = mutableListOf<PilarType>()
        val db = getDatabase()
        val query = """
            SELECT p.id, p.numero, p.nome, p.descricao
            FROM Pilar p
            JOIN UsuarioPilar up ON p.id = up.pilar_id
            WHERE up.usuario_id = ?
        """
        val cursor = db.rawQuery(query, arrayOf(usuarioId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val numero = cursor.getInt(cursor.getColumnIndexOrThrow("numero"))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                val descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao"))
                pilares.add(PilarType(id, numero, nome, descricao))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return pilares
    }

    /**
     * Retorna a lista de ações de um pilar com informações de progresso.
     *
     * Essa função calcula o total de atividades, quantas foram finalizadas
     * e quantas estão atrasadas com base na data de conclusão.
     *
     * @param pilarId ID do pilar cujas ações devem ser consultadas.
     * @return Lista de `AcaoComProgresso` com dados agregados por ação.
     */
    fun getAcoesComProgressoDoPilar(pilarId: Int): List<AcaoComProgresso> {
        val lista = mutableListOf<AcaoComProgresso>()
        val db = readableDatabase

        val cursor = db.rawQuery("""
        SELECT a.nome,
               COUNT(at.id) AS total,
               SUM(CASE WHEN at.status = 'Finalizada' THEN 1 ELSE 0 END) AS concluidas,
               SUM(CASE 
                    WHEN at.status != 'Finalizada' 
                         AND date(at.data_conclusao) < date('now') 
                    THEN 1 ELSE 0 
               END) AS atrasadas
        FROM Acao a
        LEFT JOIN Atividade at ON a.id = at.acao_id
        WHERE a.pilar_id = ?
        GROUP BY a.nome
    """.trimIndent(), arrayOf(pilarId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val nome = cursor.getString(0)
                val total = cursor.getInt(1)
                val concluidas = cursor.getInt(2)
                val atrasadas = cursor.getInt(3)

                lista.add(
                    AcaoComProgresso(
                        nome = nome,
                        total = total,
                        concluidas = concluidas,
                        atrasadas = atrasadas
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return lista
    }

    /**
     * Retorna o progresso de todas os pilares cadastrados.
     *
     * A consulta retorna a quantidade total de atividades e quantas estão finalizadas por pilar.
     * Útil para exibir progresso geral por pilar em dashboards ou relatórios.
     *
     * @return Lista de objetos `PilarComProgresso` contendo nome, total e concluídas.
     */
    fun getProgressoTodosPilares(): List<PilarComProgresso> {
        val lista = mutableListOf<PilarComProgresso>()
        val db = readableDatabase

        val cursor = db.rawQuery("""
        SELECT p.nome,
               COUNT(at.id) as total,
               SUM(CASE WHEN at.status = 'Finalizada' THEN 1 ELSE 0 END) as concluidas
        FROM Pilar p
        LEFT JOIN Acao a ON a.pilar_id = p.id
        LEFT JOIN Atividade at ON at.acao_id = a.id
        GROUP BY p.nome
    """, null)

        if (cursor.moveToFirst()) {
            do {
                lista.add(
                    PilarComProgresso(
                        nome = cursor.getString(0),
                        total = cursor.getInt(1),
                        concluidas = cursor.getInt(2)
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return lista
    }



    /**
     * Retorna todos os pilares existentes, ordenados pelo número.
     *
     * Usado para preencher listas de seleção ou montar visões hierárquicas.
     *
     * @return Lista de `PilarItem` com os campos id, número e nome.
     */
    fun getTodosPilares(): List<PilarItem> {
        val pilares = mutableListOf<PilarItem>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT id, numero, nome FROM Pilar ORDER BY numero ASC", null)

        if (cursor.moveToFirst()) {
            do {
                val pilar = PilarItem(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    numero = cursor.getInt(cursor.getColumnIndexOrThrow("numero")),
                    nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                )
                pilares.add(pilar)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return pilares
    }

    /**
     * Retorna todas as ações pertencentes a um determinado pilar.
     *
     * Ideal para navegação hierárquica ou quando se quer detalhar um pilar.
     *
     * @param pilarId ID do pilar.
     * @return Lista de `Acao` associadas ao pilar informado.
     */
    fun getAcoesByPilarId(pilarId: Int): List<Acao> {
        val listaAcoes = mutableListOf<Acao>()
        val db = getDatabase()

        val cursor = db.rawQuery(
            "SELECT id, nome, pilar_id FROM Acao WHERE pilar_id = ?",
            arrayOf(pilarId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                val pilarId = cursor.getInt(cursor.getColumnIndexOrThrow("pilar_id"))
                listaAcoes.add(Acao(id = id, nome = nome, pilarId))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return listaAcoes
    }

    // relatorio
    /**
     * Lista todos os usuários com o tipo 'Coordenador' ou 'Apoio'.
     *
     * Usado para preencher `Spinner`s ou para atribuir responsáveis às atividades.
     *
     * @return Lista de `Usuario` com permissões administrativas.
     */
    fun listarResponsaveis(): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM Usuario WHERE tipo = 'Coordenador' OR tipo = 'Apoio'", null
        )

        if (cursor.moveToFirst()) {
            do {
                val usuario = Usuario(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                    tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
                )
                lista.add(usuario)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }

    /**
     * Cria uma notificação para todos os usuários do sistema.
     *
     * Utilizado em eventos como atualização ou exclusão de atividades.
     *
     * @param mensagem Texto da notificação.
     * @param atividadeId ID da atividade relacionada (opcional).
     */
    fun notificarTodosUsuarios(mensagem: String, atividadeId: Int? = null) {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT id FROM Usuario", null)
        val dataAtual = System.currentTimeMillis().toString()

        if (cursor.moveToFirst()) {
            do {
                val usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val values = ContentValues().apply {
                    put("usuario_id", usuarioId)
                    put("mensagem", mensagem)
                    put("data", dataAtual)
                    put("lida", 0)
                    if (atividadeId != null) put("atividade_id", atividadeId)
                }
                db.insert("Notificacao", null, values)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
    }

    // ✔️
    /**
     * Busca todos os pilares do banco para popular um Spinner.
     *
     * Ideal para interfaces onde o usuário deve escolher um pilar existente.
     *
     * @return Lista de `Pilarspinner` com `id` e `nome` de cada pilar.
     */
    fun buscarPilaresParaSelecao(): List<Pilarspinner> {
        val lista = mutableListOf<Pilarspinner>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, nome FROM Pilar", null)

        if (cursor.moveToFirst()) {
            do {
                val pilar = Pilarspinner(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                )
                lista.add(pilar)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }


    /**
     * Retorna o progresso de todas as ações de um determinado pilar,
     * incluindo o total de atividades, finalizadas, em andamento e em atraso.
     *
     * Útil para gerar relatórios e visualizar performance geral do pilar.
     *
     * @param pilarId ID do pilar cujas ações serão analisadas.
     * @return Lista de `AcaoComProgresso` com status detalhado de cada ação.
     */
    fun getAcoesComAtrasoDoPilar(pilarId: Int): List<AcaoComProgresso> {
        val db = readableDatabase
        val lista = mutableListOf<AcaoComProgresso>()

        val query = """
    SELECT a.nome,
           COUNT(ata.id) AS total,
           COALESCE(SUM(CASE WHEN LOWER(TRIM(ata.status)) = 'finalizada' THEN 1 ELSE 0 END), 0) AS concluidas,
           COALESCE(SUM(CASE WHEN LOWER(TRIM(ata.status)) = 'em andamento' THEN 1 ELSE 0 END), 0) AS andamento,
           COALESCE(SUM(CASE WHEN LOWER(TRIM(ata.status)) = 'em atraso' THEN 1 ELSE 0 END), 0) AS atrasadas
    FROM acao a
    LEFT JOIN Atividade ata ON a.id = ata.acao_id
    WHERE a.pilar_id = ?
    GROUP BY a.nome
""".trimIndent()


        val cursor = db.rawQuery(query, arrayOf(pilarId.toString()))

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val nome = cursor.getString(0) ?: ""
                val total = cursor.getInt(1)
                val concluidas = cursor.getInt(2)
                val andamento = cursor.getInt(3)
                val atrasadas = cursor.getInt(4)

                val acao = AcaoComProgresso(
                    nome = nome,
                    total = total,
                    concluidas = concluidas,
                    andamento = andamento,
                    atrasadas = atrasadas
                )

                lista.add(acao)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return lista
    }


    /**
     * Retorna todas as ações vinculadas a um pilar específico, com informações detalhadas.
     *
     * Normalmente usada para popular interfaces onde o usuário deve escolher ações de um pilar.
     *
     * @param pilarId ID do pilar relacionado.
     * @return Lista de `AcaoEstrategica` com detalhes relevantes de cada ação.
     */
    fun buscarAcoesPorPilarParaSelecao(pilarId: Int): List<AcaoEstrategica> {
        val lista = mutableListOf<AcaoEstrategica>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, pilar_id, nome, descricao, criado_por, aprovado FROM Acao WHERE pilar_id = ?",
            arrayOf(pilarId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val acao = AcaoEstrategica(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    pilarId = cursor.getInt(cursor.getColumnIndexOrThrow("pilar_id")),
                    nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                    criadoPor = cursor.getInt(cursor.getColumnIndexOrThrow("criado_por")),
                    aprovado = cursor.getInt(cursor.getColumnIndexOrThrow("aprovado")) == 1
                )
                lista.add(acao)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    /**
     * Busca uma atividade específica pelo seu ID, retornando dados essenciais para exibição ou edição.
     *
     * Utilizado principalmente em fluxos de atualização de atividade.
     *
     * @param atividadeId ID da atividade.
     * @return Objeto `Atividadespinner` com informações da atividade ou `null` se não encontrada.
     */
    fun getAtividadePorId(atividadeId: Int): Atividadespinner? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, acao_id, nome, descricao, status, data_inicio, data_conclusao, criado_por, aprovado, responsavel_id FROM Atividade WHERE id = ?",
            arrayOf(atividadeId.toString())
        )

        var atividade: Atividadespinner? = null

        if (cursor.moveToFirst()) {
            val statusString = cursor.getString(cursor.getColumnIndexOrThrow("status"))

            val dataConclusao = if (cursor.isNull(cursor.getColumnIndexOrThrow("data_conclusao"))) {
                null
            } else {
                cursor.getString(cursor.getColumnIndexOrThrow("data_conclusao"))
            }

            atividade = Atividadespinner(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                acaoId = cursor.getInt(cursor.getColumnIndexOrThrow("acao_id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                status = statusString,  // usa direto string aqui
                dataInicio = cursor.getString(cursor.getColumnIndexOrThrow("data_inicio")),
                dataConclusao = dataConclusao,
                criadoPor = cursor.getInt(cursor.getColumnIndexOrThrow("criado_por")),
                aprovado = cursor.getInt(cursor.getColumnIndexOrThrow("aprovado")) == 1,
                responsavelId = if (cursor.isNull(cursor.getColumnIndexOrThrow("responsavel_id")))
                    null else cursor.getInt(cursor.getColumnIndexOrThrow("responsavel_id"))
            )
        }

        cursor.close()
        return atividade
    }


    /**
     * Retorna todas as atividades vinculadas a uma determinada ação.
     *
     * Usado em telas onde o usuário precisa selecionar atividades específicas por ação.
     *
     * @param acaoId ID da ação cujas atividades devem ser buscadas.
     * @return Lista de `Atividadespinner` com os dados essenciais para exibição.
     */
    fun buscarAtividadesPorAcaoParaSelecao(acaoId: Int): List<Atividadespinner> {
        val lista = mutableListOf<Atividadespinner>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, acao_id, nome, descricao, status, data_inicio, data_conclusao, criado_por, aprovado, responsavel_id FROM Atividade WHERE acao_id = ?",
            arrayOf(acaoId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))

                val dataConclusao = if (cursor.isNull(cursor.getColumnIndexOrThrow("data_conclusao"))) {
                    null
                } else {
                    cursor.getString(cursor.getColumnIndexOrThrow("data_conclusao"))
                }

                val atividade = Atividadespinner(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    acaoId = cursor.getInt(cursor.getColumnIndexOrThrow("acao_id")),
                    nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                    status = status,
                    dataInicio = cursor.getString(cursor.getColumnIndexOrThrow("data_inicio")),
                    dataConclusao = dataConclusao,
                    criadoPor = cursor.getInt(cursor.getColumnIndexOrThrow("criado_por")),
                    aprovado = cursor.getInt(cursor.getColumnIndexOrThrow("aprovado")) == 1,
                    responsavelId = if (cursor.isNull(cursor.getColumnIndexOrThrow("responsavel_id")))
                        null else cursor.getInt(cursor.getColumnIndexOrThrow("responsavel_id"))
                )
                lista.add(atividade)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }


    /**
     * Retorna o nome de um usuário responsável com base no ID.
     *
     * Usado para exibir o nome em interfaces onde só o ID está disponível.
     *
     * @param id ID do usuário.
     * @return Nome do usuário ou `null` se não encontrado.
     */
    fun getNomeDoResponsavel(id: Int): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT nome FROM Usuario WHERE id = ?", arrayOf(id.toString()))
        val nome = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow("nome"))
        } else {
            null
        }
        cursor.close()
        return nome
    }



    /**
     * Marca uma notificação como lida, alterando o valor do campo `lida` para 1.
     *
     * @param idNotificacao ID da notificação que deve ser atualizada.
     */
    fun marcarNotificacaoComoLida(idNotificacao: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("lida", 1)
        }
        db.update("Notificacao", values, "id = ?", arrayOf(idNotificacao.toString()))
        db.close()
    }

    /**
     * Verifica se há pilares com datas de conclusão próximas (3 ou 7 dias) e notifica os usuários.
     *
     * A função evita notificações duplicadas verificando se já existe uma com a mesma mensagem
     * e data aproximada no banco de dados.
     */
    fun verificarPilaresProximosDaConclusao() {
        val db = writableDatabase
        val cursorPilares = db.rawQuery("SELECT id, nome, data_conclusao FROM Pilar", null)

        val hoje = Calendar.getInstance()
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        while (cursorPilares.moveToNext()) {
            val nomePilar = cursorPilares.getString(1)
            val dataConclusaoStr = cursorPilares.getString(2)

            val dataConclusao: Long? = try {
                formatter.parse(dataConclusaoStr)?.time
            } catch (e: Exception) {
                null
            }

            // Pula se a data for inválida
            if (dataConclusao == null) continue

            val diasRestantes =
                ((dataConclusao - hoje.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

            if (diasRestantes == 7 || diasRestantes == 3) {
                val mensagem =
                    "O Pilar \"$nomePilar\" está a $diasRestantes dias da data de conclusão."

                // Evita notificação duplicada
                val cursorCheck = db.rawQuery(
                    """
                SELECT COUNT(*) FROM Notificacao 
                WHERE mensagem = ? AND ABS(CAST(data AS INTEGER) - ?) < 86400000
                """.trimIndent(),
                    arrayOf(mensagem, System.currentTimeMillis().toString())
                )

                cursorCheck.moveToFirst()
                val jaExiste = cursorCheck.getInt(0) > 0
                cursorCheck.close()

                if (!jaExiste) {
                    notificarTodosUsuarios(mensagem)
                }
            }
        }

        cursorPilares.close()
        db.close()
    }


    /**
     * Cadastra um novo pilar no banco de dados e dispara notificações aos usuários.
     *
     * Após o cadastro, também verifica se há pilares próximos da conclusão para atualizar alertas.
     *
     * @param numero Número sequencial do pilar.
     * @param nome Nome do pilar.
     * @param descricao Descrição opcional.
     * @param dataInicio Data de início do pilar (formato: yyyy-MM-dd).
     * @param dataConclusao Data de término do pilar (formato: yyyy-MM-dd).
     * @param criadoPorId ID do usuário que está criando o pilar.
     * @return ID do novo registro inserido ou -1 em caso de falha.
     */
    fun cadastrarPilar(
        numero: Int,
        nome: String,
        descricao: String? = null,
        dataInicio: String,
        dataConclusao: String,
        criadoPorId: Int
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("numero", numero)
            put("nome", nome)
            if (descricao != null) put("descricao", descricao) else putNull("descricao")
            put("data_inicio", dataInicio)
            put("data_conclusao", dataConclusao)
            put("criado_por", criadoPorId)
        }

        val resultado = db.insert("Pilar", null, values)

        if (resultado != -1L) {
            notificarTodosUsuarios("Novo Pilar criado: $nome")
            verificarPilaresProximosDaConclusao() //
        }

        db.close()
        return resultado
    }


    /**
     * Obtém o próximo número sequencial para um novo pilar.
     *
     * Garante que cada pilar seja numerado corretamente mesmo em múltiplas inserções.
     *
     * @return Próximo número inteiro disponível.
     */
    fun obterProximoNumeroPilar(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT MAX(numero) as max_numero FROM Pilar", null)
        var proximo = 1
        if (cursor.moveToFirst()) {
            proximo = cursor.getInt(cursor.getColumnIndexOrThrow("max_numero")) + 1
        }
        cursor.close()
        db.close()
        return proximo
    }

    /**
     * Retorna todas as ações e suas respectivas atividades para um pilar específico.
     *
     * Usado para compor visões hierárquicas ou detalhadas de um pilar.
     *
     * @param pilarId ID do pilar que será analisado.
     * @return Lista de `AcaoComAtividades`, agrupando atividades por ação.
     */
    fun buscarAcoesEAtividadesPorPilar(pilarId: Int): List<AcaoComAtividades> {
        val lista = mutableListOf<AcaoComAtividades>()
        val db = this.readableDatabase

        val queryAcao = "SELECT * FROM Acao WHERE pilar_id = ? AND aprovado = 1"
        val cursorAcao = db.rawQuery(queryAcao, arrayOf(pilarId.toString()))

        while (cursorAcao.moveToNext()) {
            val acaoId = cursorAcao.getInt(cursorAcao.getColumnIndexOrThrow("id"))
            val nomeAcao = cursorAcao.getString(cursorAcao.getColumnIndexOrThrow("nome"))
            val pilarId = cursorAcao.getInt(cursorAcao.getColumnIndexOrThrow("pilar_id"))

            val queryAtividades = "SELECT id, nome , status, aprovado, acao_id FROM Atividade WHERE acao_id = ? AND aprovado = 1"
            val cursorAtividades = db.rawQuery(queryAtividades, arrayOf(acaoId.toString()))

            val atividades = mutableListOf<Atividade>()
            while (cursorAtividades.moveToNext()) {
                val idAtividade =
                    cursorAtividades.getInt(cursorAtividades.getColumnIndexOrThrow("id"))
                val nomeAtividade =
                    cursorAtividades.getString(cursorAtividades.getColumnIndexOrThrow("nome"))
                val status =
                    cursorAtividades.getString(cursorAtividades.getColumnIndexOrThrow("status"))
                val aprovado =
                    cursorAtividades.getInt(cursorAtividades.getColumnIndexOrThrow("aprovado")) == 1
                val acaoId =
                    cursorAtividades.getInt(cursorAtividades.getColumnIndexOrThrow("acao_id"))
                atividades.add(Atividade(idAtividade, nomeAtividade, status, aprovado))
            }
            cursorAtividades.close()

            lista.add(AcaoComAtividades(Acao(id = acaoId, nome = nomeAcao, pilarId), atividades))
        }

        cursorAcao.close()
        db.close()
        return lista
    }

    /**
     * Busca o responsável vinculado a uma determinada atividade.
     *
     * Útil para exibir o nome do responsável em telas de visualização ou edição da atividade.
     *
     * @param atividadeId ID da atividade que será consultada.
     * @return Objeto `Usuario` com os dados do responsável ou `null` se não encontrado.
     */
    fun buscarResponsavelPorAtividade(atividadeId: Int): Usuario? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT u.id, u.nome, u.tipo 
            FROM Usuario u
            INNER JOIN Atividade a ON u.id = a.responsavel_id
            WHERE a.id = ?
        """.trimIndent(), arrayOf(atividadeId.toString())
        )

        return if (cursor.moveToFirst()) {
            Usuario(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo")),
            ).also { cursor.close(); db.close() }
        } else {
            null.also { cursor.close(); db.close() }
        }
    }

    /**
     * Retorna o nome e as datas de início e conclusão de um pilar específico.
     *
     * Normalmente usado antes de editar ou validar alterações nas datas de um pilar.
     *
     * @param id ID do pilar.
     * @return `Triple` com nome, data de início e data de conclusão ou `null` se não encontrado.
     */
    fun getDatasPilarById(id: Int): Triple<String, String, String>? {
        val db = getDatabase()
        val cursor = db.rawQuery(
            "SELECT nome, data_inicio, data_conclusao FROM Pilar WHERE id = ?",
            arrayOf(id.toString())
        )

        var resultado: Triple<String, String, String>? = null
        if (cursor.moveToFirst()) {
            val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
            val dataInicio = cursor.getString(cursor.getColumnIndexOrThrow("data_inicio"))
            val dataConclusao = cursor.getString(cursor.getColumnIndexOrThrow("data_conclusao"))
            resultado = Triple(nome, dataInicio, dataConclusao)
        }

        cursor.close()
        return resultado
    }

    /**
     * Atualiza o nome e datas de um pilar existente.
     *
     * Após a atualização, verifica se houve mudanças nas datas e dispara notificações informando a alteração.
     *
     * @param id ID do pilar que será atualizado.
     * @param novoNome Novo nome do pilar.
     * @param novaDataInicio Nova data de início no formato `yyyy-MM-dd`.
     * @param novaDataConclusao Nova data de conclusão no formato `yyyy-MM-dd`.
     */
    fun atualizarPilar(
        id: Int,
        novoNome: String,
        novaDataInicio: String,
        novaDataConclusao: String
    ) {
        val db = getDatabase()

        // Pega os dados antigos (nome, data_inicio, data_conclusao)
        val dadosAntigos = getDatasPilarById(id)

        val values = ContentValues().apply {
            put("nome", novoNome)
            put("data_inicio", novaDataInicio)
            put("data_conclusao", novaDataConclusao)
        }

        db.update("Pilar", values, "id = ?", arrayOf(id.toString()))

        // Compara datas e envia notificação
        if (dadosAntigos != null) {
            val (nomeAntigo, dataInicioAntiga, dataConclusaoAntiga) = dadosAntigos
            val alteracoes = mutableListOf<String>()

            if (dataInicioAntiga != novaDataInicio) {
                alteracoes.add("data de início")
            }
            if (dataConclusaoAntiga != novaDataConclusao) {
                alteracoes.add("data de conclusão")
            }

            if (alteracoes.isNotEmpty()) {
                val msg =
                    "O Pilar \"$nomeAntigo\" teve alteração na ${alteracoes.joinToString(" e ")}."
                notificarTodosUsuarios(msg)
            }
        }
    }


    /**
     * Exclui um pilar do banco de dados com base no ID.
     *
     * Também dispara uma notificação informando que o pilar foi excluído, se a operação for bem-sucedida.
     *
     * @param id ID do pilar a ser excluído.
     * @return `true` se o pilar foi excluído com sucesso, `false` caso contrário.
     */
    fun excluirPilar(id: Int): Boolean {
        val db = writableDatabase

        // Buscar nome do Pilar antes de excluir
        val cursor = db.rawQuery("SELECT nome FROM Pilar WHERE id = ?", arrayOf(id.toString()))
        var nomePilar: String? = null
        if (cursor.moveToFirst()) {
            nomePilar = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
        }
        cursor.close()

        // Executar a exclusão
        val rowsDeleted = db.delete("Pilar", "id = ?", arrayOf(id.toString()))

        // Se excluído com sucesso, enviar notificação
        if (rowsDeleted > 0 && nomePilar != null) {
            notificarTodosUsuarios("O Pilar \"$nomePilar\" foi excluído da lista de Pilares.")
        }

        db.close()
        return rowsDeleted > 0
    }

    /**
     * Atualiza o nome de uma ação existente no banco de dados.
     *
     * Após atualizar, verifica se há alterações e dispara notificação informando a mudança.
     *
     * @param id ID da ação a ser atualizada.
     * @param novoNome Novo nome da ação.
     */
    fun atualizarAcao(
        id: Int,
        novoNome: String,
    ) {
        val db = getDatabase()

        val dadosAntigos = buscarAcaoPorId(id)

        val values = ContentValues().apply {
            put("nome", novoNome)
        }

        db.update("Acao", values, "id = ?", arrayOf(id.toString()))

        if (dadosAntigos != null) {
            val (nomeAntigo) = dadosAntigos
            val alteracoes = mutableListOf<String>()

            if (alteracoes.isNotEmpty()) {
                val msg =
                    "A Ação \"$nomeAntigo\" teve alteração na ${alteracoes.joinToString(" e ")}."
                notificarTodosUsuarios(msg)
            }
        }
    }

    /**
     * Exclui uma ação do banco de dados.
     *
     * Também notifica todos os usuários em caso de exclusão bem-sucedida.
     *
     * @param id ID da ação a ser excluída.
     * @return `true` se a exclusão foi realizada com sucesso, `false` caso contrário.
     */
    fun excluirAcao(id: Int): Boolean {
        val db = writableDatabase

        val cursor = db.rawQuery("SELECT nome FROM Acao WHERE id = ?", arrayOf(id.toString()))
        var nomeAcao: String? = null
        if (cursor.moveToFirst()) {
            nomeAcao = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
        }
        cursor.close()

        val rowsDeleted = db.delete("Acao", "id = ?", arrayOf(id.toString()))

        if (rowsDeleted > 0 && nomeAcao != null) {
            notificarTodosUsuarios("A Ação \"$nomeAcao\" foi excluída.")
        }

        db.close()
        return rowsDeleted > 0
    }


    /**
     * DTO representando uma ação estratégica com ID, nome e vínculo com o pilar.
     */
    data class AcaoDTO(
        val id: Int, val nome: String, val pilar_id: Int, val criadoPorId: Int
    )


    /**
     * Busca os dados de uma ação específica pelo ID.
     *
     * @param id ID da ação.
     * @return Objeto `AcaoDTO` contendo os dados da ação ou `null` se não encontrada.
     */
    fun buscarAcaoPorId(id: Int?): AcaoDTO? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Acao WHERE id = ?", arrayOf(id.toString()))

        var acao: AcaoDTO? = null

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
            val pilarId = cursor.getInt(cursor.getColumnIndexOrThrow("pilar_id"))
            val criadoPor = cursor.getInt(cursor.getColumnIndexOrThrow("criado_por"))
            acao = AcaoDTO(id, nome, pilarId,  criadoPor)
        }

        cursor.close()
        db.close()
        return acao
    }

    /**
     * DTO contendo os dados completos de um pilar.
     */
    data class PilarDTO(
        val id: Int, val numero: Int, val nome: String, val descricao: String,
        val dataInicio: String, val dataConclusao: String, val criadoPor: Int
    )

    /**
     * Retorna os pilares que possuem atividades atribuídas ao usuário.
     *
     * Usado para filtrar pilares relevantes ao perfil do usuário logado.
     *
     * @param usuarioId ID do usuário.
     * @return Lista de `PilarDTO` contendo dados dos pilares com atividades do usuário.
     */
    fun Cursor.getStringOrNull(columnIndex: Int): String? {
        return if (isNull(columnIndex)) null else getString(columnIndex)
    }

    fun getPilaresComAtividadesDoUsuario(usuarioId: Int): List<PilarDTO> {
        val pilares = mutableListOf<PilarDTO>()
        val db = readableDatabase

        val query = """
        SELECT DISTINCT p.id, p.numero, p.nome, p.descricao, p.data_inicio, p.data_conclusao, p.criado_por
        FROM Pilar p
        JOIN Acao a ON a.pilar_id = p.id
        JOIN Atividade at ON at.acao_id = a.id
        WHERE at.responsavel_id = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(usuarioId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val pilar = PilarDTO(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    numero = cursor.getInt(cursor.getColumnIndexOrThrow("numero")),
                    nome = cursor.getStringOrNull(cursor.getColumnIndexOrThrow("nome")) ?: "Sem nome",
                    descricao = cursor.getStringOrNull(cursor.getColumnIndexOrThrow("descricao")) ?: "",
                    dataInicio = cursor.getStringOrNull(cursor.getColumnIndexOrThrow("data_inicio")) ?: "",
                    dataConclusao = cursor.getStringOrNull(cursor.getColumnIndexOrThrow("data_conclusao")) ?: "",
                    criadoPor = cursor.getInt(cursor.getColumnIndexOrThrow("criado_por"))
                )
                pilares.add(pilar)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return pilares
    }


    /**
     * DTO com dados mínimos de um pilar (usado em contextos mais simples).
     */
    data class PilarDTObyId(
        val id: Int, val nome: String
    )

    /**
     * Busca o nome de um pilar com base no ID.
     *
     * Usado em situações onde apenas o nome é necessário (ex: exibição).
     *
     * @param id ID do pilar.
     * @return Objeto `PilarDTObyId` com nome e ID, ou `null` se não encontrado.
     */
    fun buscarPilarPorId(id: Int): PilarDTObyId? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Pilar WHERE id = ?", arrayOf(id.toString()))

        var pilar: PilarDTObyId? = null

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
            pilar = PilarDTObyId(id, nome)
        }

        cursor.close()
        db.close()
        return pilar
    }


    /**
     * Retorna todas as ações e suas atividades atribuídas a um usuário dentro de um pilar.
     *
     * @param pilarId ID do pilar.
     * @param usuarioId ID do usuário.
     * @return Lista de `AcaoComAtividades` com ações e suas respectivas atividades do usuário.
     */
    fun buscarAcoesEAtividadesDoUsuarioPorPilar(
        pilarId: Int,
        usuarioId: Int
    ): List<AcaoComAtividades> {
        val resultado = mutableListOf<AcaoComAtividades>()
        val db = readableDatabase

        val acoesQuery = """
        SELECT DISTINCT a.id, a.nome, a.pilar_id
        FROM Acao a
        JOIN Atividade at ON a.id = at.acao_id
        WHERE a.pilar_id = ? AND at.responsavel_id = ? AND a.aprovado = 1
    """.trimIndent()

        val cursorAcoes = db.rawQuery(acoesQuery, arrayOf(pilarId.toString(), usuarioId.toString()))

        if (cursorAcoes.moveToFirst()) {
            do {
                val acaoId = cursorAcoes.getInt(0)
                val acaoNome = cursorAcoes.getString(1)
                val pilarId = cursorAcoes.getInt(2)
                val acao = Acao(acaoId, acaoNome, pilarId)

                val atividades = mutableListOf<Atividade>()

                val atividadesQuery = """
                SELECT id, nome, status, aprovado
                FROM Atividade
                WHERE acao_id = ? AND responsavel_id = ? AND aprovado = 1
            """.trimIndent()

                val cursorAtividades =
                    db.rawQuery(atividadesQuery, arrayOf(acaoId.toString(), usuarioId.toString()))

                if (cursorAtividades.moveToFirst()) {
                    do {
                        val atividadeId = cursorAtividades.getInt(0)
                        val atividadeNome = cursorAtividades.getString(1)
                        val status = cursorAtividades.getString(cursorAtividades.getColumnIndexOrThrow("status"))
                        val aprovado = true

                        atividades.add(Atividade(atividadeId, atividadeNome, status, aprovado))
                    } while (cursorAtividades.moveToNext())
                }

                cursorAtividades.close()
                resultado.add(AcaoComAtividades(acao, atividades))
            } while (cursorAcoes.moveToNext())
        }

        cursorAcoes.close()
        db.close()
        return resultado
    }

    /**
     * Atualiza os dados de uma atividade e notifica o usuário responsável.
     *
     * Notificações são disparadas em caso de mudança de responsável ou alterações relevantes.
     *
     * @return A nova versão da atividade após a atualização ou `null` se não encontrada.
     */
    fun atualizarAtividade(
        id: Int,
        novoNome: String,
        novoResponsavel: Int,
        novaDataInicio: String,
        novaDataConclusao: String,
        novoStatus: String
    ): AtividadeEdit? {
        val db = this.writableDatabase

        // Obter dados antigos
        val atividadeAntiga = buscarAtividadePorId(id) ?: return null

        val values = ContentValues().apply {
            put("nome", novoNome)
            put("responsavel_id", novoResponsavel)
            put("data_inicio", novaDataInicio)
            put("data_conclusao", novaDataConclusao)
            put("status", novoStatus)
        }

        db.update("Atividade", values, "id = ?", arrayOf(id.toString()))

        val atividadeAtualizada = buscarAtividadePorId(id)

        // 🚨 Notificações
        if (atividadeAtualizada != null) {
            val mudouResponsavel = atividadeAntiga.responsavel_id != novoResponsavel
            val semResponsavelAntes = atividadeAntiga.responsavel_id == 0 || atividadeAntiga.responsavel_id == -1

            val dadosAlterados = atividadeAntiga.nome != novoNome ||
                    atividadeAntiga.data_inicio != novaDataInicio ||
                    atividadeAntiga.data_conclusao != novaDataConclusao

            // ✅ Notificar novo responsável se foi designado agora
            if (mudouResponsavel && semResponsavelAntes && novoResponsavel > 0) {
                notificarUsuario(
                    usuarioId = novoResponsavel,
                    mensagem = "Você foi designado como responsável pela atividade '${novoNome}'.",
                    atividadeId = id,
                    tipo = TipoNotificacao.IMPORTANTE
                )
            }

            // ✅ Notificar usuário responsável atual se houve alteração importante
            if (dadosAlterados && novoResponsavel > 0) {
                notificarUsuario(
                    usuarioId = novoResponsavel,
                    mensagem = "A atividade '${novoNome}', da qual você é responsável, foi atualizada.",
                    atividadeId = id,
                    tipo = TipoNotificacao.ALERTA
                )
            }
        }

        return atividadeAtualizada
    }

    /**
     * Envia uma notificação para um usuário específico.
     *
     * Pode incluir referência a uma atividade e tipo da notificação (alerta, importante, etc.).
     */
    fun notificarUsuario(
        usuarioId: Int,
        mensagem: String,
        atividadeId: Int? = null,
        tipo: TipoNotificacao = TipoNotificacao.GERAL
    ) {
        val db = this.writableDatabase
        val dataAtual = System.currentTimeMillis()

        val values = ContentValues().apply {
            put("usuario_id", usuarioId)
            put("mensagem", mensagem)
            put("data", dataAtual)
            put("lida", 0)
            put("tipo_notificacao", tipo.name)
            if (atividadeId != null) {
                put("atividade_id", atividadeId)
            }
        }

        db.insert("Notificacao", null, values)


        Log.d("NOTIFICAÇÃO", "Notificação enviada para usuário $usuarioId: $mensagem")
    }


    /**
     * Retorna o nome de uma ação a partir de seu ID.
     *
     * @param acaoId ID da ação.
     * @return Nome da ação, ou "Ação" se não encontrada.
     */
    fun getNomeAcaoById(acaoId: Int): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT nome FROM Acao WHERE id = ?", arrayOf(acaoId.toString()))
        var nome = "Ação" // valor padrão

        if (cursor.moveToFirst()) {
            nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
        }

        cursor.close()
        return nome
    }

    /**
     * Busca todas as atividades associadas a uma ação específica.
     *
     * Retorna dados detalhados como responsável e status para cada atividade.
     */
    fun getAtividadesByAcaoId(acaoId: Int): List<AtividadeDetalhe> {
        val lista = mutableListOf<AtividadeDetalhe>()
        val db = readableDatabase
        val query = """
        SELECT A.nome, U.nome, A.data_inicio, A.data_conclusao, A.status
        FROM Atividade A
        LEFT JOIN Usuario U ON A.responsavel_id = U.id
        WHERE A.acao_id = ?
    """
        val cursor = db.rawQuery(query, arrayOf(acaoId.toString()))
        while (cursor.moveToNext()) {
            val nome = cursor.getString(0)
            val responsavel = cursor.getString(1) ?: "Sem responsável"
            val dataInicio = cursor.getString(2)
            val dataConclusao = cursor.getString(3)
            val status = cursor.getString(4)
            lista.add(AtividadeDetalhe(nome, responsavel, dataInicio, dataConclusao, status))
        }
        cursor.close()
        return lista
    }

    /**
     * Retorna os dados de uma atividade pelo seu ID.
     *
     * @param id ID da atividade.
     * @return Objeto `AtividadeEdit` ou `null` se não encontrado.
     */
    fun buscarAtividadePorId(id: Int?): AtividadeEdit? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Atividade WHERE id = ?", arrayOf(id.toString()))

        var atividade: AtividadeEdit? = null

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
            val responsavelId = cursor.getInt(cursor.getColumnIndexOrThrow("responsavel_id"))
            val dataInicio = cursor.getString(cursor.getColumnIndexOrThrow("data_inicio"))
            val dataConclusao = cursor.getString(cursor.getColumnIndexOrThrow("data_conclusao"))
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            val criadoPor = cursor.getInt(cursor.getColumnIndexOrThrow("criado_por"))
            val acaoId = cursor.getInt(cursor.getColumnIndexOrThrow("acao_id"))
            atividade = AtividadeEdit(id, nome, status, dataInicio, dataConclusao, responsavelId, criadoPor, acaoId)
        }

        cursor.close()
        return atividade
    }

    /**
     * Busca todas as atividades com status "Em atraso".
     *
     * @return Lista de `Triple` com ID, nome e status da atividade.
     */
    fun buscarAtividadePorStatus(): List<Triple<Int, String, String>> {
        val lista = mutableListOf<Triple<Int, String, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, nome, status FROM Atividade WHERE status = ?",
            arrayOf("Em atraso")
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
                lista.add(Triple(id, nome, status))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return lista
    }

    /**
     * DTO para representar aprovação de uma atividade.
     */
    data class AtividadeDTObyId(
        val id: Int, val aprovado: Boolean
    )

    /**
     * Verifica se uma atividade foi aprovada.
     *
     * @param id ID da atividade.
     * @return Objeto `AtividadeDTObyId` ou `null` se não encontrada.
     */
    fun buscarAtividadeAprovada(id: Int?): AtividadeDTObyId? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Atividade WHERE id = ?", arrayOf(id.toString()))

        var atividade: AtividadeDTObyId? = null

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val aprovado = cursor.getInt(cursor.getColumnIndexOrThrow("aprovado")) == 1
            atividade = AtividadeDTObyId(id, aprovado)
        }

        cursor.close()
        db.close()
        return atividade
    }


    /**
     * DTO para representar aprovação de uma atividade.
     */
    data class AcaoDTObyId(
        val id: Int, val aprovado: Boolean
    )

    /**
     * Verifica se uma ação foi aprovada.
     *
     * @param id ID da ação.
     * @return Objeto `AcaoDTObyId` ou `null` se não encontrada.
     */
    fun buscarAcaoAprovada(id: Int?): AcaoDTObyId? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Acao WHERE id = ?", arrayOf(id.toString()))

        var acao: AcaoDTObyId? = null

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val aprovado = cursor.getInt(cursor.getColumnIndexOrThrow("aprovado")) == 1
            acao = AcaoDTObyId(id, aprovado)
        }

        cursor.close()
        db.close()
        return acao
    }

    /**
     * Insere uma nova atividade no banco de dados, com possibilidade de notificar o responsável.
     *
     * Caso um responsável seja atribuído, envia uma notificação com o nome da ação e pilar.
     *
     * @return ID da atividade inserida ou -1 em caso de falha.
     */
    fun inserirAtividade(
        acaoId: Int,
        nome: String,
        descricao: String,
        status: String,
        dataInicio: String,
        dataConclusao: String?,
        criadoPor: Int,
        responsavelId: Int?,
        aprovado: Boolean
    ): Long {

        val db = writableDatabase

        val cursorAcao = db.rawQuery(
            "SELECT nome, pilar_id FROM Acao WHERE id = ?",
            arrayOf(acaoId.toString())
        )

        var nomeAcao = ""
        var pilarId = -1

        if (cursorAcao.moveToFirst()) {
            nomeAcao = cursorAcao.getString(cursorAcao.getColumnIndexOrThrow("nome"))
            pilarId = cursorAcao.getInt(cursorAcao.getColumnIndexOrThrow("pilar_id"))
        }
        cursorAcao.close()

        var nomePilar = ""
        if (pilarId != -1) {
            val cursorPilar = db.rawQuery(
                "SELECT nome FROM Pilar WHERE id = ?",
                arrayOf(pilarId.toString())
            )
            if (cursorPilar.moveToFirst()) {
                nomePilar = cursorPilar.getString(cursorPilar.getColumnIndexOrThrow("nome"))
            }
            cursorPilar.close()
        }

        val values = ContentValues().apply {
            put("acao_id", acaoId)
            put("nome", nome)
            put("descricao", descricao)
            put("status", status)
            put("data_inicio", dataInicio)
            put("data_conclusao", dataConclusao)
            put("criado_por", criadoPor)
            put("aprovado", aprovado)
            if (responsavelId != null) {
                put("responsavel_id", responsavelId)
            }
        }

        val atividadeId = db.insert("Atividade", null, values)

        // Gerar notificação se tiver responsável
        if (responsavelId != null && atividadeId != -1L) {
            val mensagem = "Nova atividade \"$nome\" atribuída a você, na ação \"$nomeAcao\" do pilar \"$nomePilar\"."
            val dataAtual = System.currentTimeMillis().toString()

            val notificacao = ContentValues().apply {
                put("usuario_id", responsavelId)
                put("mensagem", mensagem)
                put("data", dataAtual)
                put("lida", 0)
                put("atividade_id", atividadeId.toInt())
            }

            db.insert("Notificacao", null, notificacao)
        }

        db.close()

        return atividadeId  // Retorna o ID inserido
    }


    /**
     * Verifica atividades próximas da data de conclusão (3 ou 7 dias) e envia notificação ao responsável.
     *
     * Evita envio duplicado de notificação no mesmo dia.
     */
    fun verificarAtividadesProximasDaConclusao() {
        val db = writableDatabase
        val cursor = db.rawQuery(
            """
        SELECT a.id, a.nome, a.data_conclusao, u.id as responsavel_id, 
               ac.nome as nome_acao, p.numero as numero_pilar, p.nome as nome_pilar
        FROM Atividade a
        JOIN Acao ac ON a.acao_id = ac.id
        JOIN Pilar p ON ac.pilar_id = p.id
        JOIN Usuario u ON a.responsavel_id = u.id
        WHERE a.data_conclusao IS NOT NULL AND a.responsavel_id IS NOT NULL
        """.trimIndent(), null
        )

        val hoje = Calendar.getInstance()
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        while (cursor.moveToNext()) {
            val atividadeId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val nomeAtividade = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
            val dataConclusaoStr = cursor.getString(cursor.getColumnIndexOrThrow("data_conclusao"))
            val responsavelId = cursor.getInt(cursor.getColumnIndexOrThrow("responsavel_id"))
            val numeroPilar = cursor.getInt(cursor.getColumnIndexOrThrow("numero_pilar"))
            val nomePilar = cursor.getString(cursor.getColumnIndexOrThrow("nome_pilar"))

            val dataConclusao: Long? = try {
                formatter.parse(dataConclusaoStr)?.time
            } catch (e: Exception) {
                null
            }

            if (dataConclusao == null) continue

            val diasRestantes = ((dataConclusao - hoje.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

            if (diasRestantes == 3 || diasRestantes == 7) {
                val mensagem = "Sua atividade \"$nomeAtividade\" do pilar $numeroPilar - $nomePilar está a $diasRestantes dias da conclusão."

                // Verificar se já existe uma notificação parecida hoje para não duplicar
                val cursorCheck = db.rawQuery(
                    """
                SELECT COUNT(*) FROM Notificacao 
                WHERE usuario_id = ? AND atividade_id = ? AND mensagem = ? AND ABS(CAST(data AS INTEGER) - ?) < 86400000
                """.trimIndent(),
                    arrayOf(
                        responsavelId.toString(),
                        atividadeId.toString(),
                        mensagem,
                        System.currentTimeMillis().toString()
                    )
                )

                cursorCheck.moveToFirst()
                val jaExiste = cursorCheck.getInt(0) > 0
                cursorCheck.close()

                if (!jaExiste) {
                    val values = ContentValues().apply {
                        put("usuario_id", responsavelId)
                        put("mensagem", mensagem)
                        put("data", System.currentTimeMillis().toString())
                        put("lida", 0)
                        put("atividade_id", atividadeId)
                    }

                    db.insert("Notificacao", null, values)
                }
            }
        }

        cursor.close()
        db.close()
    }


    /**
     * Atualiza o status de uma atividade.
     *
     * @param idAtividade ID da atividade.
     * @param novoStatus Novo status a ser atribuído (ex: "Finalizada", "Em andamento").
     */
    fun atualizarStatus(idAtividade: Int, novoStatus: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("status", novoStatus)
        }
        db.update("Atividade", values, "id = ?", arrayOf(idAtividade.toString()))
        db.close()
    }

    /**
     * Calcula o progresso percentual de um pilar com base nas atividades concluídas.
     *
     * @param pilarId ID do pilar.
     * @return Progresso percentual (0 a 100).
     */
    fun calcularProgressoPilar(pilarId: Int): Int {
        val db = this.readableDatabase

        val queryTotal = """
        SELECT COUNT(*) FROM Atividade 
        INNER JOIN Acao ON Atividade.acao_id = Acao.id
        WHERE Acao.pilar_id = ?
    """.trimIndent()

        val queryConcluidas = """
        SELECT COUNT(*) FROM Atividade 
        INNER JOIN Acao ON Atividade.acao_id = Acao.id
        WHERE Acao.pilar_id = ? AND Atividade.status = 'Finalizada'
    """.trimIndent()

        val cursorTotal = db.rawQuery(queryTotal, arrayOf(pilarId.toString()))
        val cursorConcluidas = db.rawQuery(queryConcluidas, arrayOf(pilarId.toString()))

        var total = 0
        var concluidas = 0

        if (cursorTotal.moveToFirst()) {
            total = cursorTotal.getInt(0)
        }
        if (cursorConcluidas.moveToFirst()) {
            concluidas = cursorConcluidas.getInt(0)
        }

        cursorTotal.close()
        cursorConcluidas.close()
        db.close()

        if (total == 0) return 0

        val progresso = (concluidas * 100) / total
        return progresso
    }

    /**
     * Cria notificações para todos os usuários do tipo "Coordenador".
     *
     * Cada coordenador recebe uma notificação com a mensagem informada, associada a uma atividade (opcional),
     * e um tipo de notificação.
     *
     * @param mensagem Texto da notificação a ser enviada.
     * @param atividadeId Id da atividade relacionada à notificação (pode ser null).
     * @param tipoNotificacao Tipo da notificação, utilizado para categorizar a notificação.
     */
    fun criarNotificacaoParaCoordenador(mensagem: String, atividadeId: Int? = null, acaoId: Int? = null, tipoNotificacao: TipoNotificacao) {
        val db = writableDatabase
        val dataAtual = System.currentTimeMillis().toString()

        val cursor = db.rawQuery("SELECT id FROM Usuario WHERE tipo = 'Coordenador'", null)

        if (cursor.moveToFirst()) {
            do {
                val coordenadorId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val values = ContentValues().apply {
                    put("usuario_id", coordenadorId)
                    put("mensagem", mensagem)
                    put("data", dataAtual)
                    put("lida", 0)
                    if (atividadeId != null) put("atividade_id", atividadeId)
                    if (acaoId != null) put("acao_id", acaoId)
                    put("tipo_notificacao", tipoNotificacao.toString())
                }
                db.insert("Notificacao", null, values)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
    }


    /**
     * Marca uma atividade como aprovada no banco de dados.
     *
     * @param id Identificador da atividade a ser aprovada.
     */
    fun aprovarAtividade(id: Int?) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("aprovado", 1)
        }
        db.update("Atividade", values, "id = ?", arrayOf(id.toString()))
    }

    /**
     * Marca uma acao como aprovada no banco de dados.
     *
     * @param id Identificador da atividade a ser aprovada.
     */
    fun aprovarAcao(id: Int?) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("aprovado", 1)
        }
        db.update("Acao", values, "id = ?", arrayOf(id.toString()))
    }

    /**
     * Busca um usuário pelo seu id no banco de dados.
     *
     * @param id Identificador do usuário.
     * @return Um objeto [Usuario] preenchido com os dados do usuário, ou null se não encontrado.
     */
    fun obterUsuario(id: Int): Usuario? {
        val db = this.readableDatabase

        val cursor = db.rawQuery("SELECT * FROM Usuario WHERE id = ?", arrayOf(id.toString()))

        var usuario: Usuario? = null

        if (cursor.moveToFirst()) {
            usuario = Usuario(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
            )
        }

        cursor.close()
        db.close()
        return usuario
    }

    /**
     * Exclui uma atividade do banco de dados e notifica todos os usuários sobre a exclusão.
     *
     * Se a exclusão for bem-sucedida, envia uma notificação para todos os usuários informando qual atividade foi excluída.
     *
     * @param id Identificador da atividade a ser excluída.
     * @return `true` se a exclusão foi realizada com sucesso, `false` caso contrário.
     */
    fun excluirAtividade(id: Int): Boolean {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT nome FROM Atividade WHERE id = ?", arrayOf(id.toString()))
        var nomeAtividade: String? = null
        if (cursor.moveToFirst()) {
            nomeAtividade = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
        }
        cursor.close()

        val rowsDeleted = db.delete("Atividade", "id = ?", arrayOf(id.toString()))

        if (rowsDeleted > 0 && nomeAtividade != null) {
            notificarTodosUsuarios("A Atividade \"$nomeAtividade\" foi excluída.")
        }

        db.close()
        return rowsDeleted > 0
    }

    /**
     * Insere uma nova ação associada a um pilar no banco de dados.
     *
     * A ação é criada com status inicial de não aprovada (aprovado = 0).
     *
     * @param pilarId Identificador do pilar ao qual a ação pertence.
     * @param nome Nome da ação.
     * @param descricao Descrição detalhada da ação.
     * @param criadoPor Id do usuário que criou a ação.
     * @return `true` se a inserção foi bem-sucedida, `false` caso contrário.
     */
    fun inserirAcao(pilarId: Int, nome: String, descricao: String, criadoPor: Int, aprovado: Boolean): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("pilar_id", pilarId)
            put("nome", nome)
            put("descricao", descricao)
            put("criado_por", criadoPor)
            put("aprovado", aprovado)
        }
        val id = db.insert("Acao", null, values)
        db.close()
        return id
    }

    /**
     * Verifica se uma notificação semelhante já existe para evitar duplicidade.
     *
     * Considera notificações do mesmo usuário, associadas à mesma atividade, com a mesma mensagem,
     * criadas no intervalo de 24 horas.
     *
     * @param db Instância do banco de dados SQLite para consulta.
     * @param usuarioId Id do usuário destinatário da notificação.
     * @param atividadeId Id da atividade relacionada à notificação.
     * @param mensagem Mensagem da notificação.
     * @param timestamp Timestamp da notificação para comparação.
     * @return `true` se uma notificação similar já existe, `false` caso contrário.
     */
    fun notificacaoJaExiste(
        db: SQLiteDatabase,
        usuarioId: Int,
        atividadeId: Int,
        mensagem: String,
        timestamp: Long
    ): Boolean {
        val cursor = db.rawQuery(
            """
        SELECT COUNT(*) FROM Notificacao 
        WHERE usuario_id = ? AND atividade_id = ? AND mensagem = ? AND ABS(CAST(data AS INTEGER) - ?) < 86400000
        """.trimIndent(),
            arrayOf(usuarioId.toString(), atividadeId.toString(), mensagem, timestamp.toString())
        )
        cursor.moveToFirst()
        val exists = cursor.getInt(0) > 0
        cursor.close()
        return exists
    }

    /**
     * Verifica no banco de dados as atividades que estão atrasadas em relação à data de conclusão prevista.
     *
     * Atualiza o status das atividades para "Em atraso" quando aplicável e gera notificações para os responsáveis
     * e coordenadores, evitando notificações duplicadas.
     *
     * O cálculo de atraso é feito pela diferença entre a data atual e a data de conclusão prevista.
     */
    fun verificarAtividadesAtrasadas() {
        val db = writableDatabase
        val cursor = db.rawQuery(
            """
        SELECT a.id, a.nome, a.data_conclusao, a.status, u.id as responsavel_id, 
               ac.nome as nome_acao, p.numero as numero_pilar, p.nome as nome_pilar
        FROM Atividade a
        JOIN Acao ac ON a.acao_id = ac.id
        JOIN Pilar p ON ac.pilar_id = p.id
        JOIN Usuario u ON a.responsavel_id = u.id
        WHERE a.data_conclusao IS NOT NULL AND a.responsavel_id IS NOT NULL
        """.trimIndent(), null
        )

        val hoje = Calendar.getInstance()
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        while (cursor.moveToNext()) {
            val atividadeId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val nomeAtividade = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
            val dataConclusaoStr = cursor.getString(cursor.getColumnIndexOrThrow("data_conclusao"))
            val responsavelId = cursor.getInt(cursor.getColumnIndexOrThrow("responsavel_id"))
            val numeroPilar = cursor.getInt(cursor.getColumnIndexOrThrow("numero_pilar"))
            val nomePilar = cursor.getString(cursor.getColumnIndexOrThrow("nome_pilar"))
            val statusAtual = cursor.getString(cursor.getColumnIndexOrThrow("status"))

            val dataConclusao: Long? = try {
                formatter.parse(dataConclusaoStr)?.time
            } catch (e: Exception) {
                null
            }

            if (dataConclusao == null) continue

            val diasAtraso = ((hoje.timeInMillis - dataConclusao) / (1000 * 60 * 60 * 24)).toInt()

            if (diasAtraso > 0) {
                if (statusAtual != "Em atraso") {
                    val updateValues = ContentValues().apply {
                        put("status", "Em atraso")
                    }
                    db.update("Atividade", updateValues, "id = ?", arrayOf(atividadeId.toString()))
                }

                val mensagem = "A atividade \"$nomeAtividade\" do pilar $numeroPilar - $nomePilar está atrasada há $diasAtraso dias."
                val dataAtual = System.currentTimeMillis()

                if (!notificacaoJaExiste(db, responsavelId, atividadeId, mensagem, dataAtual)) {
                    val values = ContentValues().apply {
                        put("usuario_id", responsavelId)
                        put("mensagem", mensagem)
                        put("data", dataAtual)
                        put("lida", 0)
                        put("atividade_id", atividadeId)
                        put("tipo_notificacao", TipoNotificacao.ALERTA.toString())
                    }
                    db.insert("Notificacao", null, values)
                }

                val cursorCoord = db.rawQuery("SELECT id FROM Usuario WHERE tipo = 'Coordenador'", null)
                if (cursorCoord.moveToFirst()) {
                    do {
                        val coordenadorId = cursorCoord.getInt(cursorCoord.getColumnIndexOrThrow("id"))
                        if (coordenadorId != responsavelId &&
                            !notificacaoJaExiste(db, coordenadorId, atividadeId, mensagem, dataAtual)
                        ) {
                            val values = ContentValues().apply {
                                put("usuario_id", coordenadorId)
                                put("mensagem", mensagem)
                                put("data", dataAtual)
                                put("lida", 0)
                                put("atividade_id", atividadeId)
                                put("tipo_notificacao", TipoNotificacao.ALERTA.toString())
                            }
                            db.insert("Notificacao", null, values)
                        }
                    } while (cursorCoord.moveToNext())
                }
                cursorCoord.close()
            }
        }

        cursor.close()
        db.close()
    }

}
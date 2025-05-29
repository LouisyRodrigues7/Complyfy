package com.example.senacplanner

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.senacplanner.Acoes.Type.Acao
import com.example.senacplanner.Acoes.Type.AcaoComAtividades
import com.example.senacplanner.Acoes.Type.Atividade
import com.example.senacplanner.Acoes.Type.PilarType
import com.example.senacplanner.Acoes.Type.Usuario
import com.example.senacplanner.adapter.TipoNotificacao
import com.example.senacplanner.editarAtividade.AtividadeEdit
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import java.util.Locale
import android.util.Log
import com.example.senacplanner.model.AtividadeDetalhe
import com.example.senacplanner.model.PilarItem
import com.example.senacplanner.model.AcaoComProgresso
import com.example.senacplanner.model.PilarComProgresso

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "novobanco.db"
        private const val DB_VERSION = 1
    }

    private val dbPath: String = context.getDatabasePath(DB_NAME).path
    private var myDatabase: SQLiteDatabase? = null

    init {
        createDatabase()
    }

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

    override fun onCreate(db: SQLiteDatabase?) {}
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    fun openDatabase() {
        myDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
    }

    override fun close() {
        myDatabase?.close()
        super.close()
    }

    fun getDatabase(): SQLiteDatabase {
        if (myDatabase == null || !myDatabase!!.isOpen) {
            openDatabase()
        }
        return myDatabase!!
    }

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

    fun getAcoesComProgressoDoPilar(pilarId: Int): List<AcaoComProgresso> {
        val lista = mutableListOf<AcaoComProgresso>()
        val db = readableDatabase

        val cursor = db.rawQuery("""
        SELECT a.nome,
               COUNT(at.id) as total,
               SUM(CASE WHEN at.status = 'Finalizada' THEN 1 ELSE 0 END) as concluidas
        FROM Acao a
        LEFT JOIN Atividade at ON a.id = at.acao_id
        WHERE a.pilar_id = ?
        GROUP BY a.nome
    """, arrayOf(pilarId.toString()))

        if (cursor.moveToFirst()) {
            do {
                lista.add(
                    AcaoComProgresso(
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

    // 🔹 Buscar ações de um pilar específico

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

    fun marcarNotificacaoComoLida(idNotificacao: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("lida", 1)
        }
        db.update("Notificacao", values, "id = ?", arrayOf(idNotificacao.toString()))
        db.close()
    }

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

    fun buscarAcoesEAtividadesPorPilar(pilarId: Int): List<AcaoComAtividades> {
        val lista = mutableListOf<AcaoComAtividades>()
        val db = this.readableDatabase

        val queryAcao = "SELECT * FROM Acao WHERE pilar_id = ?"
        val cursorAcao = db.rawQuery(queryAcao, arrayOf(pilarId.toString()))

        while (cursorAcao.moveToNext()) {
            val acaoId = cursorAcao.getInt(cursorAcao.getColumnIndexOrThrow("id"))
            val nomeAcao = cursorAcao.getString(cursorAcao.getColumnIndexOrThrow("nome"))
            val pilarId = cursorAcao.getInt(cursorAcao.getColumnIndexOrThrow("pilar_id"))

            val queryAtividades = "SELECT id, nome , status, aprovado FROM Atividade WHERE acao_id = ?"
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
                atividades.add(Atividade(idAtividade, nomeAtividade, status, aprovado))
            }
            cursorAtividades.close()

            lista.add(AcaoComAtividades(Acao(id = acaoId, nome = nomeAcao, pilarId), atividades))
        }

        cursorAcao.close()
        db.close()
        return lista
    }

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



    data class AcaoDTO(
        val id: Int, val nome: String, val pilar_id: Int
    )
    fun buscarAcaoPorId(id: Int): AcaoDTO? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Acao WHERE id = ?", arrayOf(id.toString()))

        var acao: AcaoDTO? = null

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
            val pilarId = cursor.getInt(cursor.getColumnIndexOrThrow("pilar_id"))
            acao = AcaoDTO(id, nome, pilarId)
        }

        cursor.close()
        db.close()
        return acao
    }


    data class PilarDTO(
        val id: Int, val numero: Int, val nome: String, val descricao: String,
        val dataInicio: String, val dataConclusao: String, val criadoPor: Int
    )
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
                    nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                    descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao")),
                    dataInicio = cursor.getString(cursor.getColumnIndexOrThrow("data_inicio")),
                    dataConclusao = cursor.getString(cursor.getColumnIndexOrThrow("data_conclusao")),
                    criadoPor = cursor.getInt(cursor.getColumnIndexOrThrow("criado_por"))
                )
                pilares.add(pilar)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return pilares
    }


    data class PilarDTObyId(
        val id: Int, val nome: String
    )
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
        WHERE a.pilar_id = ? AND at.responsavel_id = ?
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
                WHERE acao_id = ? AND responsavel_id = ?
            """.trimIndent()

                val cursorAtividades =
                    db.rawQuery(atividadesQuery, arrayOf(acaoId.toString(), usuarioId.toString()))

                if (cursorAtividades.moveToFirst()) {
                    do {
                        val atividadeId = cursorAtividades.getInt(0)
                        val atividadeNome = cursorAtividades.getString(1)
                        val status = cursorAtividades.getString(cursorAtividades.getColumnIndexOrThrow("status"))
                        val aprovado = cursorAtividades.getInt(cursorAtividades.getColumnIndexOrThrow("aprovado")) == 1

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


    data class AtividadeDTObyId(
        val id: Int, val aprovado: Boolean
    )
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




    fun atualizarStatus(idAtividade: Int, novoStatus: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("status", novoStatus)
        }
        db.update("Atividade", values, "id = ?", arrayOf(idAtividade.toString()))
        db.close()
    }

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


    fun criarNotificacaoParaCoordenador(mensagem: String, atividadeId: Int? = null, tipoNotificacao: TipoNotificacao) {
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
                    put("tipo_notificacao", tipoNotificacao.toString())
                }
                db.insert("Notificacao", null, values)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
    }




    fun aprovarAtividade(id: Int?) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("aprovado", 1)
        }
        db.update("Atividade", values, "id = ?", arrayOf(id.toString()))
    }


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

    fun inserirAcao(pilarId: Int, nome: String, descricao: String, criadoPor: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("pilar_id", pilarId)
            put("nome", nome)
            put("descricao", descricao)
            put("criado_por", criadoPor)
            put("aprovado", 0)
        }
        val id = db.insert("Acao", null, values)
        db.close()
        return id != -1L
    }





}
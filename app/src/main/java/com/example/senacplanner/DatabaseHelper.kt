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
import com.example.senacplanner.editarAtividade.AtividadeEdit
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import java.util.Locale

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "banco_teste19.db"
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

    // 🔹 Buscar ações de um pilar específico

    fun getAcoesByPilarId(pilarId: Int): List<Acao> {
        val listaAcoes = mutableListOf<Acao>()
        val db = getDatabase()

        val cursor = db.rawQuery(
            "SELECT id, nome FROM Acao WHERE pilar_id = ?",
            arrayOf(pilarId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                listaAcoes.add(Acao(id = id, nome = nome))
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

            val diasRestantes = ((dataConclusao - hoje.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

            if (diasRestantes == 7 || diasRestantes == 3) {
                val mensagem = "O Pilar \"$nomePilar\" está a $diasRestantes dias da data de conclusão."

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

            val queryAtividades = "SELECT * FROM Atividade WHERE acao_id = ?"
            val cursorAtividades = db.rawQuery(queryAtividades, arrayOf(acaoId.toString()))

            val atividades = mutableListOf<Atividade>()
            while (cursorAtividades.moveToNext()) {
                val idAtividade = cursorAtividades.getInt(cursorAtividades.getColumnIndexOrThrow("id"))
                val nomeAtividade = cursorAtividades.getString(cursorAtividades.getColumnIndexOrThrow("nome"))

                atividades.add(Atividade(idAtividade, nome = nomeAtividade))
            }
            cursorAtividades.close()

            lista.add(AcaoComAtividades(Acao(id = acaoId, nome = nomeAcao), atividades))
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
                val msg = "O Pilar \"$nomeAntigo\" teve alteração na ${alteracoes.joinToString(" e ")}."
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

    data class PilarDTO(val id: Int, val numero: Int, val nome: String, val descricao: String,
                        val dataInicio: String, val dataConclusao: String, val criadoPor: Int)

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

    fun buscarAcoesEAtividadesDoUsuarioPorPilar(pilarId: Int, usuarioId: Int): List<AcaoComAtividades> {
        val resultado = mutableListOf<AcaoComAtividades>()
        val db = readableDatabase

        val acoesQuery = """
        SELECT DISTINCT a.id, a.nome
        FROM Acao a
        JOIN Atividade at ON a.id = at.acao_id
        WHERE a.pilar_id = ? AND at.responsavel_id = ?
    """.trimIndent()

        val cursorAcoes = db.rawQuery(acoesQuery, arrayOf(pilarId.toString(), usuarioId.toString()))

        if (cursorAcoes.moveToFirst()) {
            do {
                val acaoId = cursorAcoes.getInt(0)
                val acaoNome = cursorAcoes.getString(1)
                val acao = Acao(acaoId, acaoNome)

                val atividades = mutableListOf<Atividade>()

                val atividadesQuery = """
                SELECT id, nome
                FROM Atividade
                WHERE acao_id = ? AND responsavel_id = ?
            """.trimIndent()

                val cursorAtividades = db.rawQuery(atividadesQuery, arrayOf(acaoId.toString(), usuarioId.toString()))

                if (cursorAtividades.moveToFirst()) {
                    do {
                        val atividadeId = cursorAtividades.getInt(0)
                        val atividadeNome = cursorAtividades.getString(1)
                        atividades.add(Atividade(atividadeId, atividadeNome))
                    } while (cursorAtividades.moveToNext())
                }

                cursorAtividades.close()
                resultado.add(AcaoComAtividades(acao, atividades))
            } while (cursorAcoes.moveToNext())
        }

        cursorAcoes.close()
        return resultado
    }

    fun atualizarAtividade(
        id: Int,
        novoNome: String,
        responsavel: Int,
        dataInicio: String,
        dataConclusao: String,
        status: String
    ): AtividadeEdit? {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("nome", novoNome)
            put("responsavel_id", responsavel)
            put("data_inicio", dataInicio)
            put("data_conclusao", dataConclusao)
            put("status", status)
        }

        db.update("Atividade", values, "id = ?", arrayOf(id.toString()))

        return buscarAtividadePorId(id)
    }


    fun buscarAtividadePorId(id: Int): AtividadeEdit? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM atividade WHERE id = ?", arrayOf(id.toString()))

        var atividade: AtividadeEdit? = null

        if (cursor.moveToFirst()) {
            val nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
            val responsavelId = cursor.getInt(cursor.getColumnIndexOrThrow("responsavel_id"))
            val dataInicio = cursor.getString(cursor.getColumnIndexOrThrow("data_inicio"))
            val dataConclusao = cursor.getString(cursor.getColumnIndexOrThrow("data_conclusao"))
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))

            atividade = AtividadeEdit(id, nome, status, dataInicio, dataConclusao, responsavelId)
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
        responsavelId: Int?

        )
    {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("acao_id", acaoId)
            put("nome", nome)
            put("descricao", descricao)
            put("status", status)
            put("data_inicio", dataInicio)
            put("data_conclusao", dataConclusao)
            put("criado_por", criadoPor)
            put("aprovado", 0)
            if (responsavelId != null) {
                put("responsavel_id", responsavelId)
            }
        }

        db.insert("atividade", null, values)
        db.close()
    }


}

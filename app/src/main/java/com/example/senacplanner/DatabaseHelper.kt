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
import java.io.FileOutputStream
import java.io.IOException

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "banco_teste17.db"
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
    // O método onCreate foi alterado para não recriar a tabela, já que o banco de dados já está copiado
    override fun onCreate(db: SQLiteDatabase?) {
        // Não é necessário recriar a tabela, já que ela já existe no arquivo copiado
    }
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
            if (descricao != null) {
                put("descricao", descricao)
            } else {
                putNull("descricao")
            }
            put("data_inicio", dataInicio)
            put("data_conclusao", dataConclusao)
            put("criado_por", criadoPorId)
        }

        val resultado = db.insert("Pilar", null, values)
        if (resultado != -1L) {
            criarNotificacaoParaTodos("Novo Pilar criado: $nome")
        }
        db.close()
        return resultado
    }

    fun vincularUsuarioAoPilar(usuarioId: Int, pilarId: Long): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("usuario_id", usuarioId)
            put("pilar_id", pilarId)
        }
        val resultado = db.insert("UsuarioPilar", null, values)
        db.close()
        return resultado != -1L
    }

    fun criarNotificacaoParaUsuario(usuarioId: Int, mensagem: String, atividadeId: Int? = null) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("usuario_id", usuarioId)
            put("mensagem", mensagem)
            put("data", System.currentTimeMillis().toString())
            put("lida", 0)
            if (atividadeId != null) put("atividade_id", atividadeId)
        }
        db.insert("Notificacao", null, values)
        db.close()
    }

    fun criarNotificacaoParaTodos(mensagem: String, atividadeId: Int? = null) {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT id FROM Usuario", null)

        if (cursor.moveToFirst()) {
            do {
                val usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val values = ContentValues().apply {
                    put("usuario_id", usuarioId)
                    put("mensagem", mensagem)
                    put("data", System.currentTimeMillis().toString())
                    put("lida", 0)
                    if (atividadeId != null) put("atividade_id", atividadeId)
                }
                db.insert("Notificacao", null, values)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
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
                val idAtividade =
                    cursorAtividades.getInt(cursorAtividades.getColumnIndexOrThrow("id"))
                val nomeAtividade =
                    cursorAtividades.getString(cursorAtividades.getColumnIndexOrThrow("nome"))
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

        val dadosAntigos = getDatasPilarById(id)
        val values = ContentValues().apply {
            put("nome", novoNome)
            put("data_inicio", novaDataInicio)
            put("data_conclusao", novaDataConclusao)
        }

        db.update("Pilar", values, "id = ?", arrayOf(id.toString()))

        if (dadosAntigos != null) {
            val (nome, dataInicioAntiga, dataConclusaoAntiga) = dadosAntigos
            val alteracoes = mutableListOf<String>()

            if (dataInicioAntiga != novaDataInicio) {
                alteracoes.add("data de início")
            }
            if (dataConclusaoAntiga != novaDataConclusao) {
                alteracoes.add("data de conclusão")
            }

            if (alteracoes.isNotEmpty()) {
                val msg = "O Pilar \"$nome\" teve alteração na ${alteracoes.joinToString(" e ")}."
                criarNotificacaoParaTodos(msg)
            }
        }
    }

    fun excluirPilar(id: Int): Boolean {
        val db = writableDatabase

        // Buscar nome antes de excluir
        val cursor = db.rawQuery("SELECT nome FROM Pilar WHERE id = ?", arrayOf(id.toString()))
        var nomePilar: String? = null
        if (cursor.moveToFirst()) {
            nomePilar = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
        }
        cursor.close()

        val rowsDeleted = db.delete("Pilar", "id = ?", arrayOf(id.toString()))

        if (rowsDeleted > 0 && nomePilar != null) {
            criarNotificacaoParaTodos("O Pilar \"$nomePilar\" foi excluído da lista de Pilares.")
        }


        db.close()
        return rowsDeleted > 0
    }

    fun marcarNotificacaoComoLida(notificacaoId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("lida", 1)
        }
        db.update("Notificacao", values, "id = ?", arrayOf(notificacaoId.toString()))
        db.close()
    }
}
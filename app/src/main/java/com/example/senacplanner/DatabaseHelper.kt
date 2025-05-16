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

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Realizar upgrade no banco, mas agora sem a coluna 'senha'
    }

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

        if (resultado != -1L) {
            val cursor = db.rawQuery("SELECT nome FROM Pilar WHERE id = ?", arrayOf(pilarId.toString()))
            if (cursor.moveToFirst()) {
                val nomePilar = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                val mensagem = "Você foi designado ao Pilar: $nomePilar"
                criarNotificacaoParaUsuario(usuarioId, mensagem)
            }
            cursor.close()
        }

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
        val cursor = db.rawQuery("""
            SELECT u.id, u.nome, u.tipo 
            FROM Usuario u
            INNER JOIN Atividade a ON u.id = a.responsavel_id
            WHERE a.id = ?
        """.trimIndent(), arrayOf(atividadeId.toString()))

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

    fun verificarNotificacoesDePilaresProximos() {
        val db = getDatabase()
        val diasAlvo = listOf(7, 3)
        val milisPorDia = 24 * 60 * 60 * 1000L
        val agora = System.currentTimeMillis()

        for (dias in diasAlvo) {
            val alvoMillis = agora + dias * milisPorDia

            val cursorPilares = db.rawQuery(
                """
            SELECT id, nome, data_conclusao FROM Pilar
            WHERE data_conclusao IS NOT NULL
            """, null
            )

            while (cursorPilares.moveToNext()) {
                val pilarId = cursorPilares.getInt(cursorPilares.getColumnIndexOrThrow("id"))
                val nomePilar = cursorPilares.getString(cursorPilares.getColumnIndexOrThrow("nome"))
                val dataConclusaoStr = cursorPilares.getString(cursorPilares.getColumnIndexOrThrow("data_conclusao"))
                val dataConclusaoMillis = dataConclusaoStr.toLongOrNull() ?: continue


                val diferenca = kotlin.math.abs(dataConclusaoMillis - alvoMillis)
                if (diferenca <= 12 * 60 * 60 * 1000) {
                    notificarUsuariosDoPilarProximo(pilarId, nomePilar, dias)
                }
            }

            cursorPilares.close()
        }
    }

    private fun notificarUsuariosDoPilarProximo(pilarId: Int, nomePilar: String, diasRestantes: Int) {
        val db = getDatabase()

        val cursor = db.rawQuery(
            "SELECT usuario_id FROM UsuarioPilar WHERE pilar_id = ?",
            arrayOf(pilarId.toString())
        )

        val mensagem = "Fique atento! O pilar \"$nomePilar\" precisa ser concluído em $diasRestantes dias."
        val dataAtual = System.currentTimeMillis().toString()

        while (cursor.moveToNext()) {
            val usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow("usuario_id"))

            // Verifica se notificação já existe para evitar duplicatas
            val cursorCheck = db.rawQuery(
                """
            SELECT id FROM Notificacao 
            WHERE usuario_id = ? AND mensagem = ? AND ABS(CAST(data AS INTEGER) - ?) < 86400000
            """.trimIndent(),
                arrayOf(usuarioId.toString(), mensagem, dataAtual)
            )

            if (!cursorCheck.moveToFirst()) {
                val values = ContentValues().apply {
                    put("usuario_id", usuarioId)
                    put("mensagem", mensagem)
                    put("data", dataAtual)
                    put("lida", 0)
                }
                db.insert("Notificacao", null, values)
            }

            cursorCheck.close()
        }

        cursor.close()
    }


    fun getDatasPilarById(id: Int): Triple<String, String, String>? {
        val db = getDatabase()
        val cursor = db.rawQuery("SELECT nome, data_inicio, data_conclusao FROM Pilar WHERE id = ?", arrayOf(id.toString()))

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

    fun atualizarPilar(id: Int, novoNome: String, novaDataInicio: String, novaDataConclusao: String) {
        val db = getDatabase()
        val values = ContentValues().apply {
            put("nome", novoNome)
            put("data_inicio", novaDataInicio)
            put("data_conclusao", novaDataConclusao)
        }
        db.update("Pilar", values, "id = ?", arrayOf(id.toString()))
    }

    fun excluirPilar(id: Int): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete("Pilar", "id = ?", arrayOf(id.toString()))
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

}

package com.example.senacplanner

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.senacplanner.Pilares.Type.PilarType
import com.example.senacplanner.Pilares.Type.Usuario
import java.io.FileOutputStream
import java.io.IOException
import android.util.Log


class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "banco_teste10.db"
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
        descricao: String ? = null,
        dataInicio: String,
        dataConclusao: String,
        criadoPorId: Int
    ): Boolean {
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
        return resultado != -1L
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

    fun salvarAtividade(titulo: String, descricao: String, acaoId: Int, criadoPorId: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("nome", titulo)  // Corrigido de "titulo" para "nome"
            put("descricao", descricao)
            put("acao_id", acaoId)  // Certifique-se de passar o valor correto para 'acao_id'
            put("data_inicio", "2025-05-10") // Exemplo, defina um valor adequado
            put("criado_por", criadoPorId)  // Certifique-se de passar o valor correto para 'criado_por'
            put("status", "Em andamento")  // Defina o status de acordo com a lógica do seu app
        }

        val resultado = db.insert("Atividade", null, values)

        if (resultado == -1L) {
            // Log do erro para o Logcat
            Log.e("DatabaseError", "Erro ao salvar atividade. Código de erro: $resultado")
            db.close()
            return false
        }

        db.close()
        return true
    }

    fun getAcoes(): List<String> {
        val acoes = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT nome FROM Acao", null)

        if (cursor.moveToFirst()) {
            do {
                val nomeAcao = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                acoes.add(nomeAcao)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return acoes
    }

    fun getAcaoIdByNome(nome: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT id FROM Acao WHERE nome = ?", arrayOf(nome))

        var id = -1
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }

        cursor.close()
        db.close()
        return id
    }

    fun listarTabelas(): List<String> {
        val tabelas = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)

        if (cursor.moveToFirst()) {
            do {
                val nomeTabela = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                tabelas.add(nomeTabela)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return tabelas
    }




}




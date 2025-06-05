package com.example.senacplanner.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.senacplanner.utils.PdfAcao
import com.example.senacplanner.utils.PdfAtividade
import com.example.senacplanner.utils.PdfPilar
import com.example.senacplanner.utils.PdfUsuario
import com.example.senacplanner.model.RelatorioPilar
import com.example.senacplanner.model.RelatorioPeriodo
import java.io.File
import java.io.FileOutputStream

class RelatorioDatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, "novobanco_.db", null, 1) {

    private val dbPath: String
        get() = context.getDatabasePath("novobanco_.db").path

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

    private fun copiarBancoSeNecessario() {
        val dbFile = File(dbPath)
        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            context.assets.open("RelatorioDB.db").use { inputStream ->
                FileOutputStream(dbFile).use { output ->
                    inputStream.copyTo(output)
                }
            }
        }
    }

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

    fun buscarPilarPorIdParaRelatorio(db: SQLiteDatabase, pilarId: Int, periodoMeses: Int): List<PdfPilar> {
        val pilar = montarPilarCompletoParaRelatorio(db, pilarId, periodoMeses)
        return if (pilar != null) listOf(pilar) else emptyList()
    }

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

    fun buscarPeriodosFixos(): List<RelatorioPeriodo> {
        return listOf(
            RelatorioPeriodo(id = 3, descricao = "Últimos 3 meses"),
            RelatorioPeriodo(id = 6, descricao = "Últimos 6 meses"),
            RelatorioPeriodo(id = 12, descricao = "Últimos 12 meses")
        )
    }


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

    private fun buscarAtividadesDaAcaoParaRelatorio(db: SQLiteDatabase, acaoId: Int, periodoMeses: Int): List<PdfAtividade> {
        val atividades = mutableListOf<PdfAtividade>()

        val query = """
        SELECT * FROM Atividade 
        WHERE acao_id = ? AND (
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

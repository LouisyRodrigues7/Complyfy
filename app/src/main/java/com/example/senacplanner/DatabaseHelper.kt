package com.example.senacplanner

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.senacplanner.types.Pilar
import java.io.FileOutputStream
import java.io.IOException

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "banco_teste1"
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

    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

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

    fun getPilarPerId(id: Int): String? {
        val db = getDatabase()
        var nomePilar: String? = null

        val query = "SELECT nome FROM pilares WHERE id = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString()))

        if (cursor.moveToFirst()) {
            nomePilar = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
        }

        cursor.close()
        db.close()

        return nomePilar
    }

    fun getAllPilares(): List<Pilar> {
        val pilares = mutableListOf<Pilar>()
        val db = getDatabase()
        val cursor = db.rawQuery("SELECT id, titulo, descricao FROM pilares", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"))
                val descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao"))

                pilares.add(Pilar(id, titulo, descricao))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return pilares
    }



}

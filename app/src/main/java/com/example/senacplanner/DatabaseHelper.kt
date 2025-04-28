package com.example.senacplanner

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.IOException

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "banco_teste1.db"  // Nome exato do seu arquivo .db
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
            this.readableDatabase.close() // Cria o arquivo vazio

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
        // Não precisa criar nada aqui porque já estamos copiando um banco pronto.
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Deixe vazio por enquanto, até precisar atualizar versão do banco.
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
}

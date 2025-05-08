package com.example.senacplanner

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    private lateinit var databaseHelper: DatabaseHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val spinner = findViewById<Spinner>(R.id.spinner_tipo_usuario)
        val editTextLogin = findViewById<EditText>(R.id.editText_login)
        val botaoEntrar = findViewById<Button>(R.id.button_entrar)

        val opcoes = listOf("Coordenador", "Apoio", "Gestor")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, opcoes)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spinner.adapter = adapter

        databaseHelper = DatabaseHelper(this)

        botaoEntrar.setOnClickListener {
            val tipoSelecionado = spinner.selectedItem.toString().lowercase()
            val login = editTextLogin.text.toString().trim()

            if (login.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                val db = databaseHelper.readableDatabase

                // Alteração: Remover a senha da consulta
                val cursor: Cursor = db.rawQuery(
                    "SELECT * FROM usuarios WHERE email = ? AND tipo = ?",
                    arrayOf(login, tipoSelecionado)
                )

                Log.d("LOGIN_DEBUG", "Tentando login com: $login | Tipo: $tipoSelecionado")

                if (cursor.moveToFirst()) {
                    val nomeUsuario = cursor.getString(cursor.getColumnIndexOrThrow("nome"))

                    when (tipoSelecionado) {
                        "coordenador" -> {
                            val intent = Intent(this, CoordenadorActivity::class.java)
                            intent.putExtra("NOME_USUARIO", nomeUsuario)
                            startActivity(intent)
                        }
                        "apoio" -> {
                            val intent = Intent(this, ApoioActivity::class.java)
                            intent.putExtra("NOME_USUARIO", nomeUsuario)
                            startActivity(intent)
                        }
                        "gestor" -> {
                            val intent = Intent(this, GestorActivity::class.java)
                            intent.putExtra("NOME_USUARIO", nomeUsuario)
                            startActivity(intent)
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Usuário não encontrado. Verifique o e-mail e tipo.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                cursor.close()
                db.close()
            }
        }
    }
}


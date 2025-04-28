package com.example.senacplanner

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    private lateinit var databaseHelper: DatabaseHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referências
        val spinner = findViewById<Spinner>(R.id.spinner_tipo_usuario)
        val editTextLogin = findViewById<EditText>(R.id.editText_login)
        val editTextSenha = findViewById<EditText>(R.id.editText_senha)
        val botaoEntrar = findViewById<Button>(R.id.button_entrar)

        // Opções do Spinner
        val opcoes = listOf("Coordenador", "Apoio", "Gestor")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Inicializar o DatabaseHelper
        databaseHelper = DatabaseHelper(this)

        // Ação do botão Entrar
        botaoEntrar.setOnClickListener {
            val tipoSelecionado = spinner.selectedItem.toString().lowercase() // deixei minúsculo porque no banco é em minúsculo
            val login = editTextLogin.text.toString()
            val senha = editTextSenha.text.toString()

            if (login.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                val db = databaseHelper.readableDatabase

                // Consulta no banco
                val cursor: Cursor = db.rawQuery(
                    "SELECT * FROM usuarios WHERE nome = ? AND tipo = ?",
                    arrayOf(login, tipoSelecionado)
                )

                if (cursor.moveToFirst()) {
                    // Se achou o usuário
                    when (tipoSelecionado) {
                        "coordenador" -> {
                            val intent = Intent(this, CoordenadorActivity::class.java)
                            intent.putExtra("NOME_USUARIO", login)
                            startActivity(intent)
                        }
                        "apoio" -> {
                            val intent = Intent(this, ApoioActivity::class.java)
                            intent.putExtra("NOME_USUARIO", login)
                            startActivity(intent)
                        }
                        "gestor" -> {
                            val intent = Intent(this, GestorActivity::class.java)
                            intent.putExtra("NOME_USUARIO", login)
                            startActivity(intent)
                        }
                    }
                } else {
                    Toast.makeText(this, "Usuário ou tipo incorretos", Toast.LENGTH_SHORT).show()
                }
                cursor.close()
                db.close()
            }
        } // Fechamento do setOnClickListener
    } // Fechamento do onCreate
} // Fechamento da classe

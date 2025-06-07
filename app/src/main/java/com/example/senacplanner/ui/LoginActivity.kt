package com.example.senacplanner.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.senacplanner.data.DatabaseHelper
import com.example.senacplanner.R

/**
 * Tela de login do sistema.
 * Permite o acesso por tipo de usuário (Coordenador, Apoio ou Gestor),
 * validando o login com base no e-mail e tipo no banco de dados.
 */
class LoginActivity : AppCompatActivity() {


    private lateinit var databaseHelper: DatabaseHelper

    /**
     * Inicializa a tela de login, configurando o spinner de tipos de usuário
     * e o botão para tentar autenticação.
     */
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Aplica padding para evitar sobreposição com barras do sistema (status/nav bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val spinner = findViewById<Spinner>(R.id.spinner_tipo_usuario)
        val editTextLogin = findViewById<EditText>(R.id.editText_login)
        val botaoEntrar = findViewById<Button>(R.id.button_entrar)

        // Lista fixa com os tipos de usuário disponíveis no sistema
        val opcoes = listOf("Coordenador", "Apoio", "Gestor")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, opcoes)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spinner.adapter = adapter

        databaseHelper = DatabaseHelper(this)

        /**
         * Evento de clique no botão "Entrar".
         * Verifica se o login existe no banco de dados e redireciona para a tela correta.
         */
        botaoEntrar.setOnClickListener {
            val tipoSelecionado = spinner.selectedItem.toString()
            val login = editTextLogin.text.toString().trim()

            if (login.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                val db = databaseHelper.readableDatabase

                // Consulta sem senha: valida apenas por e-mail e tipo
                val cursor: Cursor = db.rawQuery(
                    "SELECT * FROM Usuario WHERE email = ? AND tipo = ?",
                    arrayOf(login, tipoSelecionado)
                )

                Log.d("LOGIN_DEBUG", "Tentando login com: $login | Tipo: $tipoSelecionado")

                if (cursor.moveToFirst()) {
                    val nomeUsuario = cursor.getString(cursor.getColumnIndexOrThrow("nome"))
                    val idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("id"))

                    // Redireciona conforme o tipo de usuário
                    when (tipoSelecionado) {
                        "Coordenador", "Apoio" -> {
                            val intent = Intent(this, CoordenadorActivity::class.java).apply {
                                putExtra("NOME_USUARIO", nomeUsuario)
                                putExtra("ID_USUARIO", idUsuario)
                                putExtra("TIPO_USUARIO", tipoSelecionado)
                            }
                            startActivity(intent)
                            finish()
                        }
                        "Gestor" -> {
                            val intent = Intent(this, GestorActivity::class.java).apply {
                                putExtra("NOME_USUARIO", nomeUsuario)
                                putExtra("ID_USUARIO", idUsuario)
                                putExtra("TIPO_USUARIO", tipoSelecionado)
                            }
                            startActivity(intent)
                            finish()
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

package com.example.senacplanner

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.senacplanner.R

class LoginActivity : AppCompatActivity() {
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

        // Ação do botão Entrar
        botaoEntrar.setOnClickListener {
            val tipoSelecionado = spinner.selectedItem.toString()
            val login = editTextLogin.text.toString()
            val senha = editTextSenha.text.toString()

            // Exemplo simples: mostrar um Toast
            Toast.makeText(
                this,
                "Login: $login\nSenha: $senha\nTipo: $tipoSelecionado",
                Toast.LENGTH_SHORT
            ).show()

        }
    }
}
package com.example.senacplanner.Pilares

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.R
import com.example.senacplanner.DatabaseHelper

class CriarAtividadeActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_atividade)

        val editTitulo = findViewById<EditText>(R.id.editTitulo)
        val editDescricao = findViewById<EditText>(R.id.editDescricao)
        val spinnerAcao = findViewById<Spinner>(R.id.spinnerAcao)
        val btnSalvar = findViewById<Button>(R.id.btnSalvar)

        // Inicializa o dbHelper corretamente
        dbHelper = DatabaseHelper(this)

        // Preenche o spinner com ações
        val acoes = dbHelper.getAcoes()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, acoes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAcao.adapter = adapter

        btnSalvar.setOnClickListener {
            val titulo = editTitulo.text.toString()
            val descricao = editDescricao.text.toString()
            val acaoSelecionada = spinnerAcao.selectedItem.toString()
            val acaoId = dbHelper.getAcaoIdByNome(acaoSelecionada)
            val criadoPorId = 1 // Coloque o ID do usuário logado real aqui

            if (titulo.isBlank() || descricao.isBlank()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                val sucesso = dbHelper.salvarAtividade(titulo, descricao, acaoId, criadoPorId)
                if (sucesso) {
                    Toast.makeText(this, "Atividade salva com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Erro ao salvar atividade.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}








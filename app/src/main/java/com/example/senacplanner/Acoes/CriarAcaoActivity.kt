package com.example.senacplanner.Acoes


import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.senacplanner.DatabaseHelper
import com.example.senacplanner.R

class CriarAcaoActivity : Activity() {

    private lateinit var editNome: EditText
    private lateinit var editDescricao: EditText
    private lateinit var btnSalvar: Button
    private lateinit var btnCancelar: Button
    private var usuarioId: Int = -1
    private var pilarId: Int = -1
    private var criadoPorId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_acao)

        editNome = findViewById(R.id.editNomeAcao)
        editDescricao = findViewById(R.id.editDescricaoAcao)
        btnSalvar = findViewById(R.id.btnSalvarAcao)
        btnCancelar = findViewById(R.id.btnCancelar)

        pilarId = intent.getIntExtra("PILAR_ID", -1)
        usuarioId = intent.getIntExtra("ID_USUARIO", -1)
        criadoPorId = usuarioId // Substituir com ID do usuário logado, se tiver controle

        btnSalvar.setOnClickListener {
            val nome = editNome.text.toString()
            val descricao = editDescricao.text.toString()

            if (nome.isNotEmpty()) {
                val dbHelper = DatabaseHelper(this)
                val sucesso = dbHelper.inserirAcao(pilarId, nome, descricao, criadoPorId)
                if (sucesso) {
                    Toast.makeText(this, "Ação criada com sucesso", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, "Erro ao salvar ação", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Nome obrigatório", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }
}

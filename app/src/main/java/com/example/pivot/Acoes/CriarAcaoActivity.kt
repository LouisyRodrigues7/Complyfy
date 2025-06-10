package com.example.pivot.Acoes


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.pivot.data.DatabaseHelper
import com.example.pivot.R
import com.example.pivot.adapter.TipoNotificacao

/**
 * Activity responsável pela criação de uma nova ação vinculada a um pilar.
 *
 * Permite ao usuário inserir o nome e a descrição da ação e salvá-la no banco de dados.
 * Também oferece opção para cancelar a operação.
 */
class CriarAcaoActivity : Activity() {

    private lateinit var editNome: EditText
    private lateinit var editDescricao: EditText
    private lateinit var btnSalvar: Button
    private lateinit var btnCancelar: Button
    private var usuarioId: Int = -1
    private var pilarId: Int = -1
    private var criadoPorId: Int = -1
    private var usuarioTipo: String = ""
    private var acaoId: Long = -1

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
        usuarioTipo = intent.getStringExtra("TIPO_USUARIO").toString()

        btnSalvar.setOnClickListener {
            val nome = editNome.text.toString()
            val descricao = editDescricao.text.toString()

            if (nome.isNotEmpty()) {
                val dbHelper = DatabaseHelper(this)
                val aprovado = usuarioTipo == "Coordenador"
                val acaoId = dbHelper.inserirAcao(pilarId, nome, descricao, criadoPorId, aprovado)

                if (!aprovado) {
                    dbHelper.criarNotificacaoParaCoordenador(
                        "Nova Ação ($nome) aguardando aprovação!!",
                        null,
                        acaoId.toInt(),
                        TipoNotificacao.APROVACAO_ACAO
                    )
                    Toast.makeText(this, "Ação aguardando aprovação!", Toast.LENGTH_SHORT).show()
                    Log.d("TESTE SALVAR", acaoId.toString())
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, "Ação criada e aprovada com sucesso!", Toast.LENGTH_SHORT).show()
                    val resultIntent = Intent().apply {
                        putExtra("acao_aprovada_id", acaoId.toInt())
                    }
                    setResult(Activity.RESULT_OK)
                    finish()
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

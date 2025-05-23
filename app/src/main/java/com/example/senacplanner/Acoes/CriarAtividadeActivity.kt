package com.example.senacplanner.Acoes

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.DatabaseHelper
import com.example.senacplanner.R
import java.text.SimpleDateFormat
import java.util.*

class CriarAtividadeActivity : AppCompatActivity() {

    private lateinit var nomeEditText: EditText
    private lateinit var descricaoEditText: EditText
    private lateinit var dataInicioButton: Button
    private lateinit var dataConclusaoButton: Button
    private lateinit var statusSpinner: Spinner
    private lateinit var salvarButton: Button

    private var dataInicio: String = ""
    private var dataConclusao: String = ""
    private var acaoId: Int = -1
    private var usuarioId: Int = -1

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_atividade)

        nomeEditText = findViewById(R.id.editNome)
        descricaoEditText = findViewById(R.id.editDescricao)
        dataInicioButton = findViewById(R.id.btnDataInicio)
        dataConclusaoButton = findViewById(R.id.btnDataConclusao)
        statusSpinner = findViewById(R.id.spinnerStatus)
        salvarButton = findViewById(R.id.btnSalvar)

        acaoId = intent.getIntExtra("ACAO_ID", -1)
        usuarioId = intent.getIntExtra("USUARIO_ID", -1)

        if (acaoId == -1 || usuarioId == -1) {
            Toast.makeText(this, "Erro ao obter contexto da ação ou usuário.", Toast.LENGTH_SHORT).show()
            finish()
        }

        statusSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.status_array,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        dataInicioButton.setOnClickListener { showDatePicker { date ->
            dataInicio = date
            dataInicioButton.text = date
        }}

        dataConclusaoButton.setOnClickListener { showDatePicker { date ->
            dataConclusao = date
            dataConclusaoButton.text = date
        }}

        salvarButton.setOnClickListener {
            val nome = nomeEditText.text.toString()
            val descricao = descricaoEditText.text.toString()
            val status = statusSpinner.selectedItem.toString()

            if (nome.isBlank() || dataInicio.isBlank()) {
                Toast.makeText(this, "Preencha os campos obrigatórios.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = DatabaseHelper(this)
            db.inserirAtividade(
                acaoId,
                nome,
                descricao,
                status,
                dataInicio,
                dataConclusao,
                usuarioId
            )

            Toast.makeText(this, "Atividade criada com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showDatePicker(callback: (String) -> Unit) {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val data = Calendar.getInstance()
            data.set(year, month, day)
            callback(dateFormat.format(data.time))
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }
}

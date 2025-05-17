package com.example.senacplanner.Pilares

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.R
import com.example.senacplanner.DatabaseHelper
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log


class CriarAtividadeActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_atividade)

        val editTitulo = findViewById<EditText>(R.id.editTitulo)
        val editDescricao = findViewById<EditText>(R.id.editDescricao)
        val editDataInicio = findViewById<EditText>(R.id.editDataInicio)
        val spinnerAcao = findViewById<Spinner>(R.id.spinnerAcao)
        val btnSalvar = findViewById<Button>(R.id.btnSalvar)

        dbHelper = DatabaseHelper(this)

        // Spinner de ações
        val acoes = dbHelper.getAcoes()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, acoes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAcao.adapter = adapter

        // Calendário ao clicar no campo de data
        val calendario = Calendar.getInstance()
        val formatoData = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        editDataInicio.setOnClickListener {
            val ano = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, y, m, d ->
                calendario.set(y, m, d)
                editDataInicio.setText(formatoData.format(calendario.time))
            }, ano, mes, dia).show()
        }

        val editDataConclusao = findViewById<EditText>(R.id.editDataConclusao)

        // Calendário para data de conclusão
                editDataConclusao.setOnClickListener {
                    val ano = calendario.get(Calendar.YEAR)
                    val mes = calendario.get(Calendar.MONTH)
                    val dia = calendario.get(Calendar.DAY_OF_MONTH)

                    DatePickerDialog(this, { _, y, m, d ->
                        calendario.set(y, m, d)
                        editDataConclusao.setText(formatoData.format(calendario.time))
                    }, ano, mes, dia).show()
                }


        btnSalvar.setOnClickListener {
            val titulo = editTitulo.text.toString()
            val descricao = editDescricao.text.toString()
            val acaoSelecionada = spinnerAcao.selectedItem.toString()
            val acaoId = dbHelper.getAcaoIdByNome(acaoSelecionada)

            Log.d("CriarAtividade", "Ação selecionada: $acaoSelecionada | ID: $acaoId")

            if (acaoId == -1) {
                Toast.makeText(this, "Erro: ação não encontrada no banco!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val criadoPorId = 1 // Exemplo
            val dataInicio = editDataInicio.text.toString()
            val dataConclusao = editDataConclusao.text.toString()

            if (titulo.isBlank() || descricao.isBlank() || dataInicio.isBlank() || dataConclusao.isBlank()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                val sucesso = dbHelper.salvarAtividade(titulo, descricao, acaoId, criadoPorId, dataInicio, dataConclusao)
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










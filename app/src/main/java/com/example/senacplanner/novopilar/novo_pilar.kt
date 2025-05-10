package com.example.senacplanner.novopilar

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.DatabaseHelper
import com.example.senacplanner.Pilares.Type.Usuario
import com.example.senacplanner.R

class NovoPilarActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var editTextNome: EditText
    private lateinit var editTextDataInicio: EditText
    private lateinit var editTextDataConclusao: EditText
    private lateinit var spinnerResponsavel: Spinner
    private lateinit var btnCriarPilar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_pilar) //

        // Pegando os elementos da tela
        editTextNome = findViewById(R.id.editTextNome)
        editTextDataInicio = findViewById(R.id.editTextDataInicio)
        editTextDataConclusao = findViewById(R.id.editTextDataConclusao)
        spinnerResponsavel = findViewById(R.id.spinnerResponsavel)
        btnCriarPilar = findViewById(R.id.btnCriarPilar)

        databaseHelper = DatabaseHelper(this)

        val responsaveis = databaseHelper.listarResponsaveis()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, responsaveis)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerResponsavel.adapter = adapter

        // Exemplo: ação ao clicar no botão "Nova Ação"
        btnCriarPilar.setOnClickListener {

            val nome = editTextNome.text.toString()
            val dataInicio = editTextDataInicio.text.toString()
            val dataConclusao = editTextDataConclusao.text.toString()
            val usuarioSelecionado = spinnerResponsavel.selectedItem as Usuario
            val criadoPorId = usuarioSelecionado.id

            if (nome.isBlank() || dataInicio.isBlank() || dataConclusao.isBlank()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val numero = databaseHelper.obterProximoNumeroPilar()
            val sucesso = databaseHelper.cadastrarPilar(numero, nome, null, dataInicio, dataConclusao, criadoPorId)

            if (sucesso) {
                Toast.makeText(this, "Pilar cadastrado com sucesso!", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Erro ao cadastrar pilar.", Toast.LENGTH_SHORT).show()
            }
            Toast.makeText(this, "Responsável: ${usuarioSelecionado.nome}", Toast.LENGTH_SHORT).show()

        }


    }
}

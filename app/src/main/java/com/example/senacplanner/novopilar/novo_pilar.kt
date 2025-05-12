package com.example.senacplanner.novopilar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.senacplanner.Acoes.ListaAtividades
import com.example.senacplanner.DatabaseHelper
import com.example.senacplanner.Acoes.Type.Usuario
import com.example.senacplanner.R
import com.example.senacplanner.fragmentpilares.TodosPilaresFragment

class NovoPilarActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var editTextNome: EditText
    private lateinit var editTextDataInicio: EditText
    private lateinit var editTextDataConclusao: EditText
    private lateinit var spinnerResponsavel: Spinner
    private lateinit var btnCriarPilar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_pilar)

        // Inicializa os componentes da tela
        editTextNome = findViewById(R.id.editTextNome)
        editTextDataInicio = findViewById(R.id.editTextDataInicio)
        editTextDataConclusao = findViewById(R.id.editTextDataConclusao)
        spinnerResponsavel = findViewById(R.id.spinnerResponsavel)
        btnCriarPilar = findViewById(R.id.btnCriarPilar)

        // Inicializa o banco
        databaseHelper = DatabaseHelper(this)

        // Preenche o spinner de responsáveis
        val responsaveis = databaseHelper.listarResponsaveis()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, responsaveis)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerResponsavel.adapter = adapter

        // Quando o botão for clicado, mostra o diálogo de confirmação
        btnCriarPilar.setOnClickListener {
            mostrarDialogoConfirmacao()
        }
    }

    private fun mostrarDialogoConfirmacao() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.confirmar_pilar, null)

        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancelar.setOnClickListener {
            alertDialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            cadastrarPilar()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun cadastrarPilar() {
        val nome = editTextNome.text.toString()
        val dataInicio = editTextDataInicio.text.toString()
        val dataConclusao = editTextDataConclusao.text.toString()
        val usuarioSelecionado = spinnerResponsavel.selectedItem as? Usuario

        if (nome.isBlank() || dataInicio.isBlank() || dataConclusao.isBlank() || usuarioSelecionado == null) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        val numero = databaseHelper.obterProximoNumeroPilar()
        val sucesso = databaseHelper.cadastrarPilar(
            numero,
            nome,
            null,
            dataInicio,
            dataConclusao,
            usuarioSelecionado.id
        )

        if (sucesso) {
            Toast.makeText(this, "Pilar cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Erro ao cadastrar pilar.", Toast.LENGTH_SHORT).show()
        }

        Toast.makeText(this, "Responsável: ${usuarioSelecionado.nome}", Toast.LENGTH_SHORT).show()
    }
}


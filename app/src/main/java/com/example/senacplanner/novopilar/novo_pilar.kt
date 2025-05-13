package com.example.senacplanner.novopilar

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.Acoes.Type.Usuario
import com.example.senacplanner.DatabaseHelper
import com.example.senacplanner.R
import java.text.SimpleDateFormat
import java.util.*

class NovoPilarActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var editTextNome: EditText
    private lateinit var editTextDataInicio: EditText
    private lateinit var editTextDataConclusao: EditText
    private lateinit var spinnerResponsavel: Spinner
    private lateinit var btnCriarPilar: Button

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // 🔹 formato brasileiro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_pilar)

        // Inicializa os componentes da interface
        editTextNome = findViewById(R.id.editTextNome)
        editTextDataInicio = findViewById(R.id.editTextDataInicio)
        editTextDataConclusao = findViewById(R.id.editTextDataConclusao)
        spinnerResponsavel = findViewById(R.id.spinnerResponsavel)
        btnCriarPilar = findViewById(R.id.btnCriarPilar)

        // Inicializa o banco de dados
        databaseHelper = DatabaseHelper(this)

        // Carrega a lista de responsáveis e configura o spinner
        val responsaveis = databaseHelper.listarResponsaveis()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, responsaveis)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerResponsavel.adapter = adapter

        // Configura o botão para mostrar o diálogo de confirmação
        btnCriarPilar.setOnClickListener {
            mostrarDialogoConfirmacao()
        }

        // calendário
        editTextDataInicio.setOnClickListener {
            showDatePicker(editTextDataInicio)
        }

        editTextDataConclusao.setOnClickListener {
            showDatePicker(editTextDataConclusao)
        }
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                editText.setText(dateFormatter.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.show()
    }

    // Mostra o diálogo de confirmação
    private fun mostrarDialogoConfirmacao() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.confirmar_pilar, null)

        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Configura o botão de cancelar
        btnCancelar.setOnClickListener {
            alertDialog.dismiss()
        }

        // Configura o botão de confirmar
        btnConfirmar.setOnClickListener {
            cadastrarPilar()
            alertDialog.dismiss()
            finish()
        }

        alertDialog.show()
    }

    // Realiza o cadastro do pilar no banco de dados
    private fun cadastrarPilar() {
        val nome = editTextNome.text.toString()
        val dataInicio = editTextDataInicio.text.toString()
        val dataConclusao = editTextDataConclusao.text.toString()
        val usuarioSelecionado = spinnerResponsavel.selectedItem as? Usuario

        // Verifica se os campos obrigatórios foram preenchidos
        if (nome.isBlank() || dataInicio.isBlank() || dataConclusao.isBlank() || usuarioSelecionado == null) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        val numero = databaseHelper.obterProximoNumeroPilar()
        val sucesso = databaseHelper.cadastrarPilar(
            numero,
            nome,
            null,
            converterParaFormatoBanco(dataInicio),
            converterParaFormatoBanco(dataConclusao),
            usuarioSelecionado.id
        )

        // Exibe uma mensagem com base no resultado da operação
        if (sucesso) {
            Toast.makeText(this, "Pilar cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Erro ao cadastrar pilar.", Toast.LENGTH_SHORT).show()
        }

        Toast.makeText(this, "Responsável: ${usuarioSelecionado.nome}", Toast.LENGTH_SHORT).show()
    }

    fun converterParaFormatoBanco(dataBR: String): String {
        return try {
            val formatoBR = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formatoBanco = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val data = formatoBR.parse(dataBR)
            if (data != null) formatoBanco.format(data) else ""
        } catch (e: Exception) {
            ""
        }
    }
}

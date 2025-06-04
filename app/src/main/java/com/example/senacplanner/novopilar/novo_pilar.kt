package com.example.senacplanner.novopilar

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.data.DatabaseHelper
import com.example.senacplanner.R
import java.text.SimpleDateFormat
import java.util.*

class NovoPilarActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var editTextNome: EditText
    private lateinit var btnDataInicio: Button
    private lateinit var btnDataConclusao: Button
    private lateinit var btnCriarPilar: Button
    private lateinit var btnCancelar: Button  // botão Cancelar na tela principal

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_pilar)

        databaseHelper = DatabaseHelper(this)

        editTextNome = findViewById(R.id.editTextNome)
        btnDataInicio = findViewById(R.id.btnDataInicio)
        btnDataConclusao = findViewById(R.id.btnDataConclusao)
        btnCriarPilar = findViewById(R.id.btnCriarPilar)
        btnCancelar = findViewById(R.id.btnCancelar)

        btnCriarPilar.setOnClickListener {
            mostrarDialogoConfirmacao()
        }

        btnCancelar.setOnClickListener {
            finish() // Sai da tela ao clicar em Cancelar
        }

        btnDataInicio.setOnClickListener {
            showDatePicker(btnDataInicio)
        }

        btnDataConclusao.setOnClickListener {
            showDatePicker(btnDataConclusao)
        }
    }

    private fun showDatePicker(button: Button) {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                button.text = dateFormatter.format(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.show()
    }

    private fun mostrarDialogoConfirmacao() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.confirmar_pilar, null)

        val btnCancelarDialog = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnConfirmarDialog = dialogView.findViewById<Button>(R.id.btnConfirmar)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancelarDialog.setOnClickListener {
            alertDialog.dismiss()
        }

        btnConfirmarDialog.setOnClickListener {
            cadastrarPilar()
            alertDialog.dismiss()
            finish()
        }

        alertDialog.show()
    }

    private fun cadastrarPilar() {
        val nome = editTextNome.text.toString()
        val dataInicio = btnDataInicio.text.toString()
        val dataConclusao = btnDataConclusao.text.toString()
        val idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        if (nome.isBlank() || dataInicio == "Selecionar data" || dataConclusao == "Selecionar data") {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        val numero = databaseHelper.obterProximoNumeroPilar()
        val sucesso = databaseHelper.cadastrarPilar(
            numero,
            nome,
            null,
            converterParaDataSql(dataInicio),
            converterParaDataSql(dataConclusao),
            idUsuario
        )

        if (sucesso != -1L) {
            Toast.makeText(this, "Pilar cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Erro ao cadastrar pilar.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun converterParaDataSql(dataBR: String): String {
        return try {
            val formatoBR = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val data = formatoBR.parse(dataBR)
            val formatoSQL = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatoSQL.format(data!!)
        } catch (e: Exception) {
            ""
        }
    }
}

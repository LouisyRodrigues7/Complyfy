package com.example.senacplanner.editarpilar

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.Acoes.Type.PilarType
import com.example.senacplanner.data.DatabaseHelper
import com.example.senacplanner.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity responsável por editar e excluir pilares existentes.
 * Permite alterar nome e datas, além de deletar registros.
 */
class EditarActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var pilares: List<PilarType>
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editar_pilar)

        databaseHelper = DatabaseHelper(this)
        carregarPilaresNoSpinner()

        val btnSalvar = findViewById<Button>(R.id.btnSalvar)
        val spinnerPilar = findViewById<Spinner>(R.id.spinnerPilar)
        val btnExcluir = findViewById<Button>(R.id.btnExcluir)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        val tvDataInicio = findViewById<EditText>(R.id.tvDataInicio)
        val tvDataConclusao = findViewById<EditText>(R.id.tvDataConclusao)

        // Abre seletor de data para os campos de data
        showDatePicker(tvDataInicio)
        showDatePicker(tvDataConclusao)

        btnSalvar.setOnClickListener {
            val nomeEditado = findViewById<EditText>(R.id.etNomePilar).text.toString()
            val dataInicio = tvDataInicio.text.toString()
            val dataConclusao = tvDataConclusao.text.toString()

            val dataInicioBanco = converterParaFormatoBanco(dataInicio)
            val dataConclusaoBanco = converterParaFormatoBanco(dataConclusao)

            val pilarSelecionado = pilares[spinnerPilar.selectedItemPosition]
            databaseHelper.atualizarPilar(pilarSelecionado.id, nomeEditado, dataInicioBanco, dataConclusaoBanco)

            Toast.makeText(this, "Pilar atualizado com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnExcluir.setOnClickListener {
            mostrarDialogoConfirmacao()
        }

        btnCancelar.setOnClickListener {
            Toast.makeText(this, "Edição de Pilar cancelada!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * Exibe um diálogo de confirmação para exclusão do pilar.
     */
    private fun mostrarDialogoConfirmacao() {
        val dialogView = layoutInflater.inflate(R.layout.confirmar_excluir_pilar, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            val spinner = findViewById<Spinner>(R.id.spinnerPilar)
            val pilarSelecionado = pilares[spinner.selectedItemPosition]

            val sucesso = databaseHelper.excluirPilar(pilarSelecionado.id)
            if (sucesso) {
                Toast.makeText(this, "Pilar excluído com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Erro ao excluir o pilar.", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Carrega os pilares existentes no banco e popula o spinner.
     * Também preenche os dados de cada pilar ao selecionar.
     */
    private fun carregarPilaresNoSpinner() {
        pilares = databaseHelper.getAllPilares()

        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item_branco,
            pilares.map { it.nome }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinner = findViewById<Spinner>(R.id.spinnerPilar)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val pilarSelecionado = pilares[position]
                preencherDadosDoPilar(pilarSelecionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * Preenche os campos de nome e datas com os dados do pilar selecionado.
     */
    private fun preencherDadosDoPilar(pilar: PilarType) {
        val dados = databaseHelper.getDatasPilarById(pilar.id)
        dados?.let { (nome, dataInicio, dataConclusao) ->
            findViewById<EditText>(R.id.etNomePilar).setText(nome)
            findViewById<EditText>(R.id.tvDataInicio).setText(formatarDataParaBR(dataInicio))
            findViewById<EditText>(R.id.tvDataConclusao).setText(formatarDataParaBR(dataConclusao))
        }
    }

    /**
     * Associa um DatePickerDialog ao campo de data fornecido.
     */
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

        editText.setOnClickListener { datePicker.show() }
        editText.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) datePicker.show() }
    }

    /**
     * Converte uma data no formato do banco ("yyyy-MM-dd") para o formato brasileiro ("dd/MM/yyyy").
     */
    fun formatarDataParaBR(dataISO: String): String {
        return try {
            val timestamp = dataISO.toLongOrNull()
            val data: Date? = if (timestamp != null) {
                Date(timestamp)
            } else {
                val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                formatoEntrada.parse(dataISO)
            }
            val formatoSaida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            if (data != null) formatoSaida.format(data) else ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Converte uma data no formato BR ("dd/MM/yyyy") para o formato SQL ("yyyy-MM-dd").
     */
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

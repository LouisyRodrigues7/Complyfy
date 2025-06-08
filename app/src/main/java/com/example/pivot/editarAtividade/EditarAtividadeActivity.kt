package com.example.pivot.editarAtividade

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pivot.Acoes.Type.Usuario
import com.example.pivot.data.DatabaseHelper
import com.example.pivot.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Tela responsável por permitir a edição de uma atividade existente.
 *
 * Permite ao usuário atualizar dados como nome, datas e responsável da atividade,
 * além de excluir ou cancelar a operação.
 */
class EditarAtividadeActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private lateinit var atividade: AtividadeEdit
    private lateinit var spinnerResponsavel: Spinner

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editar_atividade)

        dbHelper = DatabaseHelper(this)

        val nomeResponsavel = intent.getStringExtra("nomeResponsavel")
        val nomePilar = intent.getStringExtra("nomePilar")
        val nomeAcao = intent.getStringExtra("nomeAcao")
        val idAtividade = intent.getIntExtra("atividadeID", -1)

        if (idAtividade != -1) {
            val dados = dbHelper.buscarAtividadePorId(idAtividade)
            dados?.let {
                preencherDadosAtividade(it)
            }
        }

        findViewById<TextView>(R.id.tvNomePilar).text = nomePilar
        findViewById<TextView>(R.id.tvNomeAcao).text = nomeAcao

        val tvDataInicio = findViewById<EditText>(R.id.tvDataInicio)
        val tvDataConclusao = findViewById<EditText>(R.id.tvDataConclusao)

        showDatePicker(tvDataInicio)
        showDatePicker(tvDataConclusao)

        val btnSalvar = findViewById<Button>(R.id.btnSalvar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        val btnExcluir = findViewById<Button>(R.id.btnExcluir)

        spinnerResponsavel = findViewById<Spinner>(R.id.spinnerResponsavel)

        val responsaveis = dbHelper.listarResponsaveis().toMutableList()
        if (nomeResponsavel.isNullOrBlank()) {
            responsaveis.add(0, Usuario(-1, "Selecione o responsável", "Tipo"))
        }

        val adapter = ArrayAdapter(this, R.layout.spinner_item, responsaveis)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerResponsavel.adapter = adapter

        nomeResponsavel?.let { nome ->
            val index = responsaveis.indexOfFirst { it.nome == nome }
            if (index != -1) {
                spinnerResponsavel.setSelection(index)
            }
        }

        btnSalvar.setOnClickListener {
            val nomeEditado = findViewById<EditText>(R.id.tvNovoNome).text.toString()
            val dataInicio = findViewById<EditText>(R.id.tvDataInicio).text.toString()
            val usuarioSelecionado = spinnerResponsavel.selectedItem as? Usuario
            val dataConclusao = findViewById<EditText>(R.id.tvDataConclusao).text.toString()
            val tvStatus = findViewById<TextView>(R.id.tvStatus).text.toString()

            val dataInicioBanco = converterParaFormatoBanco(dataInicio)
            val dataConclusaoBanco = converterParaFormatoBanco(dataConclusao)

            if (idAtividade != -1) {
                atividade = dbHelper.atualizarAtividade(
                    idAtividade,
                    nomeEditado,
                    usuarioSelecionado?.id ?: 0,
                    dataInicioBanco,
                    dataConclusaoBanco,
                    tvStatus
                )!!
                Toast.makeText(this, "Atividade atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Erro ao atualizar atividade!", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelar.setOnClickListener {
            Toast.makeText(this, "Edição de Atividade cancelada!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnExcluir.setOnClickListener {
            dbHelper.excluirAtividade(idAtividade)
            Toast.makeText(this, "Atividade Excluída!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * Preenche os campos da tela com os dados da atividade buscada.
     *
     * @param atividade Objeto da atividade que será exibido para edição.
     */
    private fun preencherDadosAtividade(atividade: AtividadeEdit) {
        val dados = dbHelper.buscarAtividadePorId(atividade.id)
        dados?.let { (id, nome, status, dataInicio, dataConclusao) ->
            findViewById<TextView>(R.id.tvNomeAtividade).setText(nome)
            findViewById<EditText>(R.id.tvDataInicio).setText(formatarDataParaBR(dataInicio))
            findViewById<EditText>(R.id.tvDataConclusao).setText(formatarDataParaBR(dataConclusao))
        }
    }

    /**
     * Mostra um `DatePickerDialog` ao clicar ou focar no campo de data.
     *
     * Facilita a seleção de datas válidas no formato esperado pela interface.
     *
     * @param editText Campo que irá exibir a data selecionada.
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

        editText.setOnClickListener {
            datePicker.show()
        }

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) datePicker.show()
        }
    }

    /**
     * Converte uma data no formato ISO ou timestamp para o formato brasileiro (dd/MM/yyyy).
     *
     * Utilizado para exibir a data de forma amigável na interface.
     *
     * @param dataISO Data no formato ISO ou como timestamp em string.
     * @return String formatada em dd/MM/yyyy ou vazia em caso de erro.
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
     * Converte uma data do formato brasileiro para o formato do banco de dados (yyyy-MM-dd).
     *
     * Essencial para manter a consistência de formato nas operações de persistência.
     *
     * @param dataBR Data no formato dd/MM/yyyy.
     * @return String no formato yyyy-MM-dd ou vazia em caso de erro.
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

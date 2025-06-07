package com.example.pivot.novopilar

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.pivot.data.DatabaseHelper
import com.example.pivot.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela responsável por permitir a criação de um novo Pilar estratégico no sistema.
 * Inclui seleção de datas e confirmação do cadastro com verificação de campos obrigatórios.
 */
class NovoPilarActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var editTextNome: EditText
    private lateinit var btnDataInicio: Button
    private lateinit var btnDataConclusao: Button
    private lateinit var btnCriarPilar: Button
    private lateinit var btnCancelar: Button

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

        // Ao clicar em "Criar Pilar", exibe caixa de confirmação
        btnCriarPilar.setOnClickListener {
            mostrarDialogoConfirmacao()
        }

        // Fecha a tela sem salvar
        btnCancelar.setOnClickListener {
            finish()
        }

        // Define ações de seleção de datas
        btnDataInicio.setOnClickListener {
            showDatePicker(btnDataInicio)
        }

        btnDataConclusao.setOnClickListener {
            showDatePicker(btnDataConclusao)
        }
    }

    /**
     * Exibe o seletor de data e atualiza o texto do botão com a data escolhida.
     *
     * @param button O botão que receberá a data escolhida.
     */
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

    /**
     * Exibe uma janela de diálogo personalizada para confirmação do cadastro do pilar.
     * Só após confirmação, a função de cadastro real é executada.
     */
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

    /**
     * Realiza o cadastro do Pilar no banco de dados, após validação dos campos obrigatórios.
     * Gera o próximo número sequencial do Pilar automaticamente.
     */
    private fun cadastrarPilar() {
        val nome = editTextNome.text.toString()
        val dataInicio = btnDataInicio.text.toString()
        val dataConclusao = btnDataConclusao.text.toString()
        val idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        // Impede tentativa de salvar campos vazios
        if (nome.isBlank() || dataInicio == "Selecionar data" || dataConclusao == "Selecionar data") {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        // Gera número sequencial do Pilar
        val numero = databaseHelper.obterProximoNumeroPilar()

        val sucesso = databaseHelper.cadastrarPilar(
            numero,
            nome,
            null, // descrição ainda não é utilizada nesta tela
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

    /**
     * Converte data do formato brasileiro (dd/MM/yyyy) para formato SQL (yyyy-MM-dd)
     * usado no banco de dados.
     */
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

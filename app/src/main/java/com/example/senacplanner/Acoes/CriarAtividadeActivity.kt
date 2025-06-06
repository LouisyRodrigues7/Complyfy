package com.example.senacplanner.Acoes

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.Acoes.Type.Usuario
import com.example.senacplanner.DatabaseHelper
import com.example.senacplanner.NotificacoesActivity
import com.example.senacplanner.R
import com.example.senacplanner.adapter.NotificacaoAdapter
import com.example.senacplanner.adapter.TipoNotificacao
import java.text.SimpleDateFormat
import java.util.*

class CriarAtividadeActivity : AppCompatActivity() {

    private lateinit var nomeEditText: EditText
    private lateinit var descricaoEditText: EditText
    private lateinit var dataInicioButton: Button
    private lateinit var dataConclusaoButton: Button
    private lateinit var statusSpinner: Spinner
    private lateinit var salvarButton: Button
    private lateinit var spinnerResponsavel: Spinner
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var cancelarButton: Button


    private var dataInicio: String = ""
    private var dataConclusao: String = ""
    private var acaoId: Int = -1
    private var usuarioId: Int = -1
    private var idUsuario: Int = -1
    private var usuarioTipo: String = ""
    private var pilarNome: String = ""
    private var atividadeId: Long = -1

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
        spinnerResponsavel = findViewById(R.id.spinnerResponsavel)

        dbHelper = DatabaseHelper(this)

        acaoId = intent.getIntExtra("ACAO_ID", -1)
        usuarioId = intent.getIntExtra("ID_USUARIO", -1)
        pilarNome = intent.getStringExtra("PILAR_NOME").toString()
        usuarioTipo = intent.getStringExtra("TIPO_USUARIO").toString()

        val tipoUsuario = dbHelper.obterUsuario(usuarioId)
        Log.d("USUARIO ID", (tipoUsuario?.tipo ?: '-').toString())
        val responsaveis = dbHelper.listarResponsaveis()
        val adapter = ArrayAdapter(this, R.layout.spinner_item, responsaveis)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerResponsavel.adapter = adapter

        if (acaoId == -1 || usuarioId == -1) {
            Toast.makeText(this, "Erro ao obter contexto da ação ou usuário.", Toast.LENGTH_SHORT)
                .show()
            finish()
        }

        statusSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.status_array,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        dataInicioButton.setOnClickListener {
            showDatePicker { date ->
                dataInicio = date
                dataInicioButton.text = formatarDataParaBR(date)
            }
        }

        dataConclusaoButton.setOnClickListener {
            if (dataInicio.isBlank()) {
                Toast.makeText(this, "Selecione a data de início primeiro.", Toast.LENGTH_SHORT).show()
            } else {
                mostrarDatePickerSaida(this, dataInicio) { dataValida ->
                    dataConclusao = dataValida
                    dataConclusaoButton.text = formatarDataParaBR(dataValida)
                }
            }
        }


        salvarButton.setOnClickListener {
            val nome = nomeEditText.text.toString()
            val descricao = descricaoEditText.text.toString()
            val status = statusSpinner.selectedItem.toString()
            val usuarioSelecionado = spinnerResponsavel.selectedItem as? Usuario

            if (nome.isBlank() || dataInicio.isBlank()) {
                Toast.makeText(this, "Preencha os campos obrigatórios.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (usuarioSelecionado == null) {
                Toast.makeText(this, "Selecione um responsável.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val aprovado = usuarioTipo == "Coordenador"

            atividadeId = dbHelper.inserirAtividade(
                acaoId,
                nome,
                descricao,
                status,
                dataInicio,
                dataConclusao,
                usuarioId,
                usuarioSelecionado.id,
                aprovado
            )

            if (!aprovado) {
                dbHelper.criarNotificacaoParaCoordenador(
                    "Nova atividade ($nome) aguardando aprovação!!",
                    atividadeId.toInt(),
                    TipoNotificacao.APROVACAO_ATIVIDADE
                )
                Toast.makeText(this, "Atividade aguardando aprovação!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Atividade criada e aprovada com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        cancelarButton = findViewById(R.id.btnCancelar)
        
        cancelarButton.setOnClickListener {
            finish()  // Fecha a activity e volta para a tela anterior
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

    private fun formatarDataParaBR(dataISO: String): String {
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



    fun mostrarDatePickerSaida(context: Context, dataEntrada: String, onDataValida: (String) -> Unit) {
        val calendario = Calendar.getInstance()

        val datePicker = DatePickerDialog(context, { _, ano, mes, dia ->
            val calendarioSaida = Calendar.getInstance()
            calendarioSaida.set(ano, mes, dia)

            val formatoISO = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dataSaida = formatoISO.format(calendarioSaida.time)

            try {
                val dataEntradaDate = formatoISO.parse(dataEntrada)
                val dataSaidaDate = formatoISO.parse(dataSaida)

                if (dataSaidaDate.before(dataEntradaDate)) {
                    AlertDialog.Builder(context)
                        .setTitle("Data inválida")
                        .setMessage("A data de saída não pode ser anterior à data de entrada.")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            mostrarDatePickerSaida(context, dataEntrada, onDataValida) // Reabrir para nova escolha
                        }
                        .show()
                } else {
                    onDataValida(dataSaida)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH))

        datePicker.show()
    }

}
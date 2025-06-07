package com.example.senacplanner.Acoes

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.R
import com.example.senacplanner.data.DatabaseHelper

/**
 * Activity responsável por editar e excluir uma ação vinculada a um pilar.
 *
 * Recebe o nome do pilar, nome da ação e o ID da ação via Intent.
 * Permite alterar o nome da ação, salvar a alteração, excluir a ação
 * ou cancelar a edição.
 */
class EditarAcaoActivity: AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private var pilarNome: String? = null
    private var acaoNome: String? = null
    private var acaoId: Int? = 0

    /**
     * Inicializa a activity, recupera dados da Intent e configura
     * os botões para salvar, excluir e cancelar edição da ação.
     */
    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editar_acao)
        databaseHelper = DatabaseHelper(this)

        pilarNome = intent.getStringExtra("nomePilar")
        acaoNome = intent.getStringExtra("nomeAcao")
        acaoId = intent.getIntExtra("ACAO_ID", -1)

        val btnSalvar = findViewById<Button>(R.id.btnSalvar)
        val btnExcluir = findViewById<Button>(R.id.btnExcluir)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        val tvAcao = findViewById<TextView>(R.id.tvNomeAcao)
        val tvPilar = findViewById<TextView>(R.id.tvNomePilar)

        tvPilar.text = pilarNome
        tvAcao.text = acaoNome


        btnSalvar.setOnClickListener {
            val nomeEditado = findViewById<EditText>(R.id.etNovoNome).text.toString()
            databaseHelper.atualizarAcao(acaoId?.toInt() ?: 0, nomeEditado)
            Toast.makeText(this, "Ação atualizada com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnExcluir.setOnClickListener {
            abrirDialogConfirmarExclusao(acaoId?.toInt() ?: 0)
        }

        btnCancelar.setOnClickListener {
            Toast.makeText(this, "Edição de Ação cancelada!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }


    /**
     * Exibe um diálogo de confirmação para exclusão da ação.
     *
     * @param acaoId ID da ação a ser excluída no banco de dados.
     */
    @SuppressLint("SetTextI18n")
    private fun abrirDialogConfirmarExclusao(acaoId: Int) {

        val dialogView = LayoutInflater.from(this).inflate(R.layout.confirmar_excluir_acao, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)


        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            databaseHelper.excluirAcao(acaoId)
            Toast.makeText(this, "Ação excluida com sucesso!!", Toast.LENGTH_SHORT).show()
            finish()
        }

        dialog.show()
    }


}
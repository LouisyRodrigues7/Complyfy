package com.example.senacplanner.Acoes

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.senacplanner.Acoes.Type.AcaoComAtividades
import com.example.senacplanner.DatabaseHelper
import com.example.senacplanner.R
import com.example.senacplanner.adapter.TipoNotificacao
import com.example.senacplanner.editarAtividade.EditarAtividadeActivity
import com.example.senacplanner.model.Notificacao
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AcaoPageFragment : Fragment() {
    private var pilarNome: String? = null
    private var idUsuario: Int? = 0

    companion object {
        fun newInstance(acao: AcaoComAtividades, pilarNome: String?, idUsuario: Int): AcaoPageFragment {
            val fragment = AcaoPageFragment()
            val args = Bundle()
            args.putSerializable("acao_dados", acao)
            args.putString("PILAR_NOME", pilarNome)
            args.putInt("ID_USUARIO", idUsuario)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var acao: AcaoComAtividades

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        acao = arguments?.getSerializable("acao_dados") as AcaoComAtividades
        pilarNome = arguments?.getString("PILAR_NOME")
        idUsuario = arguments?.getInt("ID_USUARIO")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_acao_page, container, false)

        val textAcao = view.findViewById<TextView>(R.id.acaoPrincipal)
        val layoutAtividades = view.findViewById<LinearLayout>(R.id.layoutAtividades)
        val databaseHelper = DatabaseHelper(requireContext())
        textAcao.text = acao.acao.nome

        for (atividade in acao.atividades) {
            val itemView = inflater.inflate(R.layout.item_atividade, layoutAtividades, false)


            (itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                bottomMargin = 16
            }

            val tipoUsuario = databaseHelper.obterUsuario(idUsuario?.toInt() ?: 0)
            val checkboxButton = itemView.findViewById<CheckBox>(R.id.checkbox)
            val textAtividade = itemView.findViewById<TextView>(R.id.textAtividade)

            textAtividade.text = atividade.nome
            val responsavel = databaseHelper.buscarResponsavelPorAtividade(atividade.id)

            val inicialResponsavel = itemView.findViewById<TextView>(R.id.inicialResponsavel)

            responsavel?.let {
                val nomes = it.nome.trim().split("\\s+".toRegex())
                val primeiraInicial = nomes.firstOrNull()?.firstOrNull()?.uppercaseChar() ?: '-'
                val segundaInicial = nomes.lastOrNull()?.firstOrNull()?.uppercaseChar() ?: '-'
                inicialResponsavel.text = "$primeiraInicial$segundaInicial"
                inicialResponsavel.visibility = View.VISIBLE
            } ?: run {
                inicialResponsavel.visibility = View.GONE
            }

            layoutAtividades.addView(itemView)

            checkboxButton.isChecked = atividade.status == "Finalizada"

            checkboxButton.setOnCheckedChangeListener { buttonView, isChecked ->
                val usuarioLogadoId = tipoUsuario?.id
                val responsavel = databaseHelper.buscarResponsavelPorAtividade(atividade.id)

                if (tipoUsuario?.tipo == "Coordenador") {
                    val novoStatus = if (isChecked) "Finalizada" else "Em andamento"
                    Log.d("ATIVIDADE ID CHECKBOX", atividade.id.toString())
                    databaseHelper.atualizarStatus(atividade.id, novoStatus)
                    Log.d("ATIVIDADE STATUS CHECKBOX", atividade.status)
                } else if (tipoUsuario?.tipo == "Apoio") {
                    buttonView.isChecked = !isChecked

                    if (usuarioLogadoId != null && responsavel?.id == usuarioLogadoId) {
                        abrirDialogPedidoStatus(atividade.id)
                    } else {
                        Toast.makeText(
                            context,
                            "Apenas o responsável pode solicitar a mudança de status.",
                            Toast.LENGTH_SHORT
                        ).show()

                        checkboxButton.alpha = 0.5f
                        checkboxButton.postDelayed({
                            checkboxButton.alpha = 1.0f
                        }, 1000)
                    }
                }
            }


            itemView.setOnClickListener {
                val intent = Intent(requireContext(), EditarAtividadeActivity::class.java)
                intent.putExtra("atividadeID", atividade.id)
                intent.putExtra("nomeAcao", acao.acao.nome)
                intent.putExtra("nomeResponsavel", responsavel?.nome)
                intent.putExtra("nomePilar", pilarNome)
                startActivity(intent)
            }
        }

        return view
    }

    @SuppressLint("SetTextI18n")
    private fun abrirDialogPedidoStatus(atividadeId: Int) {
        val db = DatabaseHelper(requireContext())

        val atividade = db.buscarAtividadePorId(atividadeId)
        val usuarioResponsavel = db.obterUsuario(atividade?.responsavel_id ?: 0)
        val acao = db.buscarAcaoPorId(atividade?.acao_id ?: 0 )
        val pilar = db.buscarPilarPorId(acao?.pilar_id ?: 0)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.autorizar_conclusao, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val tvPilar = dialogView.findViewById<TextView>(R.id.tvNomePilar)
        val tvAcao = dialogView.findViewById<TextView>(R.id.tvNomeAcao)
        val tvNomeAtividade = dialogView.findViewById<TextView>(R.id.tvNomeAtividade)
        val tvResponsavel = dialogView.findViewById<TextView>(R.id.tvNomeResponsavel)
        val tvDataInicio = dialogView.findViewById<TextView>(R.id.tvDataInicio)
        val tvDataConclusao = dialogView.findViewById<TextView>(R.id.tvDataConclusao)
        val etComentario = dialogView.findViewById<EditText>(R.id.etComentario)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnEnviar = dialogView.findViewById<Button>(R.id.btnEnviar)

        tvPilar.text = "Pilar: ${pilar?.nome ?: "-"}"
        tvAcao.text = "Ação: ${acao?.nome ?: "-"}"
        tvNomeAtividade.text = atividade?.nome
        tvResponsavel.text = usuarioResponsavel?.nome ?: "-"
        tvDataInicio.text = formatarDataParaBR(atividade?.data_inicio ?: "-" )
        tvDataConclusao.text = formatarDataParaBR(atividade?.data_conclusao ?: "-")

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnEnviar.setOnClickListener {
            db.criarNotificacaoParaCoordenador(
                "Atividade (${atividade?.nome}) aguardando conclusão!!",
                atividadeId,
                TipoNotificacao.CONCLUSAO_STATUS
            )
            dialog.dismiss()
        }

        dialog.show()
    }

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

}
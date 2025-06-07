package com.example.senacplanner.Acoes

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.senacplanner.Acoes.Type.AcaoComAtividades
import com.example.senacplanner.data.DatabaseHelper
import com.example.senacplanner.R
import com.example.senacplanner.adapter.TipoNotificacao
import com.example.senacplanner.editarAtividade.EditarAtividadeActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragmento que exibe uma ação e suas atividades relacionadas,
 * permitindo visualizar, editar e alterar o status das atividades.
 *
 * Exibe a lista de atividades, controla permissões de alteração
 * do status baseado no tipo do usuário (Coordenador ou Apoio)
 * e abre telas para edição das ações e atividades.
 */
class AcaoPageFragment : Fragment() {
    private var pilarNome: String? = null
    private var idUsuario: Int? = 0
    private var acaoId: Int = 0

    companion object {
        /**
         * Cria uma nova instância do fragmento configurada com os dados da ação,
         * nome do pilar, ID do usuário e ID da ação.
         *
         * @param acao Objeto [AcaoComAtividades] contendo a ação e suas atividades.
         * @param pilarNome Nome do pilar relacionado (pode ser nulo).
         * @param idUsuario ID do usuário atual.
         * @param acaoId ID da ação atual.
         * @return Nova instância do [AcaoPageFragment].
         */
        fun newInstance(acao: AcaoComAtividades, pilarNome: String?, idUsuario: Int, acaoId: Int): AcaoPageFragment {
            val fragment = AcaoPageFragment()
            val args = Bundle()
            args.putSerializable("acao_dados", acao)
            args.putString("PILAR_NOME", pilarNome)
            args.putInt("ID_USUARIO", idUsuario)
            args.putInt("ACAO_ID", acaoId)
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
        acaoId = arguments?.getInt("ACAO_ID") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_acao_page, container, false)


        val iconEdit = view.findViewById<ImageView>(R.id.ivEdit)
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

        iconEdit.setOnClickListener {
            val intent = Intent(requireContext(), EditarAcaoActivity::class.java)
            intent.putExtra("ACAO_ID", acao.acao.id)
            intent.putExtra("nomeAcao", acao.acao.nome)
            intent.putExtra("nomePilar", pilarNome)
            startActivity(intent)
        }

        return view
    }

    /**
     * Abre um diálogo para o usuário do tipo Apoio solicitar mudança de status da atividade.
     *
     * @param atividadeId ID da atividade que terá o pedido de alteração.
     */
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

    /**
     * Formata uma data ISO (timestamp ou yyyy-MM-dd) para o formato brasileiro dd/MM/yyyy.
     *
     * @param dataISO Data no formato ISO (timestamp em string ou yyyy-MM-dd).
     * @return Data formatada para dd/MM/yyyy, ou string vazia se não for possível formatar.
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



}
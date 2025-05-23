package com.example.senacplanner.Acoes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.senacplanner.Acoes.Type.AcaoComAtividades
import com.example.senacplanner.DatabaseHelper
import com.example.senacplanner.R
import com.example.senacplanner.editarAtividade.EditarAtividadeActivity

class AcaoPageFragment : Fragment() {
    private var pilarNome: String? = null

    companion object {
        fun newInstance(acao: AcaoComAtividades, pilarNome: String?): AcaoPageFragment {
            val fragment = AcaoPageFragment()
            val args = Bundle()
            args.putSerializable("acao_dados", acao)
            args.putString("PILAR_NOME", pilarNome)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var acao: AcaoComAtividades

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        acao = arguments?.getSerializable("acao_dados") as AcaoComAtividades
        pilarNome = arguments?.getString("PILAR_NOME")
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

            val radioButton = itemView.findViewById<RadioButton>(R.id.radioButton)
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

}
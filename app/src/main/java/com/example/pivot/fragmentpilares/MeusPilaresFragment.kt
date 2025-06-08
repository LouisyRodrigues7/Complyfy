package com.example.pivot.fragmentpilares

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pivot.data.DatabaseHelper
import com.example.pivot.Acoes.ListaAtividades
import com.example.pivot.R

/**
 * Fragmento que exibe os pilares relacionados ao usuário logado.
 *
 * Este fragmento é responsável por listar visualmente os pilares que possuem
 * atividades associadas ao usuário atual. Cada pilar é exibido como um item clicável
 * que leva à lista de atividades daquele pilar.
 */
class MeusPilaresFragment : Fragment() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var layout: LinearLayout
    private var idUsuario: Int = -1
    private var usuarioTipo: String = ""
    private var usuarioNome: String = ""

    /**
     * Cria a view do fragmento e inicializa os dados do usuário
     * a partir da intent da activity que o contém.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lista_pilares, container, false)
        layout = view.findViewById<LinearLayout>(R.id.layoutListaPilares)

        databaseHelper = DatabaseHelper(requireContext())
        idUsuario = requireActivity().intent.getIntExtra("ID_USUARIO", -1)
        usuarioTipo = requireActivity().intent.getStringExtra("TIPO_USUARIO").toString()
        usuarioNome = requireActivity().intent.getStringExtra("NOME_USUARIO").toString()

        carregarPilaresDoUsuario(inflater)
        return view
    }

    /**
     * Recarrega os pilares sempre que o fragmento voltar a estar visível.
     */
    override fun onResume() {
        super.onResume()
        carregarPilaresDoUsuario(layoutInflater)
    }

    /**
     * Busca os pilares com atividades associadas ao usuário e os exibe dinamicamente.
     *
     * Cada item de pilar é clicável e redireciona para a tela de lista de atividades.
     */
    private fun carregarPilaresDoUsuario(inflater: LayoutInflater) {
        layout.removeAllViews()

        val pilaresDoUsuario = databaseHelper.getPilaresComAtividadesDoUsuario(idUsuario)

        pilaresDoUsuario.forEach { pilar ->
            val item = inflater.inflate(R.layout.item_pilar_grande, layout, false)
            item.findViewById<TextView>(R.id.numeroPilarGrande).text = pilar.numero.toString()
            item.findViewById<TextView>(R.id.textoPilarGrande).text = pilar.nome

            item.setOnClickListener {
                val intent = Intent(requireContext(), ListaAtividades::class.java).apply {
                    putExtra("PILAR_ID", pilar.id)
                    putExtra("PILAR_NUMERO", pilar.numero)
                    putExtra("PILAR_NOME", pilar.nome)
                    putExtra("ID_USUARIO", idUsuario)
                    putExtra("TIPO_USUARIO", usuarioTipo)
                    putExtra("NOME_USUARIO", usuarioNome)
                }
                startActivity(intent)
            }

            layout.addView(item)
        }
    }
}

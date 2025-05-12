package com.example.senacplanner.fragmentpilares

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.senacplanner.DatabaseHelper
import com.example.senacplanner.Acoes.ListaAtividades
import com.example.senacplanner.R

class MeusPilaresFragment : Fragment() {
    private lateinit var databaseHelper: DatabaseHelper
    /*private val pilaresUsuario = listOf(
        "Avaliação de riscos",
        "Controles internos",
        "Código de conduta e ética e Políticas de Compliance"
    )*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lista_pilares, container, false)
        val layout = view.findViewById<LinearLayout>(R.id.layoutListaPilares)

        databaseHelper = DatabaseHelper(requireContext());
        val idUsuario = requireActivity().intent.getIntExtra("ID_USUARIO", -1)
        val pilaresDoUsuario = databaseHelper.getPilaresByUsuarioId(idUsuario);

        pilaresDoUsuario.forEachIndexed { index, pilar ->
            val item = inflater.inflate(R.layout.item_pilar_grande, layout, false)
            item.findViewById<TextView>(R.id.numeroPilarGrande).text = pilar.numero.toString()
            val textoPilar = item.findViewById<TextView>(R.id.textoPilarGrande)
            textoPilar.text = pilar.nome

                item.setOnClickListener {
                    val intent = Intent(requireContext(), ListaAtividades::class.java)
                    intent.putExtra("PILAR_ID", pilar.id)
                    intent.putExtra("PILAR_NUMERO", pilar.numero)
                    intent.putExtra("PILAR_NOME", pilar.nome)
                    startActivity(intent)
                }

            layout.addView(item)
        }

        return view
    }
}

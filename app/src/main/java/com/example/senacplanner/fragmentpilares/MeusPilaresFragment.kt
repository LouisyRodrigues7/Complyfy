package com.example.senacplanner.fragmentpilares

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.senacplanner.Pilares.ListaAtividades
import com.example.senacplanner.R

class MeusPilaresFragment : Fragment() {

    private val pilaresUsuario = listOf(
        "Avaliação de riscos",
        "Controles internos",
        "Código de conduta e ética e Políticas de Compliance"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lista_pilares, container, false)
        val layout = view.findViewById<LinearLayout>(R.id.layoutListaPilares)

        pilaresUsuario.forEachIndexed { index, nome ->
            val item = inflater.inflate(R.layout.item_pilar_grande, layout, false)
            item.findViewById<TextView>(R.id.numeroPilarGrande).text = "${index + 1}"
            val textoPilar = item.findViewById<TextView>(R.id.textoPilarGrande)
            textoPilar.text = nome

            if (nome == "Avaliação de riscos") {
                item.setOnClickListener {
                    val intent = Intent(requireContext(), ListaAtividades::class.java)
                    startActivity(intent)
                }
            }

            layout.addView(item)
        }

        return view
    }
}
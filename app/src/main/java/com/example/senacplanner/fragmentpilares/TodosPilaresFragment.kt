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
import com.example.senacplanner.Pilares.ListaAtividades
import com.example.senacplanner.R

class TodosPilaresFragment : Fragment() {
    private lateinit var databaseHelper: DatabaseHelper
    // Lista simulada de todos os pilares do banco de dados
    /*private val todosPilares = listOf(
        "Avaliação de riscos",
        "Código de conduta e ética e Políticas de Compliance",
        "Controles internos",
        "Investigações Internas",
        "Transparência"
    )*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lista_pilares, container, false)
        val layout = view.findViewById<LinearLayout>(R.id.layoutListaPilares)

        databaseHelper = DatabaseHelper(requireContext());

        val pilares = databaseHelper.getAllPilares()

        pilares.forEachIndexed { index, pilar ->
            val item = inflater.inflate(R.layout.item_pilar_grande, layout, false)
            item.findViewById<TextView>(R.id.numeroPilarGrande).text = pilar.numero.toString()
            item.findViewById<TextView>(R.id.textoPilarGrande).text = pilar.nome

            if (pilar.nome == "Avaliação de Riscos") {
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

package com.example.senacplanner.fragmentpilares

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.senacplanner.DatabaseHelper
import com.example.senacplanner.LoginActivity
import com.example.senacplanner.Pilares.ListaAtividades
import com.example.senacplanner.R

class TodosPilaresFragment : Fragment() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var layout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lista_pilares, container, false)
        layout = view.findViewById(R.id.layoutListaPilares)

        val idUsuario = requireActivity().intent.getIntExtra("ID_USUARIO", -1)

        if (idUsuario == -1) {
            Toast.makeText(requireContext(), "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
            return view
        }

        databaseHelper = DatabaseHelper(requireContext())

        carregarPilares()

        return view
    }

    override fun onResume() {
        super.onResume()
        carregarPilares()
    }

    private fun carregarPilares() {
        layout.removeAllViews()
        val pilares = databaseHelper.getAllPilares()

        pilares.forEach { pilar ->
            val item = layoutInflater.inflate(R.layout.item_pilar_grande, layout, false)
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
    }
}


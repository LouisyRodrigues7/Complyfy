package com.example.senacplanner.fragmentpilares

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.senacplanner.data.DatabaseHelper
import com.example.senacplanner.Acoes.ListaAtividades
import com.example.senacplanner.R

class MeusPilaresFragment : Fragment() {
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var layout: LinearLayout
    private var idUsuario: Int = -1
    private var usuarioTipo: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lista_pilares, container, false)
        layout = view.findViewById<LinearLayout>(R.id.layoutListaPilares)

        databaseHelper = DatabaseHelper(requireContext());
        idUsuario = requireActivity().intent.getIntExtra("ID_USUARIO", -1)
        usuarioTipo = requireActivity().intent.getStringExtra("TIPO_USUARIO").toString()
        carregarPilaresDoUsuario(inflater)
        return view
    }

    override fun onResume() {
        super.onResume()
        carregarPilaresDoUsuario(layoutInflater)
    }

    private fun carregarPilaresDoUsuario(inflater: LayoutInflater) {
        layout.removeAllViews()

        val pilaresDoUsuario = databaseHelper.getPilaresComAtividadesDoUsuario(idUsuario)
        pilaresDoUsuario.forEach { pilar ->
            val item = inflater.inflate(R.layout.item_pilar_grande, layout, false)
            item.findViewById<TextView>(R.id.numeroPilarGrande).text = pilar.numero.toString()
            item.findViewById<TextView>(R.id.textoPilarGrande).text = pilar.nome

            item.setOnClickListener {
                val intent = Intent(requireContext(), ListaAtividades::class.java)
                intent.putExtra("PILAR_ID", pilar.id)
                intent.putExtra("PILAR_NUMERO", pilar.numero)
                intent.putExtra("PILAR_NOME", pilar.nome)
                intent.putExtra("ID_USUARIO", idUsuario)
                intent.putExtra("TIPO_USUARIO", usuarioTipo)
                startActivity(intent)
            }

            layout.addView(item)
        }
    }



}

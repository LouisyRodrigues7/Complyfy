package com.example.senacplanner.apoio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.senacplanner.R
import com.example.senacplanner.Pilar  // Verifique o pacote correto do modelo
import com.example.senacplanner.adapter.PilarAdapter

class MeusPilaresApoioFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PilarAdapter

    // Pilares do apoio
    private val pilaresApoio = listOf(
        Pilar("Transparência", 1),
        Pilar("Investigações Internas", 2)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_lista_pilares, container, false)

        // Configura o RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewPilaresApoio)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Passa a lista de pilares para o adapter
        adapter = PilarAdapter(pilaresApoio) { pilar ->
            // Aqui você pode definir o que acontece ao clicar, sem navegação
            println("Clicou no pilar: ${pilar.titulo}")  // Exemplo de ação ao clicar
        }
        recyclerView.adapter = adapter

        return view
    }
}
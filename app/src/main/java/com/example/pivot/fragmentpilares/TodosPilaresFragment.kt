package com.example.pivot.fragmentpilares

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.pivot.data.DatabaseHelper
import com.example.pivot.ui.LoginActivity
import com.example.pivot.Acoes.ListaAtividades
import com.example.pivot.R

/**
 * Fragmento que lista todos os pilares cadastrados no sistema,
 * independentemente do usuário criador ou responsável.
 *
 * Esse fragmento é utilizado principalmente por usuários com
 * perfil de visualização ampla (como gestores ou coordenadores).
 */
class TodosPilaresFragment : Fragment() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var layout: LinearLayout
    private var idUsuario: Int = -1
    private var usuarioTipo: String = ""
    private var usuarioNome: String = ""

    /**
     * Infla a interface do fragmento e recupera os dados do usuário da intent da activity.
     *
     * Se o ID do usuário for inválido, redireciona para a tela de login.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lista_pilares, container, false)
        layout = view.findViewById(R.id.layoutListaPilares)

        idUsuario = requireActivity().intent.getIntExtra("ID_USUARIO", -1)
        usuarioTipo = requireActivity().intent.getStringExtra("TIPO_USUARIO").toString()
        usuarioNome = requireActivity().intent.getStringExtra("NOME_USUARIO").toString()

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

    /**
     * Atualiza a lista de pilares toda vez que o fragmento volta ao foco.
     */
    override fun onResume() {
        super.onResume()
        carregarPilares()
    }

    /**
     * Busca todos os pilares do banco e exibe dinamicamente na interface.
     *
     * Cada item é clicável e direciona para a `ListaAtividades` do pilar correspondente.
     * A flag `VISUALIZACAO_GERAL` indica que a navegação é feita a partir de uma visão ampla.
     */
    private fun carregarPilares() {
        layout.removeAllViews()
        val pilares = databaseHelper.getAllPilares()

        pilares.forEach { pilar ->
            val item = layoutInflater.inflate(R.layout.item_pilar_grande, layout, false)
            item.findViewById<TextView>(R.id.numeroPilarGrande).text = pilar.numero.toString()
            item.findViewById<TextView>(R.id.textoPilarGrande).text = pilar.nome

            item.setOnClickListener {
                val intent = Intent(requireContext(), ListaAtividades::class.java).apply {
                    putExtra("PILAR_ID", pilar.id)
                    putExtra("PILAR_NUMERO", pilar.numero)
                    putExtra("PILAR_NOME", pilar.nome)
                    putExtra("VISUALIZACAO_GERAL", true)
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

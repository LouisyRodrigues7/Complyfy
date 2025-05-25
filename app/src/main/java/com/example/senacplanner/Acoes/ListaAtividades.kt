package com.example.senacplanner.Acoes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.senacplanner.Acoes.Type.AcaoComAtividades
import com.example.senacplanner.DatabaseHelper
import com.example.senacplanner.R
import com.google.android.material.floatingactionbutton.FloatingActionButton


class ListaAtividades : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var databaseHelper: DatabaseHelper
    private var pilarId: Int = -1
    private var pilarNumero: Int = -1
    private var pilarNome: String? = null
    private var idUsuario: Int = -1
    private var idAcao: Int = -1
    private lateinit var acoes: List<AcaoComAtividades>
    private var visualizacaoGeral: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_atividades)

        pilarId = intent.getIntExtra("PILAR_ID", -1)
        pilarNumero = intent.getIntExtra("PILAR_NUMERO", -1)
        pilarNome = intent.getStringExtra("PILAR_NOME")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)
        visualizacaoGeral = intent.getBooleanExtra("VISUALIZACAO_GERAL", false)

        if (pilarId == -1) return

        databaseHelper = DatabaseHelper(this)
        viewPager = findViewById(R.id.viewPager)

        carregarDados()

        val fabNovaAtividade = findViewById<FloatingActionButton>(R.id.fabNovaAtividade)
        fabNovaAtividade.setOnClickListener {
            if (idAcao == -1 || idUsuario == -1) {
                Log.w("ListaAtividades", "Tentativa de criar atividade sem acao ou usuario definidos")
                Toast.makeText(this, "Não há ação selecionada para adicionar uma atividade.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, CriarAtividadeActivity::class.java)
            intent.putExtra("ACAO_ID", idAcao)
            intent.putExtra("USUARIO_ID", idUsuario)
            startActivity(intent)
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.title = "Pilar $pilarNumero - $pilarNome"
        toolbar.setNavigationOnClickListener {
            finish()
        }

    }

    override fun onResume() {
        super.onResume()
        carregarDados()
    }

    private fun carregarDados() {
       acoes = if (visualizacaoGeral) {
            databaseHelper.buscarAcoesEAtividadesPorPilar(pilarId)
        } else {
            databaseHelper.buscarAcoesEAtividadesDoUsuarioPorPilar(pilarId, idUsuario)
       }

        val currentItem = if (::viewPager.isInitialized) viewPager.currentItem else 0
        viewPager.adapter = AcoesPagerAdapter(this, acoes, pilarNome)
        viewPager.setCurrentItem(currentItem, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position < acoes.size) {
                    idAcao = acoes[position].acao.id
                }
            }
        })

        if (acoes.isNotEmpty()) {
            val acaoAtual = acoes[currentItem]
            idAcao = acaoAtual.acao.id
        }
    }
}

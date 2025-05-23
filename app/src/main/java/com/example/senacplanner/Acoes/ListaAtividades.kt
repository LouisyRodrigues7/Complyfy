package com.example.senacplanner.Acoes

import android.content.Intent
import android.os.Bundle
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
    private lateinit var acoes: List<AcaoComAtividades>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_atividades)

        pilarId = intent.getIntExtra("PILAR_ID", -1)
        pilarNumero = intent.getIntExtra("PILAR_NUMERO", -1)
        pilarNome = intent.getStringExtra("PILAR_NOME")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)

        if (pilarId == -1) return

        databaseHelper = DatabaseHelper(this)
        viewPager = findViewById(R.id.viewPager)

        val fabNovaAtividade = findViewById<FloatingActionButton>(R.id.fabNovaAtividade)
        fabNovaAtividade.setOnClickListener {
            val intent = Intent(this, CriarAtividadeActivity::class.java)
            intent.putExtra("ACAO_ID", pilarId)
            intent.putExtra("USUARIO_ID", idUsuario)
            startActivity(intent)
        }

        carregarDados()

        acoes = if (idUsuario != -1) {
            databaseHelper.buscarAcoesEAtividadesDoUsuarioPorPilar(pilarId, idUsuario)
        } else {
            databaseHelper.buscarAcoesEAtividadesPorPilar(pilarId)
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
        val acoes = if (idUsuario != -1) {
            databaseHelper.buscarAcoesEAtividadesDoUsuarioPorPilar(pilarId, idUsuario)
        } else {
            databaseHelper.buscarAcoesEAtividadesPorPilar(pilarId)
        }

        val currentItem = if (::viewPager.isInitialized) viewPager.currentItem else 0
        viewPager.adapter = AcoesPagerAdapter(this, acoes, pilarNome)
        viewPager.setCurrentItem(currentItem, false)
    }
}

package com.example.senacplanner.Acoes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.senacplanner.Acoes.Type.AcaoComAtividades
import com.example.senacplanner.NotificacoesActivity
import com.example.senacplanner.data.DatabaseHelper
import com.example.senacplanner.R
import com.example.senacplanner.ui.CoordenadorActivity
import com.example.senacplanner.ui.GraficosActivity
import com.example.senacplanner.ui.LoginActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.content.ContextCompat
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence

/**

Activity que exibe a lista de ações e suas respectivas atividades vinculadas a um pilar.

Permite navegação entre ações usando ViewPager2, criação de novas ações e atividades,

e acesso a funcionalidades auxiliares como gráficos, notificações e logout.
 */
class ListaAtividades : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var databaseHelper: DatabaseHelper
    private var pilarId: Int = -1
    private var pilarNumero: Int = -1
    private var pilarNome: String? = null
    private var idUsuario: Int = -1
    private var idAcao: Int = -1
    private var acoes: List<AcaoComAtividades> = emptyList()
    private var visualizacaoGeral: Boolean = false
    private var usuarioTipo: String? = ""
    private var usuarioNome: String = ""
    private lateinit var fabAdicionar: FloatingActionButton

    /**
     * Atualiza a lista de ações e atividades exibidas no ViewPager.
     * Se estiver em visualização geral, carrega todas as ações do pilar,
     * caso contrário, carrega somente as ações vinculadas ao usuário.
     * Após atualizar a lista, posiciona o ViewPager na última ação.
     */

    private fun atualizarListaDeAcoes() {
        acoes = if (visualizacaoGeral) {
            databaseHelper.buscarAcoesEAtividadesPorPilar(pilarId)
        } else {
            databaseHelper.buscarAcoesEAtividadesDoUsuarioPorPilar(pilarId, idUsuario)
        }
        viewPager.adapter = AcoesPagerAdapter(this, acoes, pilarNome, idUsuario, idAcao)
        if (acoes.isNotEmpty()) {
            val ultimaPosicao = acoes.size - 1
            viewPager.setCurrentItem(ultimaPosicao, false)
            idAcao = acoes[ultimaPosicao].acao.id
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_atividades)

        val btnGraficos = findViewById<ImageView>(R.id.btnGraficos)
        val btnHome = findViewById<ImageView>(R.id.btnHome)
        val btnNotificacoes = findViewById<ImageView>(R.id.btnNotificacoes)
        val btnAcoes = findViewById<ImageView>(R.id.btnAcoes)
        val fab = findViewById<FloatingActionButton>(R.id.fabAdicionar)
        fab.setColorFilter(ContextCompat.getColor(this, android.R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)

        pilarId = intent.getIntExtra("PILAR_ID", -1)
        pilarNumero = intent.getIntExtra("PILAR_NUMERO", -1)
        pilarNome = intent.getStringExtra("PILAR_NOME")
        idUsuario = intent.getIntExtra("ID_USUARIO", -1)
        usuarioTipo = intent.getStringExtra("TIPO_USUARIO").toString()
        visualizacaoGeral = intent.getBooleanExtra("VISUALIZACAO_GERAL", false)
        usuarioNome = intent.getStringExtra("NOME_USUARIO").toString()

        btnGraficos.setOnClickListener {
            val intent = Intent(this, GraficosActivity::class.java)
            intent.putExtra("NOME_USUARIO", usuarioNome)
            intent.putExtra("ID_USUARIO", idUsuario)
            intent.putExtra("TIPO_USUARIO", usuarioTipo)
            startActivity(intent)
        }

        btnHome.setOnClickListener {
            finish()
        }

        btnNotificacoes.setOnClickListener {
            val intent = Intent(this, NotificacoesActivity::class.java)
            intent.putExtra("ID_USUARIO", idUsuario)
            intent.putExtra("NOME_USUARIO", usuarioNome)
            intent.putExtra("TIPO_USUARIO", usuarioTipo)
            startActivity(intent)
        }

        btnAcoes.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        if (pilarId == -1) return

        databaseHelper = DatabaseHelper(this)
        viewPager = findViewById(R.id.viewPager)
        fabAdicionar = fab

        fabAdicionar.setOnClickListener {
            if (idUsuario == -1) {
                Toast.makeText(this, "Erro ao obter o contexto do usuário", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val options = mutableListOf<String>()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Escolha uma opção")

            if (idAcao != -1) {
                options.add("Criar Atividade")
            }
            options.add("Criar Ação")

            builder.setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Criar Atividade" -> {
                        val intent = Intent(this, CriarAtividadeActivity::class.java)
                        intent.putExtra("ACAO_ID", idAcao)
                        intent.putExtra("ID_USUARIO", idUsuario)
                        intent.putExtra("TIPO_USUARIO", usuarioTipo)
                        startActivity(intent)
                    }
                    "Criar Ação" -> {
                        val intent = Intent(this, CriarAcaoActivity::class.java)
                        intent.putExtra("PILAR_ID", pilarId)
                        intent.putExtra("ID_USUARIO", idUsuario)
                        intent.putExtra("TIPO_USUARIO", usuarioTipo)
                        startActivityForResult(intent, 100)
                    }
                }
            }
            builder.show()
        }

        // Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.title = "Pilar $pilarNumero - $pilarNome"
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // ---------- TOUR GUIADO ----------
        val btnAjuda = findViewById<ImageView>(R.id.btnAjuda)

        btnAjuda.setOnClickListener {
            val sequence = TapTargetSequence(this)
                .targets(
                    TapTarget.forView(fabAdicionar, "Adicionar", "Clique aqui para criar uma nova ação nesse pilar ou se quiser adicionar uma nova Atividade dentro da ação na tela, caso queira adicionar atividade em outra ação é só navegar até a tela dela e clicar aqui novamente.")
                        .cancelable(true)
                        .drawShadow(true)
                        .id(5)
                )
                .listener(object : TapTargetSequence.Listener {
                    override fun onSequenceFinish() {
                        Toast.makeText(this@ListaAtividades, "Tour finalizado!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}

                    override fun onSequenceCanceled(lastTarget: TapTarget) {
                        Toast.makeText(this@ListaAtividades, "Tour cancelado.", Toast.LENGTH_SHORT).show()
                    }
                })

            sequence.start()
        }


        carregarDados()
    }

    /**
     * Recebe o resultado de outras Activities (ex: criação de ação) e atualiza a lista
     * para refletir novas ações criadas.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            atualizarListaDeAcoes()
        }
    }

    override fun onResume() {
        super.onResume()
        carregarDados()
    }


    /**
     * Carrega as ações e atividades do banco conforme o modo de visualização atual.
     * Atualiza o adapter do ViewPager e mantém a posição atual da página exibida.
     */
    private fun carregarDados() {
        acoes = if (visualizacaoGeral) {
            databaseHelper.buscarAcoesEAtividadesPorPilar(pilarId)
        } else {
            databaseHelper.buscarAcoesEAtividadesDoUsuarioPorPilar(pilarId, idUsuario)
        }

        val currentItem = if (::viewPager.isInitialized) {
            val pos = viewPager.currentItem
            if (pos >= acoes.size) acoes.size - 1 else pos
        } else {
            0
        }

        viewPager.adapter = AcoesPagerAdapter(this, acoes, pilarNome, idUsuario, idAcao)
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
            idAcao = acoes[currentItem].acao.id
        } else {
            idAcao = -1
        }
    }
}

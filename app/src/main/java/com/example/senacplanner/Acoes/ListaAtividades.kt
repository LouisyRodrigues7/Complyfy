package com.example.senacplanner.Acoes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.senacplanner.DatabaseHelper
import com.example.senacplanner.R

class ListaAtividades : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_atividades)

        val pilarId = intent.getIntExtra("PILAR_ID", -1)
        val pilarNumero = intent.getIntExtra("PILAR_NUMERO", -1)
        val pilarNome = intent.getStringExtra("PILAR_NOME")
        if (pilarId == -1) return

        databaseHelper = DatabaseHelper(this)
        val acoes = databaseHelper.buscarAcoesEAtividadesPorPilar(pilarId)

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = AcoesPagerAdapter(this, acoes)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.title = "Pilar $pilarNumero - $pilarNome"
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}




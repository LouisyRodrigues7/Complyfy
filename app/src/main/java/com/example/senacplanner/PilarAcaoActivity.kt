package com.example.senacplanner

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.Acoes.Type.PilarType

class PilarAcaoActivity : AppCompatActivity() {

    private lateinit var spinnerPilar: Spinner
    private lateinit var btnBuscar: Button
    private lateinit var dbHelper: DatabaseHelper

    private var listaPilares: List<PilarType> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilar_acao)

        spinnerPilar = findViewById(R.id.spinnerPilar)
        btnBuscar = findViewById(R.id.btnBuscar)
        dbHelper = DatabaseHelper(this)

        carregarPilares()

        btnBuscar.setOnClickListener {
            val posicaoSelecionada = spinnerPilar.selectedItemPosition

            if (posicaoSelecionada >= 0 && listaPilares.isNotEmpty()) {
                val pilarSelecionado = listaPilares[posicaoSelecionada]

                val intent = Intent(this, EvolucaoPilarActivity::class.java)
                intent.putExtra("PILAR_ID", pilarSelecionado.id)
                startActivity(intent)

            } else {
                Toast.makeText(this, "Selecione um Pilar!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun carregarPilares() {
        listaPilares = dbHelper.getAllPilares()

        if (listaPilares.isEmpty()) {
            Toast.makeText(this, "Nenhum Pilar encontrado", Toast.LENGTH_SHORT).show()
        }

        val nomesPilares = listaPilares.map { it.nome }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nomesPilares
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPilar.adapter = adapter
    }
}

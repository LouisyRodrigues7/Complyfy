package com.example.senacplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.senacplanner.model.PilarItem

class ProgressoPilaresActivity : AppCompatActivity() {

    private lateinit var spinnerPilares: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progresso_pilares)

        spinnerPilares = findViewById(R.id.spinnerPilares)

        val db = DatabaseHelper(this)
        val pilaresDoBanco = db.getTodosPilares().toMutableList()

        // Adiciona "Todos os pilares" no início da lista
        pilaresDoBanco.add(0, PilarItem(id = -1, numero = 0, nome = "Todos os pilares"))

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            pilaresDoBanco
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPilares.adapter = adapter

        // Listener de seleção
        spinnerPilares.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position) as PilarItem
                val intent = Intent(this@ProgressoPilaresActivity, DashboardGraficoActivity::class.java)
                intent.putExtra("pilar_id", item.id)
                startActivity(intent)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}

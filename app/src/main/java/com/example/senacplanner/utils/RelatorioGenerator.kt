package com.example.senacplanner.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

class RelatorioGenerator {

    fun gerarRelatorioPDF(context: Context, pilares: List<PdfPilar>, nomeArquivo: String) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        var pageNumber = 1
        var y = 100

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 20f
        titlePaint.color = Color.BLACK

        paint.textSize = 12f

        canvas.drawText("Relatório de Compliance", 200f, y.toFloat(), titlePaint)
        y += 40

        pilares.forEach { pilar ->

            if (y > 800) {
                pdfDocument.finishPage(page)
                pageNumber++
                val pageInfoNew = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfoNew)
                canvas = page.canvas
                y = 50
            }

            canvas.drawText("Pilar: ${pilar.nome} (Nº ${pilar.numero})", 20f, y.toFloat(), titlePaint)
            y += 20
            canvas.drawText("Período: ${pilar.dataInicio} até ${pilar.dataConclusao ?: "-"}", 20f, y.toFloat(), paint)
            y += 20
            canvas.drawText("Descrição: ${pilar.descricao ?: "Sem descrição"}", 20f, y.toFloat(), paint)
            y += 20

            var totalAtividades = 0
            var concluidas = 0

            pilar.acoes.forEach { acao ->
                if (y > 800) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    val pageInfoNew = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(pageInfoNew)
                    canvas = page.canvas
                    y = 50
                }

                canvas.drawText("→ Ação: ${acao.nome}", 40f, y.toFloat(), titlePaint)
                y += 20

                val statusContagem = mutableMapOf(
                    "Concluída" to 0,
                    "Em andamento" to 0,
                    "Atrasada" to 0
                )

                acao.atividades.forEach { atividade ->
                    if (y > 800) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        val pageInfoNew = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                        page = pdfDocument.startPage(pageInfoNew)
                        canvas = page.canvas
                        y = 50
                    }

                    canvas.drawText("   • Atividade: ${atividade.nome}", 60f, y.toFloat(), paint)
                    y += 15
                    canvas.drawText("     Responsável: ${atividade.responsavel?.nome ?: "Não definido"}", 60f, y.toFloat(), paint)
                    y += 15
                    canvas.drawText("     Início: ${atividade.dataInicio} | Conclusão: ${atividade.dataConclusao ?: "-"}", 60f, y.toFloat(), paint)
                    y += 15
                    canvas.drawText("     Status: ${atividade.status}", 60f, y.toFloat(), paint)
                    y += 15

                    statusContagem[atividade.status] = (statusContagem[atividade.status] ?: 0) + 1

                    totalAtividades++
                    if (atividade.status == "Concluída") {
                        concluidas++
                    }
                }

                canvas.drawText(
                    "     ↳ Resumo Ação: ${statusContagem["Concluída"]} concluídas, ${statusContagem["Em andamento"]} em andamento, ${statusContagem["Atrasada"]} atrasadas",
                    60f,
                    y.toFloat(),
                    paint
                )
                y += 20
            }

            val percentual = if (totalAtividades > 0) (concluidas * 100) / totalAtividades else 0
            canvas.drawText("Percentual concluído do Pilar: $percentual%", 40f, y.toFloat(), paint)
            y += 30
            canvas.drawLine(20f, y.toFloat(), 575f, y.toFloat(), paint)
            y += 20
        }

        pdfDocument.finishPage(page)

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$nomeArquivo.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        Toast.makeText(context, "PDF salvo em: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }
}
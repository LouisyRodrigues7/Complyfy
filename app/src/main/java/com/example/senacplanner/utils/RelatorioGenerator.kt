package com.example.senacplanner.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

class RelatorioGenerator {
    // Gera um relatório em PDF com os dados dos pilares e suas ações/atividades.

    fun gerarRelatorioPDF(context: Context, pilares: List<PdfPilar>, nomeArquivo: String) {

        val pdfDocument = PdfDocument()
        val paint = Paint() // Paint para textos comuns
        val titlePaint = Paint() // Paint para títulos e textos em negrito
        val subtitlePaint = Paint() // Para subtítulos e detalhes importantes
        val linePaint = Paint() // Para linhas divisórias

        var pageNumber = 1
        var y = 100

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Configurações para os títulos principais
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 22f
        titlePaint.color = Color.parseColor("#1B2631") // azul escuro

        // Para subtítulos e textos importantes
        subtitlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        subtitlePaint.textSize = 14f
        subtitlePaint.color = Color.parseColor("#2874A6") // azul médio

        // Textos comuns
        paint.textSize = 12f
        paint.color = Color.parseColor("#212F3C") // quase preto azulado

        // Linhas divisórias
        linePaint.color = Color.parseColor("#AED6F1")
        linePaint.strokeWidth = 2f

        // Título do relatório centralizado horizontalmente
        val title = "Relatório de Compliance"
        val titleWidth = titlePaint.measureText(title)
        canvas.drawText(title, (pageInfo.pageWidth - titleWidth) / 2, y.toFloat(), titlePaint)
        y += 50

        pilares.forEach { pilar ->

            if (y > 780) {  // margem inferior para quebra de página
                pdfDocument.finishPage(page)
                pageNumber++
                val pageInfoNew = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfoNew)
                canvas = page.canvas
                y = 50
            }

            // Pilar - título em azul médio
            canvas.drawText("Pilar: ${pilar.nome} (Nº ${pilar.numero})", 20f, y.toFloat(), subtitlePaint)
            y += 22

            // Período e descrição com espaçamento maior
            paint.textSize = 12f
            canvas.drawText("Período: ${pilar.dataInicio} até ${pilar.dataConclusao ?: "-"}", 25f, y.toFloat(), paint)
            y += 18
            canvas.drawText("Descrição: ${pilar.descricao ?: "Sem descrição"}", 25f, y.toFloat(), paint)
            y += 25

            var totalAtividades = 0
            var concluidas = 0

            pilar.acoes.forEach { acao ->

                if (y > 780) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    val pageInfoNew = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(pageInfoNew)
                    canvas = page.canvas
                    y = 50
                }

                // Ação em negrito e azul escuro, com margem esquerda maior
                titlePaint.textSize = 16f
                titlePaint.color = Color.parseColor("#154360")
                canvas.drawText("→ Ação: ${acao.nome}", 40f, y.toFloat(), titlePaint)
                y += 22

                val statusContagem = mutableMapOf(
                    "Concluída" to 0,
                    "Em andamento" to 0,
                    "Atrasada" to 0
                )

                paint.textSize = 12f
                paint.color = Color.parseColor("#212F3C")

                acao.atividades.forEach { atividade ->

                    if (y > 780) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        val pageInfoNew = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                        page = pdfDocument.startPage(pageInfoNew)
                        canvas = page.canvas
                        y = 50
                    }

                    // Atividade com recuo e marcador, texto comum
                    canvas.drawText("   • Atividade: ${atividade.nome}", 60f, y.toFloat(), paint)
                    y += 17
                    canvas.drawText("     Responsável: ${atividade.responsavel?.nome ?: "Não definido"}", 60f, y.toFloat(), paint)
                    y += 17
                    canvas.drawText("     Início: ${atividade.dataInicio} | Conclusão: ${atividade.dataConclusao ?: "-"}", 60f, y.toFloat(), paint)
                    y += 17

                    // Status com cor condicional
                    when (atividade.status) {
                        "Concluída" -> paint.color = Color.parseColor("#229954") // verde
                        "Em andamento" -> paint.color = Color.parseColor("#D68910") // amarelo-alaranjado
                        "Atrasada" -> paint.color = Color.parseColor("#C0392B") // vermelho
                        else -> paint.color = Color.parseColor("#212F3C") // padrão
                    }
                    canvas.drawText("     Status: ${atividade.status}", 60f, y.toFloat(), paint)
                    y += 20

                    paint.color = Color.parseColor("#212F3C") // reset cor para texto comum

                    statusContagem[atividade.status] = (statusContagem[atividade.status] ?: 0) + 1
                    totalAtividades++
                    if (atividade.status == "Concluída") concluidas++
                }

                // Resumo da ação, cor azul médio
                subtitlePaint.textSize = 13f
                subtitlePaint.color = Color.parseColor("#2874A6")
                canvas.drawText(
                    "     ↳ Resumo Ação: ${statusContagem["Concluída"]} concluídas, ${statusContagem["Em andamento"]} em andamento, ${statusContagem["Atrasada"]} atrasadas",
                    60f,
                    y.toFloat(),
                    subtitlePaint
                )
                y += 30
            }

            val percentual = if (totalAtividades > 0) (concluidas * 100) / totalAtividades else 0

            // Percentual concluído do pilar em negrito, cor verde escuro
            titlePaint.textSize = 14f
            titlePaint.color = Color.parseColor("#196F3D")
            canvas.drawText("Percentual concluído do Pilar: $percentual%", 40f, y.toFloat(), titlePaint)
            y += 25

            // Linha divisória suave abaixo do pilar
            canvas.drawLine(20f, y.toFloat(), 575f, y.toFloat(), linePaint)
            y += 30
        }

        pdfDocument.finishPage(page)

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$nomeArquivo.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        Toast.makeText(context, "PDF salvo em: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }
}

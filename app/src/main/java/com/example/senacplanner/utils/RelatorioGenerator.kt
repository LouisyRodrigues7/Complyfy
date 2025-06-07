package com.example.senacplanner.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import android.net.Uri
import android.os.Environment
import java.text.Normalizer

class RelatorioGenerator {

    // ✅ Função auxiliar para normalizar acentos e letras
    private fun normalizarStatus(status: String?): String {
        if (status.isNullOrBlank()) return ""
        return Normalizer.normalize(status, Normalizer.Form.NFD)
            .replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
            .lowercase()
            .trim()
    }

    fun gerarRelatorioPDF(
        context: Context,
        pilares: List<PdfPilar>,
        nomeArquivo: String,
        onPdfGenerated: ((Uri?) -> Unit)? = null
    ) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()
        val subtitlePaint = Paint()
        val linePaint = Paint()
        val tableHeaderPaint = Paint()
        val cellPaint = Paint()

        var pageNumber = 1
        var y = 100f

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 22f
        titlePaint.color = Color.parseColor("#1B2631")

        subtitlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        subtitlePaint.textSize = 14f
        subtitlePaint.color = Color.parseColor("#2874A6")

        paint.textSize = 12f
        paint.color = Color.parseColor("#212F3C")

        linePaint.color = Color.parseColor("#AED6F1")
        linePaint.strokeWidth = 2f

        tableHeaderPaint.textSize = 12f
        tableHeaderPaint.color = Color.WHITE
        tableHeaderPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        cellPaint.style = Paint.Style.FILL

        val rowHeightMin = 24f
        val padding = 8f
        val rowPadding = 10f

        val columnTitles = listOf("Atividade", "Responsável", "Início", "Conclusão", "Status")
        val columnWidths = listOf(150f, 110f, 70f, 80f, 100f)
        val columnX = mutableListOf<Float>()

        var startX = 40f
        columnWidths.forEach {
            columnX.add(startX)
            startX += it
        }

        val title = "Relatório de Compliance"
        val titleWidth = titlePaint.measureText(title)
        canvas.drawText(title, (pageInfo.pageWidth - titleWidth) / 2, y, titlePaint)
        y += 60f

        val textPaint = TextPaint(paint)

        pilares.forEach { pilar ->
            if (y > 700f) {
                pdfDocument.finishPage(page)
                pageNumber++
                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(newPageInfo)
                canvas = page.canvas
                y = 60f
            }

            canvas.drawText("Pilar: ${pilar.nome} (Nº ${pilar.numero})", 20f, y, subtitlePaint)
            y += 25f
            canvas.drawText("Período: ${pilar.dataInicio} até ${pilar.dataConclusao ?: "-"}", 25f, y, paint)
            y += 22f
            canvas.drawText("Descrição: ${pilar.descricao ?: "Sem descrição"}", 25f, y, paint)
            y += 30f

            var totalAtividades = 0
            var concluidas = 0

            pilar.acoes.forEach { acao ->
                if (y > 700f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(newPageInfo)
                    canvas = page.canvas
                    y = 60f
                }

                val boldPaint = TextPaint(textPaint)
                boldPaint.typeface = Typeface.DEFAULT_BOLD

                val acaoLayout = StaticLayout.Builder.obtain("→ Ação: ${acao.nome}", 0, acao.nome.length + 8, boldPaint, 515)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1f)
                    .setIncludePad(false)
                    .build()

                canvas.save()
                canvas.translate(40f, y)
                acaoLayout.draw(canvas)
                canvas.restore()
                y += acaoLayout.height + 10f

                // Cabeçalho da tabela
                cellPaint.color = Color.parseColor("#2874A6")
                columnX.forEachIndexed { index, x ->
                    canvas.drawRect(x, y, x + columnWidths[index], y + rowHeightMin, cellPaint)
                    canvas.drawText(columnTitles[index], x + padding, y + 16f, tableHeaderPaint)
                }

                canvas.drawLine(40f, y, 555f, y, linePaint)
                y += rowHeightMin + rowPadding

                val statusContagem = mutableMapOf("finalizada" to 0, "em andamento" to 0, "em atraso" to 0)

                acao.atividades.forEach { atividade ->
                    if (y > 780f) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                        page = pdfDocument.startPage(newPageInfo)
                        canvas = page.canvas
                        y = 60f
                    }

                    val statusNormalizado = normalizarStatus(atividade.status)
                    val statusColor = when (statusNormalizado) {
                        "finalizada" -> Color.parseColor("#229954")
                        "em andamento" -> Color.parseColor("#D68910")
                        "em atraso" -> Color.parseColor("#C0392B")
                        else -> Color.parseColor("#212F3C")
                    }

                    val atividadeLayout = StaticLayout.Builder.obtain(
                        atividade.nome ?: "",
                        0,
                        atividade.nome.length,
                        textPaint,
                        columnWidths[0].toInt()
                    ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setIncludePad(false)
                        .build()

                    val responsavelLayout = StaticLayout.Builder.obtain(
                        atividade.responsavel?.nome ?: "Não definido",
                        0,
                        (atividade.responsavel?.nome ?: "Não definido").length,
                        textPaint,
                        columnWidths[1].toInt()
                    ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setIncludePad(false)
                        .build()

                    val cellHeight = maxOf(rowHeightMin, atividadeLayout.height.toFloat(), responsavelLayout.height.toFloat())

                    canvas.save()
                    canvas.translate(columnX[0] + padding, y)
                    atividadeLayout.draw(canvas)
                    canvas.restore()

                    canvas.save()
                    canvas.translate(columnX[1] + padding, y)
                    responsavelLayout.draw(canvas)
                    canvas.restore()

                    paint.color = Color.parseColor("#212F3C")
                    canvas.drawText(atividade.dataInicio ?: "-", columnX[2] + padding, y + 16f, paint)
                    canvas.drawText(atividade.dataConclusao ?: "-", columnX[3] + padding, y + 16f, paint)

                    paint.color = statusColor
                    canvas.drawText(atividade.status ?: "", columnX[4] + padding, y + 16f, paint)

                    y += cellHeight + rowPadding
                    canvas.drawLine(40f, y, 555f, y, linePaint)

                    // Contabilização
                    statusContagem[statusNormalizado] = (statusContagem[statusNormalizado] ?: 0) + 1
                    totalAtividades++
                    if (statusNormalizado == "finalizada") concluidas++
                }

                y += 10f
                paint.color = Color.parseColor("#2874A6")
                canvas.drawText(
                    "Resumo: ${statusContagem["finalizada"]} finalizadas, " +
                            "${statusContagem["em andamento"]} em andamento, " +
                            "${statusContagem["em atraso"]} em atraso",
                    40f, y, paint
                )
                y += 30f
                canvas.drawLine(50f, y, 545f, y, linePaint)
                y += 35f
            }

            val percentual = if (totalAtividades > 0) (concluidas * 100) / totalAtividades else 0
            titlePaint.textSize = 14f
            titlePaint.color = Color.parseColor("#196F3D")
            canvas.drawText("Percentual concluído do Pilar: $percentual%", 40f, y, titlePaint)
            y += 35f
            canvas.drawLine(20f, y, 575f, y, linePaint)
            y += 50f
        }

        pdfDocument.finishPage(page)

        var finalUri: Uri? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "$nomeArquivo.pdf")
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val itemUri = resolver.insert(collection, contentValues)

            itemUri?.let { uri ->
                resolver.openOutputStream(uri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                finalUri = uri
                Toast.makeText(context, "PDF salvo na pasta Downloads", Toast.LENGTH_LONG).show()
            }
        } else {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "$nomeArquivo.pdf"
            )
            pdfDocument.writeTo(FileOutputStream(file))
            finalUri = Uri.fromFile(file)
            Toast.makeText(context, "PDF salvo em: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }

        pdfDocument.close()
        onPdfGenerated?.invoke(finalUri)
    }
}

package com.example.pivot.utils

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


/**
 * Responsável por gerar relatórios em PDF contendo dados de pilares, ações e atividades.
 *
 * Essa classe utiliza a API `PdfDocument` do Android para desenhar manualmente o conteúdo em páginas.
 * O relatório é salvo na pasta `Downloads`, compatível com versões abaixo e acima do Android Q (API 29).
 */
class RelatorioGenerator {

    private fun desenharMarcaDagua(context: Context, canvas: Canvas, pageInfo: PdfDocument.PageInfo) {
        try {
            val bgLogoResId = context.resources.getIdentifier("bg_logo", "drawable", context.packageName)
            if (bgLogoResId != 0) {
                val bitmap = BitmapFactory.decodeResource(context.resources, bgLogoResId)
                    ?: return

                val targetWidth = 300
                val aspectRatio = bitmap.height.toFloat() / bitmap.width
                val targetHeight = (targetWidth * aspectRatio).toInt()

                val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                    ?: return

                val paint = Paint().apply {
                    alpha = 25
                    isFilterBitmap = true
                }

                val x = (pageInfo.pageWidth - targetWidth) / 2f
                val y = (pageInfo.pageHeight - targetHeight) / 2f

                canvas.drawBitmap(scaled, x, y, paint)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Normaliza o texto de status (remove acentos e formata).
     * Usado para padronizar cores e comparações sem erros causados por acentuação.
     */
    private fun normalizarStatus(status: String?): String {
        if (status.isNullOrBlank()) return ""
        return Normalizer.normalize(status, Normalizer.Form.NFD)
            .replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
            .lowercase()
            .trim()
    }

    /**
     * Gera o arquivo PDF com os dados fornecidos de pilares, ações e atividades.
     *
     * @param context Contexto Android para salvar e acessar arquivos
     * @param pilares Lista de pilares com suas ações e atividades
     * @param nomeArquivo Nome do arquivo PDF (sem extensão)
     * @param onPdfGenerated Callback com a URI do arquivo gerado (ou null em caso de erro)
     */
    fun gerarRelatorioPDF(
        context: Context,
        pilares: List<PdfPilar>,
        nomeArquivo: String,
        onPdfGenerated: ((Uri?) -> Unit)? = null
    ) {
        val pdfDocument = PdfDocument()

        // Inicializa Paints para título, textos, células e linhas
        val paint = Paint()
        val titlePaint = Paint()
        val subtitlePaint = Paint()
        val linePaint = Paint()
        val tableHeaderPaint = Paint()
        val cellPaint = Paint()

        var pageNumber = 1
        var y = 100f // coordenada vertical inicial

        // Cria a primeira página
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Marca d'água de fundo com transparência - imagem bg_logo
        val bgLogoResId = context.resources.getIdentifier("bg_logo", "drawable", context.packageName)
        if (bgLogoResId != 0) {
            val watermarkBitmap = BitmapFactory.decodeResource(context.resources, bgLogoResId)
            val targetWidth = 300 // Tamanho da marca d'água
            val aspectRatio = watermarkBitmap.height.toFloat() / watermarkBitmap.width
            val targetHeight = (targetWidth * aspectRatio).toInt()

            val scaledWatermark = Bitmap.createScaledBitmap(watermarkBitmap, targetWidth, targetHeight, true)

            val watermarkPaint = Paint().apply {
                alpha = 25 // Transparência (quanto menor, mais "suave")
                isFilterBitmap = true
            }

            val watermarkX = (pageInfo.pageWidth - targetWidth) / 2f
            val watermarkY = (pageInfo.pageHeight - targetHeight) / 2f

            canvas.drawBitmap(scaledWatermark, watermarkX, watermarkY, watermarkPaint)
        }


        // Estilos
        titlePaint.apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 22f
            color =
                Color.parseColor("#1B2631") // 🔵 Cor do **título principal do relatório** (ex: "Relatório de Compliance")
        }

        subtitlePaint.apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 14f
            color =
                Color.parseColor("#2874A6")  // 🔵 Cor dos **subtítulos**, como "Pilar: X" ou "Período: de X até Y"
        }

        paint.apply {
            textSize = 12f
            color =
                Color.parseColor("#212F3C")  // ⚫ Cor padrão do **texto geral** (descrição do pilar, datas simples, etc)
        }

        linePaint.color =
            Color.parseColor("#1C2A39") // 🔷 Cor das **linhas de grade da tabela** (linhas horizontais e verticais)
        linePaint.strokeWidth = 7f  // Espessura dessas linhas (quanto maior, mais grossas)

        tableHeaderPaint.apply {
            textSize =
                12f // Tamanho da fonte do cabeçalho da tabela (ex: "Atividade", "Responsável", etc)
            color =
                Color.WHITE // ⚪ Cor do texto no cabeçalho da tabela (costuma ficar sobre fundo azul)
            typeface =
                Typeface.create(Typeface.DEFAULT, Typeface.BOLD)  // Negrito no cabeçalho da tabela
        }

        cellPaint.style = Paint.Style.FILL

        val rowHeightMin = 24f
        val padding = 8f
        val rowPadding = 10f

        // Cabeçalhos das colunas
        val columnTitles = listOf("Atividade", "Responsável", "Início", "Conclusão", "Status")
        val columnWidths = listOf(150f, 110f, 70f, 80f, 100f)
        val columnX = mutableListOf<Float>()
        var startX = 40f

        columnWidths.forEach {
            columnX.add(startX)
            startX += it
        }

        // 1. Recupera o ID da logo
        val logoResId =
            context.resources.getIdentifier("logo_pivot", "drawable", context.packageName)

        if (logoResId != 0) {
            val originalLogo = BitmapFactory.decodeResource(context.resources, logoResId)

            // 2. Redimensiona proporcionalmente
            val logoTargetWidth = 160
            val aspectRatio = originalLogo.height.toFloat() / originalLogo.width.toFloat()
            val logoTargetHeight = (logoTargetWidth * aspectRatio).toInt()
            val logoScaled =
                Bitmap.createScaledBitmap(originalLogo, logoTargetWidth, logoTargetHeight, true)

            // 3. Centraliza a logo no topo
            val logoX = (pageInfo.pageWidth - logoScaled.width) / 2f
            val logoY = 40f
            canvas.drawBitmap(logoScaled, logoX, logoY, null)

            val logoResId = context.resources.getIdentifier("logo_pivot", "drawable", context.packageName)

            if (logoResId != 0) {
                val originalLogo = BitmapFactory.decodeResource(context.resources, logoResId)

                // 2. Redimensiona proporcionalmente
                val logoTargetWidth = 160
                val aspectRatio = originalLogo.height.toFloat() / originalLogo.width.toFloat()
                val logoTargetHeight = (logoTargetWidth * aspectRatio).toInt()
                val logoScaled = Bitmap.createScaledBitmap(originalLogo, logoTargetWidth, logoTargetHeight, true)

                // 3. Centraliza a logo no topo
                val logoX = (pageInfo.pageWidth - logoScaled.width) / 2f
                val logoY = 40f
                canvas.drawBitmap(logoScaled, logoX, logoY, null)

                // 4. Título à esquerda, com mais espaço abaixo da logo
                val title = "Relatório de Compliance"
                val titleX = 40f
                val titleY = logoY + logoScaled.height + 50f // Espaço entre logo e titulo
                canvas.drawText(title, titleX, titleY, titlePaint)

                // Atualiza y para seguir a partir do título
                y = titleY + 30f
            } else {
                // Fallback: sem logo
                val title = "Relatório de Compliance"
                val titleX = 40f
                val titleY = 60f
                canvas.drawText(title, titleX, titleY, titlePaint)
                y = titleY + 30f
            }


            val textPaint = TextPaint(paint)

            pilares.forEach { pilar ->
                if (y > 700f) {
                    // Quando o conteúdo passa do limite da página, cria nova página
                    pdfDocument.finishPage(page)
                    pageNumber++
                    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    desenharMarcaDagua(context, canvas, pageInfo)

                    y = 60f
                }

                // Exibe dados do pilar
                canvas.drawText("Pilar: ${pilar.nome} (Nº ${pilar.numero})", 20f, y, subtitlePaint)
                y += 25f
                canvas.drawText(
                    "Período: ${pilar.dataInicio} até ${pilar.dataConclusao ?: "-"}",
                    25f,
                    y,
                    paint
                )
                y += 22f
                canvas.drawText("Descrição: ${pilar.descricao ?: "Sem descrição"}", 25f, y, paint)
                y += 30f

                var totalAtividades = 0
                var concluidas = 0

                pilar.acoes.forEach { acao ->

                    // Verifica se precisa de nova página
                    if (y > 700f) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        desenharMarcaDagua(context, canvas, pageInfo)

                        y = 60f
                    }

                    // Título da ação
                    val boldPaint = TextPaint(textPaint).apply {
                        typeface = Typeface.DEFAULT_BOLD
                    }

                    val acaoLayout = StaticLayout.Builder.obtain(
                        "→ Ação: ${acao.nome}",
                        0,
                        acao.nome.length + 8,
                        boldPaint,
                        515
                    )
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(0f, 1f)
                        .setIncludePad(false)
                        .build()

                    canvas.save()
                    canvas.translate(40f, y)
                    acaoLayout.draw(canvas)
                    canvas.restore()
                    y += acaoLayout.height + 10f

                    // Cabeçalho da tabela de atividades
                    cellPaint.color = Color.parseColor("#2874A6")
                    columnX.forEachIndexed { index, x ->
                        canvas.drawRect(x, y, x + columnWidths[index], y + rowHeightMin, cellPaint)
                        canvas.drawText(columnTitles[index], x + padding, y + 16f, tableHeaderPaint)
                    }
                    y += rowHeightMin + rowPadding

                    val statusContagem =
                        mutableMapOf("finalizada" to 0, "em andamento" to 0, "em atraso" to 0)

                    acao.atividades.forEach { atividade ->
                        if (y > 780f) {
                            pdfDocument.finishPage(page)
                            pageNumber++
                            page = pdfDocument.startPage(
                                PdfDocument.PageInfo.Builder(
                                    595,
                                    842,
                                    pageNumber
                                ).create()
                            )
                            canvas = page.canvas
                            desenharMarcaDagua(context, canvas, pageInfo)
                            y = 60f
                        }

                        // Layouts de célula com texto quebrado automaticamente
                        val atividadeLayout = StaticLayout.Builder.obtain(
                            atividade.nome ?: "",
                            0,
                            atividade.nome.length,
                            textPaint,
                            columnWidths[0].toInt()
                        )
                            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                            .setIncludePad(false)
                            .build()

                        val responsavelNome = atividade.responsavel?.nome ?: "Não definido"
                        val responsavelLayout = StaticLayout.Builder.obtain(
                            responsavelNome,
                            0,
                            responsavelNome.length,
                            textPaint,
                            columnWidths[1].toInt()
                        )
                            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                            .setIncludePad(false)
                            .build()

                        val cellHeight = maxOf(
                            rowHeightMin,
                            atividadeLayout.height.toFloat(),
                            responsavelLayout.height.toFloat()
                        )

                        canvas.save()
                        canvas.translate(columnX[0] + padding, y)
                        atividadeLayout.draw(canvas)
                        canvas.restore()

                        canvas.save()
                        canvas.translate(columnX[1] + padding, y)
                        responsavelLayout.draw(canvas)
                        canvas.restore()

                        // Preenche campos simples
                        paint.color = Color.parseColor("#212F3C")
                        canvas.drawText(
                            atividade.dataInicio ?: "-",
                            columnX[2] + padding,
                            y + 16f,
                            paint
                        )
                        canvas.drawText(
                            atividade.dataConclusao ?: "-",
                            columnX[3] + padding,
                            y + 16f,
                            paint
                        )

                        // Status com cor personalizada
                        val status = normalizarStatus(atividade.status)
                        val cor = when (status) {
                            "finalizada" -> "#229954"
                            "em andamento" -> "#D68910"
                            "em atraso" -> "#C0392B"
                            else -> "#212F3C"
                        }
                        paint.color = Color.parseColor(cor)
                        canvas.drawText(
                            atividade.status ?: "",
                            columnX[4] + padding,
                            y + 16f,
                            paint
                        )

                        y += cellHeight + rowPadding


                        linePaint.color = Color.LTGRAY
                        linePaint.strokeWidth = 1f
                        val linhaTopo = y - cellHeight - rowPadding
                        val linhaBase = y - rowPadding

                        columnX.forEach { x ->
                            canvas.drawLine(x, linhaTopo, x, linhaBase, linePaint)
                        }

                        canvas.drawLine(
                            columnX.last() + columnWidths.last(),
                            linhaTopo,
                            columnX.last() + columnWidths.last(),
                            linhaBase,
                            linePaint
                        )
                        // Linhas horizontais superior e inferior
                        canvas.drawLine(
                            columnX.first(),
                            linhaTopo,
                            columnX.last() + columnWidths.last(),
                            linhaTopo,
                            linePaint
                        ) // topo da linha
                        canvas.drawLine(
                            columnX.first(),
                            linhaBase,
                            columnX.last() + columnWidths.last(),
                            linhaBase,
                            linePaint
                        ) // base da linha


                        // Contagem para resumo
                        statusContagem[status] = (statusContagem[status] ?: 0) + 1
                        totalAtividades++
                        if (status == "finalizada") concluidas++
                    }

                    y += 10f
                    paint.color = Color.parseColor("#2874A6")
                    canvas.drawText(
                        "Resumo: ${statusContagem["finalizada"]} finalizadas, ${statusContagem["em andamento"]} em andamento, ${statusContagem["em atraso"]} em atraso",
                        40f,
                        y,
                        paint
                    )
                    y += 30f
                }

                val percentual =
                    if (totalAtividades > 0) (concluidas * 100) / totalAtividades else 0
                titlePaint.textSize = 14f
                titlePaint.color = Color.parseColor("#196F3D")
                canvas.drawText("Percentual concluído do Pilar: $percentual%", 40f, y, titlePaint)
                y += 50f
            }

            pdfDocument.finishPage(page)



            // 📁 Salvar PDF conforme versão do Android
            var finalUri: Uri? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ usa MediaStore para salvar
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, "$nomeArquivo.pdf")
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val collection =
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri = resolver.insert(collection, contentValues)

                itemUri?.let { uri ->
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    finalUri = uri
                    Toast.makeText(context, "PDF salvo na pasta Downloads", Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                // Android 9 ou inferior: salva diretamente
                val file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "$nomeArquivo.pdf"
                )
                pdfDocument.writeTo(FileOutputStream(file))
                finalUri = Uri.fromFile(file)
                Toast.makeText(context, "PDF salvo em: ${file.absolutePath}", Toast.LENGTH_LONG)
                    .show()
            }

            pdfDocument.close()
            onPdfGenerated?.invoke(finalUri)
        }
    }
}

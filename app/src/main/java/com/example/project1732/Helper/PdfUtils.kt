package com.example.project1732.Helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.project1732.Domain.Order
import com.itextpdf.kernel.colors.Color
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.io.IOException

object PdfUtils {

    fun generatePdf(context: Context, order: Order): File? {
        val pdfFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "Order_${order.idPedido}.pdf"
        )

        try {
            PdfWriter(pdfFile.absolutePath).use { pdfWriter ->
                PdfDocument(pdfWriter).use { pdfDocument ->
                    Document(pdfDocument, PageSize.A4).use { document ->

                        // Fonts
                        val titleFont =
                            PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD)
                        val bodyFont =
                            PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA)

                        // Company and Order Information
                        document.add(
                            Paragraph("Rapidin Delivery\nRua Janete Marilda de Souza\nFone: +5545991208208")
                                .setFont(bodyFont)
                                .setTextAlignment(TextAlignment.CENTER)
                        )
                        document.add(
                            Paragraph(
                                "Tipo de pedido: Entrega a domicílio\n\n" +
                                        "ID do pedido: ${order.idPedido}\n" +
                                        "Nome do cliente: ${order.userName}\n" +
                                        "Fone: ${Utils.formatPhone(order.telefone)}\n" +
                                        "Endereço de entrega: ${order.formatAddress(order.deliveryAddress)}\n" +
                                        "Data: ${order.formattedTimestamp()}"
                            )
                                .setFont(bodyFont)
                                .setTextAlignment(TextAlignment.LEFT)
                                .setMarginBottom(10f)
                        )

//                        Utils.formatCpf(order.cpf), // Chamada para formatar CPF
//                        // Chamada para formatar CPF
//                        Utils.formatPhone(order.telefone),

                        // Item List Table
                        val itemTable =
                            Table(UnitValue.createPercentArray(floatArrayOf(1f, 5f, 2f)))
                        itemTable.setWidth(UnitValue.createPercentValue(100f))

                        itemTable.addCell(
                            createCell(
                                "Qtd",
                                isBold = true,
                                backgroundColor = ColorConstants.LIGHT_GRAY
                            )
                        )
                        itemTable.addCell(
                            createCell(
                                "Item",
                                isBold = true,
                                backgroundColor = ColorConstants.LIGHT_GRAY
                            )
                        )
                        itemTable.addCell(
                            createCell(
                                "Preço",
                                isBold = true,
                                alignment = TextAlignment.RIGHT,
                                backgroundColor = ColorConstants.LIGHT_GRAY
                            )
                        )

                        for (itemMap in order.foodsList) {
                            val numberInCart = (itemMap["numberInCart"] as? Number)?.toInt() ?: 0
                            val title = itemMap["Title"] as? String ?: "Sem título"
                            val price = (itemMap["Price"] as? Number)?.toDouble() ?: 0.0

                            itemTable.addCell(
                                createCell(
                                    "$numberInCart",
                                    alignment = TextAlignment.LEFT
                                )
                            )
                            itemTable.addCell(createCell(title))
                            itemTable.addCell(
                                createCell(
                                    "R$ ${String.format("%.2f", price)}",
                                    alignment = TextAlignment.RIGHT
                                )
                            )
                        }

                        document.add(itemTable)

                        // Order Summary Table
                        val summaryTable = Table(UnitValue.createPercentArray(2))
                        summaryTable.setWidth(UnitValue.createPercentValue(100f))

                        summaryTable.addCell(
                            createCell(
                                "Preço dos itens",
                                alignment = TextAlignment.LEFT
                            )
                        )
                        summaryTable.addCell(
                            createCell(
                                "R$ ${
                                    String.format(
                                        "%.2f",
                                        order.totalFee
                                    )
                                }", alignment = TextAlignment.RIGHT
                            )
                        )

//                        summaryTable.addCell(createCell("Custo adicional", alignment = TextAlignment.LEFT))
//                        summaryTable.addCell(createCell("R$ 0.00", alignment = TextAlignment.RIGHT))

//                        summaryTable.addCell(createCell("Subtotal", alignment = TextAlignment.LEFT))
//                        summaryTable.addCell(createCell("R$ ${String.format("%.2f", order.totalFee)}", alignment = TextAlignment.RIGHT))

//                        summaryTable.addCell(createCell("Desconto", alignment = TextAlignment.LEFT))
//                        summaryTable.addCell(createCell("R$ 2.65", alignment = TextAlignment.RIGHT))

//                        summaryTable.addCell(createCell("Taxa Extra", alignment = TextAlignment.LEFT))
//                        summaryTable.addCell(createCell("R$ 0.00", alignment = TextAlignment.RIGHT))

                        summaryTable.addCell(
                            createCell(
                                "Taxa de entrega",
                                alignment = TextAlignment.LEFT
                            )
                        )
                        summaryTable.addCell(
                            createCell(
                                "R$ ${
                                    String.format(
                                        "%.2f",
                                        order.taxaEntrega
                                    )
                                }", alignment = TextAlignment.RIGHT
                            )
                        )

                        summaryTable.addCell(
                            createCell(
                                "Total",
                                isBold = true,
                                backgroundColor = ColorConstants.LIGHT_GRAY
                            )
                        )
                        summaryTable.addCell(
                            createCell(
                                "R$ ${
                                    String.format(
                                        "%.2f",
                                        order.totalFee + order.taxaEntrega
                                    )
                                }",
                                isBold = true,
                                alignment = TextAlignment.RIGHT,
                                backgroundColor = ColorConstants.LIGHT_GRAY
                            )
                        )

                        document.add(summaryTable)

                        // Footer
                        document.add(
                            Paragraph("\n\nOBRIGADO\nPela preferência")
                                .setFont(titleFont)
                                .setTextAlignment(TextAlignment.CENTER)
                        )
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("PdfUtils", "Error generating PDF", e)
            Toast.makeText(context, "Erro ao gerar PDF", Toast.LENGTH_SHORT).show()
            return null
        }

        return pdfFile
    }

    private fun createCell(
        text: String,
        isBold: Boolean = false,
        alignment: TextAlignment = TextAlignment.LEFT,
        backgroundColor: Color? = null
    ): Cell {
        val paragraph = Paragraph(text)
        if (isBold) paragraph.setBold()
        paragraph.setTextAlignment(alignment)
        val cell = Cell().add(paragraph)
            .setBorder(SolidBorder(ColorConstants.BLACK, 1f))
            .setPadding(5f)

        if (backgroundColor != null) {
            cell.setBackgroundColor(backgroundColor)
        }

        return cell
    }

    fun openPdf(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Nenhum aplicativo disponível para abrir o PDF",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}



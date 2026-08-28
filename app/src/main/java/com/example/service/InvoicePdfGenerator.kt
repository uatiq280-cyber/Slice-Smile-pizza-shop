package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InvoicePdfGenerator {

    fun generateInvoiceReport(
        context: Context,
        reportTitle: String,
        orders: List<Order>,
        filterLabel: String
    ): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 standard width in points (72 dpi)
            val pageHeight = 842 // A4 standard height

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val primaryColor = Color.rgb(139, 30, 30) // PolishMaroonDark / Pizza Red
            val darkText = Color.rgb(26, 26, 26)
            val mutedText = Color.rgb(110, 110, 110)
            val lightBg = Color.rgb(248, 248, 248)
            val borderLine = Color.rgb(220, 220, 220)

            // Header Background
            paint.color = primaryColor
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 95f, paint)

            // Title
            paint.color = Color.WHITE
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("SLICE SMILE PIZZA & FAST FOOD", 24f, 40f, paint)

            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Main Commercial Market, Jinnah Town, Sadiqabad | Ph: 0325-4946190", 24f, 60f, paint)

            paint.textSize = 10f
            paint.color = Color.rgb(255, 215, 0) // Gold accent
            canvas.drawText("Sales Invoice & Financial Summary Report", 24f, 78f, paint)

            // Generation Date
            val sdf = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault())
            val dateStr = "Generated: ${sdf.format(Date())}"
            paint.color = Color.WHITE
            paint.textSize = 9f
            paint.typeface = Typeface.DEFAULT
            val dateWidth = paint.measureText(dateStr)
            canvas.drawText(dateStr, pageWidth - dateWidth - 24f, 78f, paint)

            var yPos = 118f

            // Filter Badge & Period Header
            paint.color = darkText
            paint.textSize = 15f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(reportTitle, 24f, yPos, paint)

            paint.color = mutedText
            paint.textSize = 10f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("Period Filter: $filterLabel", 24f, yPos + 16f, paint)

            yPos += 34f

            // Metrics Summary Cards
            val totalOrders = orders.size
            val deliveredOrders = orders.filter { it.status == OrderStatus.DELIVERED }
            val grossSales = orders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.totalAmount }
            val totalDiscounts = orders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.discount }
            val codSales = orders.filter { it.status != OrderStatus.CANCELLED && it.paymentMethod == PaymentMethod.CASH_ON_DELIVERY }.sumOf { it.totalAmount }
            val onlineSales = grossSales - codSales

            val cardWidth = (pageWidth - 48f - 24f) / 3f
            val cardHeight = 54f

            // Card 1: Total Orders
            drawSummaryCard(canvas, 24f, yPos, cardWidth, cardHeight, "Total Orders", "$totalOrders Orders", "Delivered: ${deliveredOrders.size}", lightBg, primaryColor)

            // Card 2: Total Revenue
            drawSummaryCard(canvas, 24f + cardWidth + 12f, yPos, cardWidth, cardHeight, "Net Revenue", "Rs. $grossSales", "Discounts: Rs. $totalDiscounts", lightBg, Color.rgb(46, 125, 50))

            // Card 3: Payment Split
            drawSummaryCard(canvas, 24f + (cardWidth + 12f) * 2, yPos, cardWidth, cardHeight, "Payment Split", "COD: Rs. $codSales", "Online: Rs. $onlineSales", lightBg, Color.rgb(21, 101, 192))

            yPos += cardHeight + 24f

            // Table Header
            paint.color = primaryColor
            paint.style = Paint.Style.FILL
            canvas.drawRect(24f, yPos, pageWidth - 24f, yPos + 22f, paint)

            paint.color = Color.WHITE
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            canvas.drawText("#ID", 28f, yPos + 15f, paint)
            canvas.drawText("Date/Time", 68f, yPos + 15f, paint)
            canvas.drawText("Customer & Area", 160f, yPos + 15f, paint)
            canvas.drawText("Items Summary", 310f, yPos + 15f, paint)
            canvas.drawText("Payment", 460f, yPos + 15f, paint)
            canvas.drawText("Amount", pageWidth - 76f, yPos + 15f, paint)

            yPos += 24f

            // Table Rows
            val itemPaint = Paint().apply {
                textSize = 8.5f
                typeface = Typeface.DEFAULT
                color = darkText
            }

            val linePaint = Paint().apply {
                color = borderLine
                strokeWidth = 0.8f
            }

            val maxRowsPerPage = 22
            val displayOrders = orders.take(maxRowsPerPage)

            for ((idx, order) in displayOrders.withIndex()) {
                val rowBg = if (idx % 2 == 0) Color.WHITE else Color.rgb(250, 250, 250)
                paint.color = rowBg
                paint.style = Paint.Style.FILL
                canvas.drawRect(24f, yPos, pageWidth - 24f, yPos + 22f, paint)

                canvas.drawLine(24f, yPos + 22f, pageWidth - 24f, yPos + 22f, linePaint)

                // #ID
                itemPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                itemPaint.color = primaryColor
                canvas.drawText("#${order.orderId}", 28f, yPos + 15f, itemPaint)

                // Date
                itemPaint.typeface = Typeface.DEFAULT
                itemPaint.color = mutedText
                val timeShort = if (order.formattedTime.length > 15) order.formattedTime.take(15) else order.formattedTime
                canvas.drawText(timeShort, 68f, yPos + 15f, itemPaint)

                // Customer
                itemPaint.color = darkText
                val custInfo = "${order.customerName} (${order.areaLandmark.ifBlank { order.deliveryAddress.take(10) }})"
                canvas.drawText(custInfo.take(24), 160f, yPos + 15f, itemPaint)

                // Summary
                itemPaint.color = mutedText
                canvas.drawText(order.itemsSummary.take(26), 310f, yPos + 15f, itemPaint)

                // Payment
                val payMethod = if (order.paymentMethod == PaymentMethod.CASH_ON_DELIVERY) "COD" else "Online"
                canvas.drawText(payMethod, 460f, yPos + 15f, itemPaint)

                // Amount
                itemPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                itemPaint.color = primaryColor
                canvas.drawText("Rs. ${order.totalAmount}", pageWidth - 76f, yPos + 15f, itemPaint)

                yPos += 22f
            }

            // Footer
            paint.color = primaryColor
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, pageHeight - 32f, pageWidth.toFloat(), pageHeight.toFloat(), paint)

            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("Slice Smile Pizza Sadiqabad • Multi-Partner Management • Official Financial Record", 24f, pageHeight - 12f, paint)

            pdfDocument.finishPage(page)

            // Save PDF to reports folder
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val fileName = "SliceSmile_Report_${System.currentTimeMillis()}.pdf"
            val file = File(reportsDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            return file
        } catch (e: Exception) {
            Log.e("InvoicePdfGenerator", "Error generating PDF invoice", e)
            return null
        }
    }

    fun generateSingleOrderInvoice(
        context: Context,
        order: Order
    ): File? {
        return generateInvoiceReport(
            context = context,
            reportTitle = "Customer Order Invoice #${order.orderId}",
            orders = listOf(order),
            filterLabel = "Individual Receipt"
        )
    }

    private fun drawSummaryCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        mainValue: String,
        subValue: String,
        bgColor: Int,
        valueColor: Int
    ) {
        val paint = Paint()
        paint.color = bgColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(x, y, x + width, y + height), 6f, 6f, paint)

        paint.color = Color.rgb(225, 225, 225)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(x, y, x + width, y + height), 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(120, 120, 120)
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(title, x + 8f, y + 15f, paint)

        paint.color = valueColor
        paint.textSize = 12.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(mainValue, x + 8f, y + 32f, paint)

        paint.color = Color.rgb(100, 100, 100)
        paint.textSize = 8f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(subValue, x + 8f, y + 46f, paint)
    }

    fun sharePdf(context: Context, pdfFile: File, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Here is the $title from Slice Smile Pizza Management System.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Share / Download Invoice PDF"))
        } catch (e: Exception) {
            Log.e("InvoicePdfGenerator", "Error sharing PDF", e)
            Toast.makeText(context, "Could not open PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

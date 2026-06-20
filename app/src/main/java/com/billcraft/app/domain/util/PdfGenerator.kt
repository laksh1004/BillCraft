package com.billcraft.app.domain.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.billcraft.app.domain.model.Business
import com.billcraft.app.domain.model.Customer
import com.billcraft.app.domain.model.GSTSummary
import com.billcraft.app.domain.model.Invoice
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

class PdfGenerator(private val context: Context) {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Page dimensions (A4 at 72 dpi)
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 40f

    // Colors
    private val primaryColor = Color.parseColor("#1565C0")
    private val accentColor = Color.parseColor("#0288D1")
    private val lightGray = Color.parseColor("#F5F5F5")
    private val darkGray = Color.parseColor("#424242")
    private val mediumGray = Color.parseColor("#757575")
    private val borderColor = Color.parseColor("#BDBDBD")

    // Paints
    private val titlePaint = Paint().apply {
        color = Color.WHITE
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val headerPaint = Paint().apply {
        color = Color.WHITE
        textSize = 11f
        isAntiAlias = true
    }

    private val boldPaint = Paint().apply {
        color = darkGray
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val normalPaint = Paint().apply {
        color = darkGray
        textSize = 9f
        isAntiAlias = true
    }

    private val smallGrayPaint = Paint().apply {
        color = mediumGray
        textSize = 8f
        isAntiAlias = true
    }

    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val strokePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = borderColor
        strokeWidth = 0.5f
        isAntiAlias = true
    }

    /**
     * Generates a professional GST invoice PDF and saves it to the app's invoices directory.
     * Returns the File object for sharing.
     */
    fun generateInvoicePdf(invoice: Invoice): File {
        val gstSummary = GSTCalculator.calculateGST(invoice.lineItems, invoice.isInterState)
        val document = PdfDocument()

        // Page 1 - Main invoice content
        val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = document.startPage(pageInfo1)
        drawPage1(page1.canvas, invoice, gstSummary)
        document.finishPage(page1)

        // Page 2 (if many line items or terms) - Bank details, terms
        if (invoice.lineItems.size > 8 || !invoice.termsAndConditions.isNullOrBlank()) {
            val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
            val page2 = document.startPage(pageInfo2)
            drawPage2(page2.canvas, invoice, gstSummary)
            document.finishPage(page2)
        }

        // Save to file
        val invoicesDir = File(context.filesDir, "invoices")
        invoicesDir.mkdirs()
        val file = File(invoicesDir, "${invoice.invoiceNumber.replace("/", "-")}.pdf")

        FileOutputStream(file).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        return file
    }

    private fun drawPage1(canvas: Canvas, invoice: Invoice, gstSummary: GSTSummary) {
        var y = 0f

        // ── Header background ──────────────────────────────────────────────
        fillPaint.color = primaryColor
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 120f, fillPaint)

        // Business name
        titlePaint.textSize = 20f
        canvas.drawText(invoice.business?.name ?: "BillCraft", margin, 45f, titlePaint)

        // GST Invoice label
        titlePaint.textSize = 14f
        canvas.drawText(invoice.type.name.uppercase().replace("_", " "), margin, 68f, titlePaint)

        // Invoice number (top right)
        val invoiceNumText = "# ${invoice.invoiceNumber}"
        boldPaint.color = Color.WHITE
        boldPaint.textSize = 11f
        val numWidth = boldPaint.measureText(invoiceNumText)
        canvas.drawText(invoiceNumText, pageWidth - margin - numWidth, 45f, boldPaint)

        // Date (top right)
        headerPaint.textSize = 9f
        val dateText = "Date: ${invoice.invoiceDate.format(dateFormatter)}"
        val dateWidth = headerPaint.measureText(dateText)
        canvas.drawText(dateText, pageWidth - margin - dateWidth, 62f, headerPaint)

        // Due date if present
        invoice.dueDate?.let {
            val dueText = "Due: ${it.format(dateFormatter)}"
            val dueWidth = headerPaint.measureText(dueText)
            canvas.drawText(dueText, pageWidth - margin - dueWidth, 78f, headerPaint)
        }

        // GSTIN (business)
        invoice.business?.gstin?.let {
            headerPaint.textSize = 8f
            canvas.drawText("GSTIN: $it", margin, 85f, headerPaint)
        }

        // Business address
        val addrText = listOf(
            invoice.business?.address,
            invoice.business?.city,
            invoice.business?.state
        ).filterNotNull().filter { it.isNotBlank() }.joinToString(", ")
        if (addrText.isNotBlank()) {
            headerPaint.textSize = 8f
            canvas.drawText(addrText, margin, 100f, headerPaint)
        }

        y = 130f
        boldPaint.color = darkGray
        boldPaint.textSize = 10f
        normalPaint.color = darkGray

        // ── Bill To / Ship To section ──────────────────────────────────────
        val colMid = pageWidth / 2f

        // Bill To label
        fillPaint.color = lightGray
        canvas.drawRect(margin, y, colMid - 10f, y + 16f, fillPaint)
        canvas.drawText("BILL TO", margin + 4f, y + 12f, boldPaint)

        y += 22f
        val customer = invoice.customer
        if (customer != null) {
            boldPaint.textSize = 11f
            canvas.drawText(customer.name, margin, y, boldPaint)
            boldPaint.textSize = 10f
            y += 14f
            customer.gstin?.let { canvas.drawText("GSTIN: $it", margin, y, normalPaint); y += 12f }
            customer.phone.let { if (it.isNotBlank()) { canvas.drawText("Ph: $it", margin, y, normalPaint); y += 12f } }
            customer.email?.let { canvas.drawText(it, margin, y, normalPaint); y += 12f }
            val custAddr = listOf(customer.address, customer.city, customer.state, customer.pincode)
                .filter { it.isNotBlank() }.joinToString(", ")
            if (custAddr.isNotBlank()) {
                canvas.drawText(custAddr, margin, y, normalPaint)
                y += 12f
            }
        }

        y = maxOf(y, 200f) + 16f

        // ── Divider ───────────────────────────────────────────────────────
        strokePaint.color = borderColor
        canvas.drawLine(margin, y, pageWidth - margin, y, strokePaint)
        y += 10f

        // ── Line Items Table Header ────────────────────────────────────────
        val col0 = margin          // #
        val col1 = margin + 20f    // Description
        val col2 = 280f            // HSN
        val col3 = 320f            // Qty
        val col4 = 360f            // Rate
        val col5 = 410f            // Disc%
        val col6 = 455f            // GST%
        val col7 = pageWidth - margin - 55f // Amount

        fillPaint.color = primaryColor
        canvas.drawRect(margin, y, pageWidth - margin, y + 18f, fillPaint)

        boldPaint.color = Color.WHITE
        boldPaint.textSize = 8f
        canvas.drawText("#", col0 + 2f, y + 13f, boldPaint)
        canvas.drawText("Description", col1, y + 13f, boldPaint)
        canvas.drawText("HSN", col2, y + 13f, boldPaint)
        canvas.drawText("Qty", col3, y + 13f, boldPaint)
        canvas.drawText("Rate", col4, y + 13f, boldPaint)
        canvas.drawText("Disc%", col5, y + 13f, boldPaint)
        canvas.drawText("GST%", col6, y + 13f, boldPaint)
        canvas.drawText("Amount", col7, y + 13f, boldPaint)

        y += 22f
        boldPaint.color = darkGray
        boldPaint.textSize = 9f
        normalPaint.textSize = 9f

        // ── Line Items ─────────────────────────────────────────────────────
        invoice.lineItems.forEachIndexed { index, item ->
            val rowBg = if (index % 2 == 0) Color.WHITE else lightGray
            fillPaint.color = rowBg
            canvas.drawRect(margin, y - 2f, pageWidth - margin, y + 14f, fillPaint)

            normalPaint.color = darkGray
            canvas.drawText("${index + 1}", col0 + 2f, y + 10f, normalPaint)
            // Truncate long descriptions
            val desc = if (item.description.length > 22) item.description.take(22) + "…" else item.description
            canvas.drawText(desc, col1, y + 10f, normalPaint)
            canvas.drawText(item.hsnCode ?: "", col2, y + 10f, normalPaint)
            canvas.drawText(String.format("%.2f", item.quantity), col3, y + 10f, normalPaint)
            canvas.drawText(String.format("%.2f", item.pricePerUnit), col4, y + 10f, normalPaint)
            canvas.drawText(String.format("%.1f%%", item.discountPercent), col5, y + 10f, normalPaint)
            canvas.drawText(String.format("%.1f%%", item.gstRate), col6, y + 10f, normalPaint)
            canvas.drawText(CurrencyFormatter.formatWithoutSymbol(item.amount), col7, y + 10f, normalPaint)

            y += 16f

            // Stop if close to bottom, continue on page 2
            if (y > pageHeight - 200f) return@forEachIndexed
        }

        y += 8f
        strokePaint.color = borderColor
        canvas.drawLine(margin, y, pageWidth - margin, y, strokePaint)
        y += 10f

        // ── Totals Section ────────────────────────────────────────────────
        val totalsLeft = pageWidth - 220f
        val totalsRight = pageWidth - margin.toFloat()

        fun drawTotalRow(label: String, value: String, isBold: Boolean = false) {
            if (isBold) {
                boldPaint.textSize = 10f
                boldPaint.color = darkGray
                canvas.drawText(label, totalsLeft, y + 10f, boldPaint)
                boldPaint.color = primaryColor
                canvas.drawText(value, totalsRight - boldPaint.measureText(value), y + 10f, boldPaint)
            } else {
                normalPaint.textSize = 9f
                canvas.drawText(label, totalsLeft, y + 10f, normalPaint)
                canvas.drawText(value, totalsRight - normalPaint.measureText(value), y + 10f, normalPaint)
            }
            y += 14f
        }

        drawTotalRow("Subtotal:", "₹${CurrencyFormatter.formatWithoutSymbol(gstSummary.subtotal)}")

        if (gstSummary.isInterState) {
            gstSummary.gstBreakdown.forEach {
                drawTotalRow("IGST @${it.rate}%:", "₹${CurrencyFormatter.formatWithoutSymbol(it.igst)}")
            }
        } else {
            gstSummary.gstBreakdown.forEach {
                drawTotalRow("CGST @${it.rate / 2}%:", "₹${CurrencyFormatter.formatWithoutSymbol(it.cgst)}")
                drawTotalRow("SGST @${it.rate / 2}%:", "₹${CurrencyFormatter.formatWithoutSymbol(it.sgst)}")
            }
        }

        if (invoice.amountPaid > 0) {
            drawTotalRow("Amount Paid:", "₹${CurrencyFormatter.formatWithoutSymbol(invoice.amountPaid)}")
        }

        // Grand Total
        y += 4f
        fillPaint.color = primaryColor
        canvas.drawRect(totalsLeft - 8f, y - 2f, totalsRight, y + 18f, fillPaint)
        boldPaint.color = Color.WHITE
        boldPaint.textSize = 11f
        canvas.drawText("TOTAL:", totalsLeft, y + 13f, boldPaint)
        val totalStr = "₹${CurrencyFormatter.formatWithoutSymbol(gstSummary.grandTotal)}"
        canvas.drawText(totalStr, totalsRight - boldPaint.measureText(totalStr), y + 13f, boldPaint)
        y += 28f

        // Balance due
        if (invoice.balanceDue > 0) {
            boldPaint.color = Color.parseColor("#D32F2F")
            boldPaint.textSize = 10f
            val balText = "Balance Due: ₹${CurrencyFormatter.formatWithoutSymbol(invoice.balanceDue)}"
            canvas.drawText(balText, totalsLeft, y + 12f, boldPaint)
            y += 20f
        }

        y += 10f

        // Amount in words
        normalPaint.color = mediumGray
        normalPaint.textSize = 8f
        canvas.drawText(
            "Amount in words: ${CurrencyFormatter.toWords(gstSummary.grandTotal)}",
            margin, y + 10f, normalPaint
        )
        y += 24f

        // ── Bank Details ─────────────────────────────────────────────────
        val biz = invoice.business
        if (biz?.hasBankDetails == true || biz?.hasUpi == true) {
            fillPaint.color = lightGray
            canvas.drawRect(margin, y, (pageWidth / 2).toFloat(), y + 90f, fillPaint)

            boldPaint.color = primaryColor
            boldPaint.textSize = 10f
            canvas.drawText("Bank Details", margin + 6f, y + 14f, boldPaint)

            normalPaint.textSize = 9f
            normalPaint.color = darkGray
            var bankY = y + 28f
            biz.bankName?.let { canvas.drawText("Bank: $it", margin + 6f, bankY, normalPaint); bankY += 13f }
            biz.accountNumber?.let { canvas.drawText("A/C: $it", margin + 6f, bankY, normalPaint); bankY += 13f }
            biz.ifscCode?.let { canvas.drawText("IFSC: $it", margin + 6f, bankY, normalPaint); bankY += 13f }
            biz.upiId?.let { canvas.drawText("UPI: $it", margin + 6f, bankY, normalPaint) }
        }

        // ── Notes ─────────────────────────────────────────────────────────
        invoice.notes?.let {
            val notesX = (pageWidth / 2).toFloat() + 10f
            boldPaint.color = primaryColor
            boldPaint.textSize = 10f
            canvas.drawText("Notes", notesX, y + 14f, boldPaint)
            normalPaint.color = darkGray
            normalPaint.textSize = 9f
            canvas.drawText(it.take(200), notesX, y + 28f, normalPaint)
        }

        // ── Footer ────────────────────────────────────────────────────────
        normalPaint.color = mediumGray
        normalPaint.textSize = 8f
        canvas.drawText(
            "Generated by BillCraft • This is a computer generated invoice.",
            margin, pageHeight - 20f, normalPaint
        )

        // Signature placeholder
        boldPaint.color = darkGray
        boldPaint.textSize = 9f
        canvas.drawText("Authorised Signatory", pageWidth - margin - 100f, pageHeight - 30f, boldPaint)
        strokePaint.color = borderColor
        canvas.drawLine(pageWidth - margin - 110f, pageHeight - 50f, pageWidth - margin, pageHeight - 50f, strokePaint)
    }

    private fun drawPage2(canvas: Canvas, invoice: Invoice, gstSummary: GSTSummary) {
        var y = margin

        fillPaint.color = primaryColor
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 50f, fillPaint)
        titlePaint.textSize = 14f
        canvas.drawText("${invoice.invoiceNumber} — Continued", margin, 34f, titlePaint)

        y = 70f

        // Terms and Conditions
        invoice.termsAndConditions?.let { terms ->
            boldPaint.color = primaryColor
            boldPaint.textSize = 11f
            canvas.drawText("Terms & Conditions", margin, y, boldPaint)
            y += 16f

            normalPaint.color = darkGray
            normalPaint.textSize = 9f
            terms.lines().take(20).forEach { line ->
                canvas.drawText(line, margin, y, normalPaint)
                y += 13f
            }
        }

        // GST Summary table
        y += 20f
        boldPaint.color = primaryColor
        boldPaint.textSize = 11f
        canvas.drawText("GST Summary", margin, y, boldPaint)
        y += 16f

        fillPaint.color = primaryColor
        canvas.drawRect(margin, y, 400f, y + 18f, fillPaint)
        boldPaint.color = Color.WHITE
        boldPaint.textSize = 9f
        canvas.drawText("GST Rate", margin + 4f, y + 13f, boldPaint)
        canvas.drawText("Taxable Amt", margin + 80f, y + 13f, boldPaint)
        canvas.drawText("CGST", margin + 170f, y + 13f, boldPaint)
        canvas.drawText("SGST", margin + 225f, y + 13f, boldPaint)
        canvas.drawText("IGST", margin + 280f, y + 13f, boldPaint)
        y += 22f

        gstSummary.gstBreakdown.forEachIndexed { idx, row ->
            fillPaint.color = if (idx % 2 == 0) Color.WHITE else lightGray
            canvas.drawRect(margin, y - 2f, 400f, y + 14f, fillPaint)
            normalPaint.color = darkGray
            normalPaint.textSize = 9f
            canvas.drawText("${row.rate}%", margin + 4f, y + 10f, normalPaint)
            canvas.drawText(CurrencyFormatter.formatWithoutSymbol(row.taxableAmount), margin + 80f, y + 10f, normalPaint)
            canvas.drawText(CurrencyFormatter.formatWithoutSymbol(row.cgst), margin + 170f, y + 10f, normalPaint)
            canvas.drawText(CurrencyFormatter.formatWithoutSymbol(row.sgst), margin + 225f, y + 10f, normalPaint)
            canvas.drawText(CurrencyFormatter.formatWithoutSymbol(row.igst), margin + 280f, y + 10f, normalPaint)
            y += 16f
        }

        // Footer
        normalPaint.color = mediumGray
        normalPaint.textSize = 8f
        canvas.drawText(
            "Generated by BillCraft • ${invoice.invoiceNumber}",
            margin, pageHeight - 20f, normalPaint
        )
    }
}

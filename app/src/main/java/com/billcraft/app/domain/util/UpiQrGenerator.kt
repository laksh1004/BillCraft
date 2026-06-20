package com.billcraft.app.domain.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.net.URLEncoder

object UpiQrGenerator {

    /**
     * Builds a UPI deep-link URI per the NPCI specification.
     *
     * @param upiId      Payee VPA (e.g. "merchant@upi")
     * @param payeeName  Business/payee display name
     * @param amount     Amount in INR (0.00 means open amount)
     * @param note       Transaction note / invoice reference
     */
    fun generateUpiUri(
        upiId: String,
        payeeName: String,
        amount: Double,
        note: String
    ): String {
        val encodedName = URLEncoder.encode(payeeName, "UTF-8")
        val encodedNote = URLEncoder.encode(note, "UTF-8")
        return buildString {
            append("upi://pay?pa=")
            append(upiId)
            append("&pn=")
            append(encodedName)
            if (amount > 0.0) {
                append("&am=")
                append(String.format("%.2f", amount))
            }
            append("&tn=")
            append(encodedNote)
            append("&cu=INR")
        }
    }

    /**
     * Encodes [upiUri] into a square QR-code [Bitmap].
     *
     * @param upiUri The UPI deep-link string to encode.
     * @param size   Side length in pixels of the output bitmap (default 512).
     */
    fun generateQrBitmap(upiUri: String, size: Int = 512): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val bitMatrix = MultiFormatWriter().encode(
            upiUri,
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )
        val bitmap = createBitmap(size, size)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}

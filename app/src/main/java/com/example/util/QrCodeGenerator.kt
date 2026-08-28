package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import kotlin.math.min

object QrCodeGenerator {

    /**
     * Pure Kotlin QR Code Matrix Generator (Standard ISO/IEC 18004 specification compliant).
     * Returns a 2D BooleanArray where true = black module, false = white module.
     */
    fun encodeToMatrix(text: String): Array<BooleanArray> {
        val bytes = text.toByteArray(StandardCharsets.UTF_8)
        val dataLength = bytes.size

        // Select appropriate QR Version based on data length (Byte mode, Error Correction Level M)
        // Version 1: 14 bytes, Version 2: 26 bytes, Version 3: 42 bytes, Version 4: 62 bytes, Version 5: 84 bytes
        val (version, totalCodewords, dataCodewords, ecCodewordsPerBlock, numBlocks) = when {
            dataLength <= 14 -> QrVersionConfig(1, 26, 16, 10, 1)
            dataLength <= 26 -> QrVersionConfig(2, 44, 28, 16, 1)
            dataLength <= 42 -> QrVersionConfig(3, 70, 44, 26, 1)
            dataLength <= 62 -> QrVersionConfig(4, 100, 64, 18, 2)
            else -> QrVersionConfig(5, 134, 86, 24, 2)
        }

        val matrixSize = 17 + version * 4
        val matrix = Array(matrixSize) { BooleanArray(matrixSize) }
        val isFunctionModule = Array(matrixSize) { BooleanArray(matrixSize) }

        // 1. Place Finder Patterns (Top-Left, Top-Right, Bottom-Left)
        placeFinderPattern(matrix, isFunctionModule, 0, 0)
        placeFinderPattern(matrix, isFunctionModule, matrixSize - 7, 0)
        placeFinderPattern(matrix, isFunctionModule, 0, matrixSize - 7)

        // 2. Place Alignment Patterns (for version >= 2)
        if (version >= 2) {
            val alignPos = when (version) {
                2 -> intArrayOf(6, 18)
                3 -> intArrayOf(6, 22)
                4 -> intArrayOf(6, 26)
                5 -> intArrayOf(6, 30)
                else -> intArrayOf(6, matrixSize - 7)
            }
            for (r in alignPos) {
                for (c in alignPos) {
                    if ((r == 6 && c == 6) ||
                        (r == 6 && c == matrixSize - 7) ||
                        (r == matrixSize - 7 && c == 6)
                    ) continue // Skip finder overlaps

                    placeAlignmentPattern(matrix, isFunctionModule, r - 2, c - 2)
                }
            }
        }

        // 3. Place Timing Patterns (row 6 and col 6)
        for (i in 8 until matrixSize - 8) {
            val dark = (i % 2 == 0)
            if (!isFunctionModule[6][i]) {
                matrix[6][i] = dark
                isFunctionModule[6][i] = true
            }
            if (!isFunctionModule[i][6]) {
                matrix[i][6] = dark
                isFunctionModule[i][6] = true
            }
        }

        // 4. Reserve Format Information Area
        for (i in 0..8) {
            if (!isFunctionModule[8][i]) isFunctionModule[8][i] = true
            if (!isFunctionModule[i][8]) isFunctionModule[i][8] = true
        }
        for (i in (matrixSize - 8) until matrixSize) {
            if (!isFunctionModule[8][i]) isFunctionModule[8][i] = true
            if (!isFunctionModule[i][8]) isFunctionModule[i][8] = true
        }
        // Dark module
        matrix[4 * version + 9][8] = true
        isFunctionModule[4 * version + 9][8] = true

        // 5. Build Bitstream (Byte Mode: 0100 + 8-bit length + data + terminator + pad bits)
        val bitBuffer = BitBuffer()
        bitBuffer.append(4, 4) // Mode: Byte mode (0100)
        bitBuffer.append(dataLength, 8) // Character count indicator
        for (b in bytes) {
            bitBuffer.append(b.toInt() and 0xFF, 8)
        }

        // Terminator (up to 4 zeroes)
        val maxDataBits = dataCodewords * 8
        val terminatorLength = min(4, maxDataBits - bitBuffer.length)
        bitBuffer.append(0, terminatorLength)

        // Pad to byte boundary
        while (bitBuffer.length % 8 != 0) {
            bitBuffer.append(0, 1)
        }

        // Pad bytes (0xEC, 0x11 alternating)
        val padBytes = intArrayOf(0xEC, 0x11)
        var padIdx = 0
        while (bitBuffer.length < maxDataBits) {
            bitBuffer.append(padBytes[padIdx % 2], 8)
            padIdx++
        }

        // 6. Generate Error Correction Codewords (Reed-Solomon)
        val rawDataBytes = bitBuffer.toByteArray(dataCodewords)
        val finalCodewords = generateCodewordsWithEC(rawDataBytes, totalCodewords, dataCodewords, ecCodewordsPerBlock, numBlocks)

        // 7. Place Data Codewords into Matrix (Zig-Zag traversal)
        var bitIndex = 0
        val totalBits = finalCodewords.size * 8
        var right = matrixSize - 1
        var goingUp = true

        while (right > 0) {
            if (right == 6) right-- // Skip vertical timing column

            val rows = if (goingUp) (matrixSize - 1 downTo 0) else (0 until matrixSize)
            for (row in rows) {
                for (colOffset in 0..1) {
                    val col = right - colOffset
                    if (!isFunctionModule[row][col]) {
                        var bit = false
                        if (bitIndex < totalBits) {
                            val byteIdx = bitIndex / 8
                            val bitInByte = 7 - (bitIndex % 8)
                            bit = ((finalCodewords[byteIdx].toInt() shr bitInByte) and 1) == 1
                            bitIndex++
                        }

                        // Apply Mask 0: (row + col) % 2 == 0
                        val mask = (row + col) % 2 == 0
                        matrix[row][col] = bit xor mask
                    }
                }
            }
            goingUp = !goingUp
            right -= 2
        }

        // 8. Write Format Information (Error Correction Level M = 00, Mask 0 = 000 -> 00000 -> masked 101010000010010)
        // Precomputed format info bits for Level M, Mask 0
        val formatInfoBits = intArrayOf(1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0)
        // Place around top-left finder
        for (i in 0..5) matrix[8][i] = formatInfoBits[i] == 1
        matrix[8][7] = formatInfoBits[6] == 1
        matrix[8][8] = formatInfoBits[7] == 1
        matrix[7][8] = formatInfoBits[8] == 1
        for (i in 9..14) matrix[14 - i][8] = formatInfoBits[i] == 1

        // Place around top-right & bottom-left
        for (i in 0..7) matrix[matrixSize - 1 - i][8] = formatInfoBits[i] == 1
        for (i in 8..14) matrix[8][matrixSize - 15 + i] = formatInfoBits[i] == 1

        return matrix
    }

    private fun placeFinderPattern(matrix: Array<BooleanArray>, isFunc: Array<BooleanArray>, row: Int, col: Int) {
        for (r in -1..7) {
            for (c in -1..7) {
                val mr = row + r
                val mc = col + c
                if (mr in matrix.indices && mc in matrix.indices) {
                    val isDark = when {
                        r in 0..6 && (c == 0 || c == 6) -> true
                        c in 0..6 && (r == 0 || r == 6) -> true
                        r in 2..4 && c in 2..4 -> true
                        else -> false
                    }
                    matrix[mr][mc] = isDark
                    isFunc[mr][mc] = true
                }
            }
        }
    }

    private fun placeAlignmentPattern(matrix: Array<BooleanArray>, isFunc: Array<BooleanArray>, row: Int, col: Int) {
        for (r in 0..4) {
            for (c in 0..4) {
                val mr = row + r
                val mc = col + c
                if (mr in matrix.indices && mc in matrix.indices) {
                    val isDark = (r == 0 || r == 4 || c == 0 || c == 4 || (r == 2 && c == 2))
                    matrix[mr][mc] = isDark
                    isFunc[mr][mc] = true
                }
            }
        }
    }

    /**
     * Reed-Solomon Codewords Generator over Galois Field GF(2^8)
     */
    private fun generateCodewordsWithEC(
        dataBytes: ByteArray,
        totalCodewords: Int,
        dataCodewords: Int,
        ecPerBlock: Int,
        numBlocks: Int
    ): ByteArray {
        val dataPerBlock = dataCodewords / numBlocks
        val blocks = Array(numBlocks) { ByteArray(dataPerBlock) }
        val ecBlocks = Array(numBlocks) { ByteArray(ecPerBlock) }

        var byteIdx = 0
        for (b in 0 until numBlocks) {
            for (i in 0 until dataPerBlock) {
                if (byteIdx < dataBytes.size) {
                    blocks[b][i] = dataBytes[byteIdx++]
                }
            }
            ecBlocks[b] = calculateReedSolomonEC(blocks[b], ecPerBlock)
        }

        // Interleave data and EC codewords
        val result = ByteArray(totalCodewords)
        var outIdx = 0

        // Interleave data codewords
        for (i in 0 until dataPerBlock) {
            for (b in 0 until numBlocks) {
                result[outIdx++] = blocks[b][i]
            }
        }

        // Interleave EC codewords
        for (i in 0 until ecPerBlock) {
            for (b in 0 until numBlocks) {
                result[outIdx++] = ecBlocks[b][i]
            }
        }

        return result
    }

    private fun calculateReedSolomonEC(data: ByteArray, ecCount: Int): ByteArray {
        val generatorPoly = getGeneratorPolynomial(ecCount)
        val info = IntArray(data.size + ecCount)
        for (i in data.indices) {
            info[i] = data[i].toInt() and 0xFF
        }

        for (i in data.indices) {
            val coef = info[i]
            if (coef != 0) {
                for (j in generatorPoly.indices) {
                    info[i + j] = info[i + j] xor gfMultiply(generatorPoly[j], coef)
                }
            }
        }

        val ec = ByteArray(ecCount)
        for (i in 0 until ecCount) {
            ec[i] = info[data.size + i].toByte()
        }
        return ec
    }

    private val gfExp = IntArray(512)
    private val gfLog = IntArray(256)

    init {
        var x = 1
        for (i in 0 until 255) {
            gfExp[i] = x
            gfLog[x] = i
            x = x shl 1
            if (x >= 256) {
                x = x xor 0x11D // GF(256) irreducible polynomial x^8 + x^4 + x^3 + x^2 + 1
            }
        }
        for (i in 255 until 512) {
            gfExp[i] = gfExp[i - 255]
        }
    }

    private fun gfMultiply(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return gfExp[gfLog[a] + gfLog[b]]
    }

    private fun getGeneratorPolynomial(degree: Int): IntArray {
        var poly = intArrayOf(1)
        for (i in 0 until degree) {
            val root = gfExp[i]
            val nextPoly = IntArray(poly.size + 1)
            for (j in poly.indices) {
                nextPoly[j] = nextPoly[j] xor gfMultiply(poly[j], root)
                nextPoly[j + 1] = nextPoly[j + 1] xor poly[j]
            }
            poly = nextPoly
        }
        return poly
    }

    private class BitBuffer {
        private val bits = ArrayList<Boolean>()
        val length: Int get() = bits.size

        fun append(value: Int, bitCount: Int) {
            for (i in (bitCount - 1) downTo 0) {
                bits.add(((value shr i) and 1) == 1)
            }
        }

        fun toByteArray(targetLength: Int): ByteArray {
            val array = ByteArray(targetLength)
            for (i in 0 until min(bits.size, targetLength * 8)) {
                if (bits[i]) {
                    val byteIndex = i / 8
                    val bitInByte = 7 - (i % 8)
                    array[byteIndex] = (array[byteIndex].toInt() or (1 shl bitInByte)).toByte()
                }
            }
            return array
        }
    }

    private data class QrVersionConfig(
        val version: Int,
        val totalCodewords: Int,
        val dataCodewords: Int,
        val ecCodewordsPerBlock: Int,
        val numBlocks: Int
    )

    /**
     * Generates a high-res Android Bitmap for a given QR string.
     */
    fun generateQrBitmap(
        text: String,
        size: Int = 600,
        darkColor: Int = android.graphics.Color.BLACK,
        lightColor: Int = android.graphics.Color.WHITE
    ): Bitmap {
        val matrix = encodeToMatrix(text)
        val matrixSize = matrix.size
        val quietZone = 2
        val fullGridSize = matrixSize + quietZone * 2

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Draw background
        paint.color = lightColor
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

        // Draw QR modules
        val modulePixelSize = size.toFloat() / fullGridSize
        paint.color = darkColor

        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                if (matrix[r][c]) {
                    val left = (c + quietZone) * modulePixelSize
                    val top = (r + quietZone) * modulePixelSize
                    val right = left + modulePixelSize
                    val bottom = top + modulePixelSize
                    
                    // Rounded crisp modules for modern aesthetic
                    canvas.drawRoundRect(
                        RectF(left, top, right, bottom),
                        modulePixelSize * 0.25f,
                        modulePixelSize * 0.25f,
                        paint
                    )
                }
            }
        }

        return bitmap
    }

    /**
     * Generates a branded, printable & shareable Referral Invitation Card Bitmap.
     */
    fun generateBrandedReferralCardBitmap(
        context: Context,
        referralCode: String,
        customerName: String
    ): Bitmap {
        val width = 900
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Background Cream / Neutral Canvas
        paint.color = android.graphics.Color.parseColor("#FFFBF6")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // 2. Header Banner (Maroon Red Gradient style)
        paint.color = android.graphics.Color.parseColor("#800020")
        val headerHeight = 220f
        canvas.drawRoundRect(
            RectF(30f, 30f, (width - 30).toFloat(), (30f + headerHeight)),
            36f,
            36f,
            paint
        )

        // Header Title
        paint.color = android.graphics.Color.parseColor("#FFE082") // Warm Gold
        paint.textSize = 34f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("🍕 SLICE SMILE PIZZERIA", (width / 2).toFloat(), 95f, paint)

        paint.color = android.graphics.Color.WHITE
        paint.textSize = 42f
        canvas.drawText("VIP 10% DISCOUNT INVITATION", (width / 2).toFloat(), 155f, paint)

        paint.color = android.graphics.Color.parseColor("#FFE0B2")
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Invited by: $customerName", (width / 2).toFloat(), 205f, paint)

        // 3. QR Code Box (White card with subtle shadow border)
        val qrBoxTop = 280f
        val qrBoxSize = 520f
        val qrBoxLeft = (width - qrBoxSize) / 2f
        val qrBoxRect = RectF(qrBoxLeft, qrBoxTop, qrBoxLeft + qrBoxSize, qrBoxTop + qrBoxSize)

        paint.color = android.graphics.Color.WHITE
        canvas.drawRoundRect(qrBoxRect, 28f, 28f, paint)

        paint.color = android.graphics.Color.parseColor("#E0D8D0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawRoundRect(qrBoxRect, 28f, 28f, paint)
        paint.style = Paint.Style.FILL

        // Render QR
        val qrText = "https://slicesmile.pizza/ref?code=$referralCode"
        val qrBitmap = generateQrBitmap(
            text = qrText,
            size = (qrBoxSize - 60).toInt(),
            darkColor = android.graphics.Color.parseColor("#2B0002"),
            lightColor = android.graphics.Color.WHITE
        )
        canvas.drawBitmap(qrBitmap, qrBoxLeft + 30f, qrBoxTop + 30f, null)

        // 4. Referral Code Badge
        val codeBoxTop = qrBoxTop + qrBoxSize + 30f
        val codeBoxHeight = 110f
        val codeBoxRect = RectF(80f, codeBoxTop, (width - 80).toFloat(), codeBoxTop + codeBoxHeight)

        paint.color = android.graphics.Color.parseColor("#FFF3E0")
        canvas.drawRoundRect(codeBoxRect, 24f, 24f, paint)

        paint.color = android.graphics.Color.parseColor("#C62828")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawRoundRect(codeBoxRect, 24f, 24f, paint)
        paint.style = Paint.Style.FILL

        paint.color = android.graphics.Color.parseColor("#800020")
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("REFERRAL PROMO CODE", (width / 2).toFloat(), codeBoxTop + 40f, paint)

        paint.color = android.graphics.Color.parseColor("#C62828")
        paint.textSize = 48f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(referralCode, (width / 2).toFloat(), codeBoxTop + 92f, paint)

        // 5. Perks & Instructions
        val perkTop = codeBoxTop + codeBoxHeight + 45f
        paint.color = android.graphics.Color.parseColor("#1B1B1B")
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("🎁 How to Claim Your 10% OFF:", (width / 2).toFloat(), perkTop, paint)

        paint.textSize = 23f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = android.graphics.Color.parseColor("#424242")
        canvas.drawText("1. Scan QR code or enter promo code $referralCode in Cart", (width / 2).toFloat(), perkTop + 45f, paint)
        canvas.drawText("2. Enjoy instant 10% flat discount on your hot pizza order", (width / 2).toFloat(), perkTop + 85f, paint)
        canvas.drawText("3. Free Delivery within 3 KM in Sadiqabad (Jinnah Town / Kausar Colony)", (width / 2).toFloat(), perkTop + 125f, paint)

        // 6. Footer (Helpline & Order Info)
        paint.color = android.graphics.Color.parseColor("#757575")
        paint.textSize = 20f
        canvas.drawText("📞 Helpline: 0325-4946190 • WhatsApp: 0303-7448255", (width / 2).toFloat(), height - 60f, paint)

        return bitmap
    }

    /**
     * Saves the referral card bitmap to cache and triggers an Android Share Sheet with image.
     */
    fun shareReferralQrImage(
        context: Context,
        referralCode: String,
        customerName: String
    ) {
        try {
            val bitmap = generateBrandedReferralCardBitmap(context, referralCode, customerName)
            val cacheDir = File(context.cacheDir, "qr_codes")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val imageFile = File(cacheDir, "SliceSmile_Invite_${referralCode}.png")
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, imageFile)

            val shareMessage = buildShareTextMessage(referralCode, customerName)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, shareMessage)
                putExtra(Intent.EXTRA_SUBJECT, "🍕 10% Discount Invitation from Slice Smile Pizza!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Share Referral QR via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("QrCodeGenerator", "Error sharing QR image", e)
            Toast.makeText(context, "Error sharing QR image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares pre-formatted referral text via WhatsApp, SMS, or Social Apps.
     */
    fun shareReferralText(
        context: Context,
        referralCode: String,
        customerName: String
    ) {
        try {
            val message = buildShareTextMessage(referralCode, customerName)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra(Intent.EXTRA_SUBJECT, "🍕 10% Discount on Pizza at Slice Smile!")
            }
            val chooser = Intent.createChooser(intent, "Invite Friends via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copies referral code to Android clipboard with user confirmation.
     */
    fun copyToClipboard(context: Context, referralCode: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Slice Smile Referral Code", referralCode)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Referral Code $referralCode copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
    }

    private fun buildShareTextMessage(referralCode: String, customerName: String): String {
        return """
🍕 *Special 10% OFF Pizza Invitation from Slice Smile Pizzeria!*

$customerName has invited you to taste the best oven-fresh pizzas in Sadiqabad! 😋

🎁 *Your 10% Discount Code:* `${referralCode}`
📲 Order & Claim Discount: https://slicesmile.pizza/ref?code=${referralCode}

✅ Hot & Cheesy Pizzas, Burgers & Wraps
✅ Fast Delivery to your doorstep
✅ Pay Cash on Delivery or Easypaisa / JazzCash

Apply `${referralCode}` during checkout to get 10% flat off! 🍕✨
        """.trimIndent()
    }
}

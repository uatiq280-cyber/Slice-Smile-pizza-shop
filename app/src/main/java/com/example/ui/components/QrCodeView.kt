package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryRed
import com.example.util.QrCodeGenerator

@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    darkColor: Color = PolishMaroonDark,
    lightColor: Color = Color.White,
    showCenterBadge: Boolean = true
) {
    val matrix = remember(data) {
        try {
            QrCodeGenerator.encodeToMatrix(data)
        } catch (e: Exception) {
            // Fallback 21x21 empty matrix if any parsing error
            Array(21) { BooleanArray(21) }
        }
    }

    val matrixSize = matrix.size
    val quietZone = 2
    val totalGrid = matrixSize + quietZone * 2

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(20.dp))
            .background(lightColor)
            .border(1.dp, Color(0xFFE5DCD3), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val moduleSize = canvasWidth / totalGrid.toFloat()

            // Draw modules
            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
                    if (matrix[r][c]) {
                        val left = (c + quietZone) * moduleSize
                        val top = (r + quietZone) * moduleSize

                        // Subtle rounded modules for premium craft feel
                        drawRoundRect(
                            color = darkColor,
                            topLeft = Offset(left, top),
                            size = Size(moduleSize, moduleSize),
                            cornerRadius = CornerRadius(moduleSize * 0.28f, moduleSize * 0.28f)
                        )
                    }
                }
            }
        }

        // Center Pizza Logo Badge
        if (showCenterBadge) {
            Box(
                modifier = Modifier
                    .size(size * 0.22f)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, PolishPrimaryRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🍕",
                    fontSize = (size.value * 0.11f).sp
                )
            }
        }
    }
}

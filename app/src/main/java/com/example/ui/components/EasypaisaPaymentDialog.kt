package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.MenuDataSource
import com.example.ui.theme.EasypaisaGreen
import com.example.ui.theme.PolishBgLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryContainer
import com.example.ui.theme.PolishPrimaryContainerSubtle
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.WhatsAppGreen

@Composable
fun EasypaisaPaymentDialog(
    totalPayable: Int,
    currentTrxId: String,
    onTrxIdChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirmOrder: () -> Unit
) {
    val context = LocalContext.current
    var trxInput by remember { mutableStateOf(currentTrxId) }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Easypaisa Account", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Easypaisa number copied: $text", Toast.LENGTH_SHORT).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(26.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EasypaisaGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = EasypaisaGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Easypaisa Payment",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = PolishMaroonDark
                                )
                            )
                            Text(
                                text = "Online Food Order Payment",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EasypaisaGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_easypaisa_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = PolishTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Account Information Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PolishPrimaryContainerSubtle),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Amount Payable:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PolishTextMuted
                            )
                            Text(
                                text = "Rs. $totalPayable",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = PolishPrimaryRed
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Account Number with Copy button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .border(1.dp, PolishBorder, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Easypaisa Account Number",
                                    style = MaterialTheme.typography.labelSmall.copy(color = PolishTextMuted)
                                )
                                Text(
                                    text = MenuDataSource.EASYPAISA_ACCOUNT_NUMBER,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp,
                                        color = EasypaisaGreen
                                    )
                                )
                                Text(
                                    text = "Title: ${MenuDataSource.EASYPAISA_ACCOUNT_TITLE}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PolishMaroonDark
                                    )
                                )
                            }

                            Button(
                                onClick = { copyToClipboard(MenuDataSource.EASYPAISA_ACCOUNT_NUMBER) },
                                colors = ButtonDefaults.buttonColors(containerColor = EasypaisaGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("copy_easypaisa_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Steps in Urdu / English
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PolishBgLight)
                        .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "ادائیگی کا طریقہ کار (How to Pay):",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishMaroonDark
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. اپنی Easypaisa App کھولیں یا *786# ملائیں۔\n2. Rs. $totalPayable رقم اس نمبر 03254946190 پر ٹرانسفر کریں۔\n3. موصول ہونے والی TRX ID نیچے درج کریں۔",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PolishTextMuted,
                            lineHeight = 18.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // TRX ID input
                OutlinedTextField(
                    value = trxInput,
                    onValueChange = {
                        trxInput = it
                        onTrxIdChanged(it)
                    },
                    label = { Text("Transaction ID / TRX ID (e.g. 2938472910)") },
                    placeholder = { Text("Enter Easypaisa TRX ID") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("easypaisa_trx_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        unfocusedBorderColor = PolishBorder,
                        focusedContainerColor = PolishBgLight,
                        unfocusedContainerColor = PolishBgLight
                    ),
                    trailingIcon = {
                        if (trxInput.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EasypaisaGreen
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // WhatsApp Receipt Button
                OutlinedButton(
                    onClick = {
                        WhatsAppOrderHelper.sendRawWhatsAppMessage(
                            context,
                            MenuDataSource.PRIMARY_WHATSAPP,
                            "Salam! I am sending Easypaisa payment of Rs. $totalPayable to 03254946190. My TRX ID is: ${trxInput.ifBlank { "Attached below" }}"
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = WhatsAppGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Send Screenshot / TRX on WhatsApp",
                        color = WhatsAppGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Confirm button
                Button(
                    onClick = {
                        onTrxIdChanged(trxInput)
                        onConfirmOrder()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PolishMaroonDark,
                        contentColor = PolishPrimaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                        .testTag("confirm_easypaisa_order_btn")
                ) {
                    Text(
                        text = "Confirm & Place Order (Rs. $totalPayable)",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = PolishPrimaryContainer
                    )
                }
            }
        }
    }
}


package com.billcraft.app.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AmountText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    fontWeight: FontWeight? = FontWeight.Bold
) {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 2
    format.minimumFractionDigits = 2
    
    val formattedAmount = format.format(amount)
    
    Text(
        text = formattedAmount,
        modifier = modifier,
        style = style,
        fontWeight = fontWeight
    )
}

package com.billcraft.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.billcraft.app.presentation.theme.*

enum class InvoiceStatus {
    DRAFT, SENT, PAID, PENDING, OVERDUE
}

@Composable
fun StatusChip(status: InvoiceStatus, modifier: Modifier = Modifier) {
    val backgroundColor: Color
    val textColor: Color
    
    when (status) {
        InvoiceStatus.PAID -> {
            backgroundColor = PaidGreenContainer
            textColor = PaidGreen
        }
        InvoiceStatus.PENDING -> {
            backgroundColor = PendingAmberContainer
            textColor = PendingAmber
        }
        InvoiceStatus.OVERDUE -> {
            backgroundColor = OverdueRedContainer
            textColor = OverdueRed
        }
        InvoiceStatus.DRAFT -> {
            backgroundColor = DraftGreyContainer
            textColor = DraftGrey
        }
        InvoiceStatus.SENT -> {
            backgroundColor = SentBlueContainer
            textColor = SentBlue
        }
    }
    
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.name,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

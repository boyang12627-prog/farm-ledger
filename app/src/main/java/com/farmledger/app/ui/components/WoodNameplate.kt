package com.farmledger.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Wood = Color(0xFF6B4A2E)
private val Cream = Color(0xFFF5E6C8)
private val Ink = Color(0xFF3D2A1A)

/**
 * 木名牌：分類名＋最近一筆摘要；浮喺熱區上方，唔搶底欄。
 */
@Composable
fun WoodNameplate(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier
            .clip(shape)
            .background(Cream)
            .border(2.dp, Wood, shape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            color = Ink,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                subtitle,
                color = Ink.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

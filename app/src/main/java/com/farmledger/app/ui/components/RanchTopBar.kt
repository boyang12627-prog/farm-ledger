package com.farmledger.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.farmledger.app.R

private val Wood = Color(0xFF6B4A2E)
private val Cream = Color(0xFFF5E6C8)
private val Ink = Color(0xFF3D2A1A)

/**
 * 頂欄真實 Compose widgets（季節／天氣／連續記帳／種子幣），
 * 唔印死喺牧場底圖。
 */
@Composable
fun RanchTopBar(
    seasonLine: String,
    weatherOrPhase: String,
    streakDays: Int,
    seedCoins: Int,
    hkdMonthSummary: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WoodChip(Modifier.weight(1.2f)) {
            Text(seasonLine, color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            Text(weatherOrPhase, color = Ink.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
        }
        WoodChip {
            Text("連續 $streakDays 日", color = Ink, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
        }
        WoodChip {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(R.drawable.ic_seed),
                    contentDescription = "種子幣",
                    modifier = Modifier.size(16.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.width(4.dp))
                Text("$seedCoins", color = Ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
        }
        if (hkdMonthSummary != null) {
            WoodChip {
                Text(hkdMonthSummary, color = Ink, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
    }
}

@Composable
private fun WoodChip(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier
            .clip(shape)
            .background(Cream)
            .border(1.5.dp, Wood, shape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

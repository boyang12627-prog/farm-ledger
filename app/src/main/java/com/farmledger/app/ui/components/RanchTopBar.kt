package com.farmledger.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmledger.app.R

private val Ink = Color(0xFF3D2A1A)

/**
 * A2c 頂欄：季節章／天氣／連續日記牌／種子幣袋（真圖 chrome，字用 Compose 叠）。
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
        Box(Modifier.size(width = 56.dp, height = 48.dp), contentAlignment = Alignment.Center) {
            Image(
                painterResource(R.drawable.topbar_season_stamp_spring),
                contentDescription = "季節",
                modifier = Modifier.fillMaxWidth().height(48.dp),
                contentScale = ContentScale.Fit
            )
        }
        Box(Modifier.size(40.dp), contentAlignment = Alignment.BottomCenter) {
            Image(
                painterResource(R.drawable.topbar_weather_sunny),
                contentDescription = weatherOrPhase,
                modifier = Modifier.size(36.dp),
                contentScale = ContentScale.Fit
            )
            Text(weatherOrPhase.take(2), color = Ink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            Modifier
                .weight(1f)
                .height(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painterResource(R.drawable.topbar_streak_plaque),
                contentDescription = "連續記帳",
                modifier = Modifier.fillMaxWidth().height(44.dp),
                contentScale = ContentScale.FillBounds
            )
            Text(
                "連續 $streakDays 日・$seasonLine",
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
        Box(Modifier.width(64.dp).height(48.dp), contentAlignment = Alignment.Center) {
            Image(
                painterResource(R.drawable.topbar_seed_coin_pouch),
                contentDescription = "種子幣",
                modifier = Modifier.fillMaxWidth().height(48.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                "$seedCoins",
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
            )
        }
        if (hkdMonthSummary != null) {
            Text(
                hkdMonthSummary,
                color = Ink,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

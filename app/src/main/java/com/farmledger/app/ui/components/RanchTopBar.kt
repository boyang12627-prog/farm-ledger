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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmledger.app.R

private val Ink = Color(0xFF3D2A1A)

/**
 * A2c 橫屏頂欄：浮空 chip 左右分（季節／天氣｜連續｜種子幣｜設定），
 * **唔做通欄實心條**；真帳 HKD 唔出現喺牧場 HUD（見 A2c_landscape_label_spec §4）。
 *
 * 季節章／天氣 = 圖 only（contentDescription 做 a11y）；連續牌只顯示 streak 短文；
 * 種子袋保留數字。
 */
@Composable
fun RanchTopBar(
    seasonLine: String,
    weatherOrPhase: String,
    streakDays: Int,
    seedCoins: Int,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左：季節章＋天氣（浮空，圖 only）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(Modifier.size(width = 52.dp, height = 44.dp), contentAlignment = Alignment.Center) {
                Image(
                    painterResource(R.drawable.topbar_season_stamp_spring),
                    contentDescription = seasonLine,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                Image(
                    painterResource(R.drawable.topbar_weather_sunny),
                    contentDescription = weatherOrPhase,
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // 中：連續日記牌（只 streak 短文，唔塞 seasonLine）
        Box(
            Modifier
                .weight(1f)
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painterResource(R.drawable.topbar_streak_plaque),
                contentDescription = "連續記帳",
                // Asset is tall (320×250); Fit would shrink it to ~56dp wide and
                // leave Compose Text outside the visible wood — stretch to fill.
                modifier = Modifier.width(260.dp).height(48.dp),
                contentScale = ContentScale.FillBounds
            )
            Text(
                text = "連續 $streakDays 日",
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 2,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
                modifier = Modifier
                    .width(220.dp)
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        // 右：種子幣袋（只種子幣數字）
        Box(Modifier.width(56.dp).height(44.dp), contentAlignment = Alignment.Center) {
            Image(
                painterResource(R.drawable.topbar_seed_coin_pouch),
                contentDescription = "種子幣",
                modifier = Modifier.fillMaxWidth().height(44.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                "$seedCoins",
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp)
            )
        }

        if (onOpenSettings != null) {
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "設定",
                    tint = Ink.copy(alpha = 0.75f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

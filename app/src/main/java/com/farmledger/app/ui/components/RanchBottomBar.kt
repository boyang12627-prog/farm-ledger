package com.farmledger.app.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import com.farmledger.app.R

private val Wood = Color(0xFF6B4A2E)
private val WoodDark = Color(0xFF4A321F)
private val Cream = Color(0xFFF5E6C8)
private val Ink = Color(0xFF3D2A1A)
private val StampRed = Color(0xFFC45C4A)
private val Growth = Color(0xFF8FBF6A)

enum class RanchTab(val label: String, val iconRes: Int) {
    RANCH("牧場", R.drawable.nav_ranch),
    ENTRY("入帳", R.drawable.nav_entry),
    LEDGER("帳簿", R.drawable.nav_ledger),
    DIARY("日記", R.drawable.nav_diary)
}

/**
 * 四格獨立木框 Tab＋中央預留＋；選中＝深木＋印章紅底線。
 */
@Composable
fun RanchBottomBar(
    selected: RanchTab,
    onSelect: (RanchTab) -> Unit,
    onCenterFab: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .background(Cream)
            .border(1.5.dp, Wood)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabBtn(RanchTab.RANCH, selected == RanchTab.RANCH) { onSelect(RanchTab.RANCH) }
            TabBtn(RanchTab.ENTRY, selected == RanchTab.ENTRY) { onSelect(RanchTab.ENTRY) }
            // 中央預留位
            Spacer(Modifier.width(52.dp))
            TabBtn(RanchTab.LEDGER, selected == RanchTab.LEDGER) { onSelect(RanchTab.LEDGER) }
            TabBtn(RanchTab.DIARY, selected == RanchTab.DIARY) { onSelect(RanchTab.DIARY) }
        }
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 2.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Growth)
                .border(2.dp, Wood, CircleShape)
                .clickable(onClick = onCenterFab),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "快速入帳", tint = Ink)
        }
    }
}

@Composable
private fun TabBtn(tab: RanchTab, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    val bg = if (selected) WoodDark else Wood.copy(alpha = 0.75f)
    Column(
        Modifier
            .clip(shape)
            .background(bg)
            .border(1.5.dp, if (selected) StampRed else Wood, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painterResource(tab.iconRes),
            contentDescription = tab.label,
            modifier = Modifier.size(22.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            tab.label,
            color = Cream,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
        if (selected) {
            Spacer(Modifier.height(2.dp))
            Box(
                Modifier
                    .width(22.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(StampRed)
            )
        }
    }
}

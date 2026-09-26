package com.farmledger.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.farmledger.app.R

private val Wood = Color(0xFF6B4A2E)
private val Cream = Color(0xFFF5E6C8)
private val Ink = Color(0xFF3D2A1A)
private val Growth = Color(0xFF8FBF6A)

enum class RanchTab(val label: String) {
    RANCH("牧場"),
    ENTRY("入帳"),
    LEDGER("帳簿"),
    DIARY("日記")
}

private fun tabDrawable(tab: RanchTab, selected: Boolean): Int = when (tab) {
    RanchTab.RANCH -> if (selected) R.drawable.tab_ranch_selected else R.drawable.tab_ranch_unselected
    RanchTab.ENTRY -> if (selected) R.drawable.tab_entry_selected else R.drawable.tab_entry_unselected
    RanchTab.LEDGER -> if (selected) R.drawable.tab_ledger_selected else R.drawable.tab_ledger_unselected
    RanchTab.DIARY -> if (selected) R.drawable.tab_diary_selected else R.drawable.tab_diary_unselected
}

/**
 * A2c 底欄：矮木條＋四 Tab（橫屏高度約 56–64dp，唔遮熱區中下帶）。
 * 見 A2c_landscape_label_spec §1／§4。
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
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().height(56.dp),  // landscape strip 56–64dp

            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabImg(RanchTab.RANCH, selected == RanchTab.RANCH) { onSelect(RanchTab.RANCH) }
            TabImg(RanchTab.ENTRY, selected == RanchTab.ENTRY) { onSelect(RanchTab.ENTRY) }
            Spacer(Modifier.width(52.dp))
            TabImg(RanchTab.LEDGER, selected == RanchTab.LEDGER) { onSelect(RanchTab.LEDGER) }
            TabImg(RanchTab.DIARY, selected == RanchTab.DIARY) { onSelect(RanchTab.DIARY) }
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
private fun TabImg(tab: RanchTab, selected: Boolean, onClick: () -> Unit) {
    Image(
        painter = painterResource(tabDrawable(tab, selected)),
        contentDescription = tab.label,
        modifier = Modifier
            .width(64.dp)
            .height(48.dp)
            .clickable(onClick = onClick),
        contentScale = ContentScale.Fit
    )
}

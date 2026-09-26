package com.farmledger.app.ui.screens.farm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmledger.app.R
import com.farmledger.app.domain.model.LedgerCategory

/** A1 底圖邏輯尺寸（見 docs/art/a1/A1_assets.md） */
private const val RanchW = 1080f
private const val RanchH = 1200f
private const val HotspotLogical = 160f

/**
 * 春季牧場主畫面：A1 `spring_ranch_base`＋九分類熱區。
 * 漏記分類喺熱區旁顯示長草佔位；有記則收草。
 * Tap 熱區 → 入帳預填分類。
 */
@Composable
fun RanchHotspotScene(
    modifier: Modifier = Modifier,
    missedCategories: Set<LedgerCategory> = LedgerCategory.entries.toSet(),
    onHotspotTap: (LedgerCategory) -> Unit = {},
    showOverlayHint: Boolean = false,
) {
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .aspectRatio(RanchW / RanchH)
    ) {
        val density = LocalDensity.current
        val boxW = maxWidth
        val boxH = maxHeight
        val scaleX = with(density) { boxW.toPx() / RanchW }
        val scaleY = with(density) { boxH.toPx() / RanchH }
        val hotspotDp = with(density) { (HotspotLogical * scaleX).toDp() }

        Image(
            painter = painterResource(R.drawable.spring_ranch_base),
            contentDescription = "春季牧場",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        if (showOverlayHint) {
            Image(
                painter = painterResource(R.drawable.hotspots_overlay),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.35f),
                contentScale = ContentScale.FillBounds
            )
        }

        RanchHotspots.forEach { spot ->
            val cxPx = spot.cx * scaleX
            val cyPx = spot.cy * scaleY
            val half = with(density) { hotspotDp.toPx() / 2f }
            Box(
                Modifier
                    .offset {
                        IntOffset((cxPx - half).toInt(), (cyPx - half).toInt())
                    }
                    .size(hotspotDp)
                    .clip(CircleShape)
                    .semantics { contentDescription = "${spot.category.nameZh}・${spot.category.farmObjectZh}" }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onHotspotTap(spot.category) },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(spot.drawableRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            if (spot.category in missedCategories) {
                // 長草佔位：熱區右下小草叢
                val grassSize = hotspotDp * 0.42f
                Box(
                    Modifier
                        .offset {
                            IntOffset(
                                (cxPx + half * 0.15f).toInt(),
                                (cyPx + half * 0.25f).toInt()
                            )
                        }
                        .size(grassSize)
                ) {
                    MissedGrassMarker(Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun MissedGrassMarker(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val green = Color(0xFF7CB86A)
        val dark = Color(0xFF5A9A4A)
        fun blade(baseX: Float, tipX: Float, tipY: Float, color: Color) {
            val path = Path().apply {
                moveTo(baseX, h)
                quadraticBezierTo(baseX + (tipX - baseX) * 0.3f, h * 0.55f, tipX, tipY)
                quadraticBezierTo(baseX + (tipX - baseX) * 0.55f, h * 0.7f, baseX + w * 0.06f, h)
                close()
            }
            drawPath(path, color)
        }
        blade(w * 0.15f, w * 0.08f, h * 0.12f, dark)
        blade(w * 0.35f, w * 0.38f, h * 0.05f, green)
        blade(w * 0.55f, w * 0.62f, h * 0.15f, dark)
        blade(w * 0.72f, w * 0.78f, h * 0.22f, green)
        drawCircle(Color(0xFFC45C4A).copy(alpha = 0.55f), radius = w * 0.12f, center = Offset(w * 0.85f, h * 0.2f))
    }
    // 細字提示（無障礙以外視覺補強）
    Box(modifier, contentAlignment = Alignment.BottomCenter) {
        Text("草", fontSize = 8.sp, color = Color(0xFF3D2A1A).copy(alpha = 0.0f))
    }
}

data class RanchHotspot(
    val category: LedgerCategory,
    val cx: Float,
    val cy: Float,
    val drawableRes: Int
)

/** A1 鎖定座標（相對 1080×1200） */
val RanchHotspots: List<RanchHotspot> = listOf(
    RanchHotspot(LedgerCategory.FOOD, 220f, 780f, R.drawable.hotspot_food_table_stove),
    RanchHotspot(LedgerCategory.TRANSPORT, 540f, 860f, R.drawable.hotspot_transit_path_bike),
    RanchHotspot(LedgerCategory.HOUSING, 780f, 620f, R.drawable.hotspot_home_porch),
    RanchHotspot(LedgerCategory.DAILY, 130f, 920f, R.drawable.hotspot_daily_crate),
    RanchHotspot(LedgerCategory.ENTERTAINMENT, 900f, 820f, R.drawable.hotspot_fun_garden_pond),
    RanchHotspot(LedgerCategory.HEALTH, 380f, 700f, R.drawable.hotspot_health_herbs),
    RanchHotspot(LedgerCategory.INCOME, 650f, 740f, R.drawable.hotspot_income_mail_basket),
    RanchHotspot(LedgerCategory.SAVINGS, 430f, 900f, R.drawable.hotspot_save_piggy),
    RanchHotspot(LedgerCategory.OTHER, 960f, 680f, R.drawable.hotspot_other_sign),
)

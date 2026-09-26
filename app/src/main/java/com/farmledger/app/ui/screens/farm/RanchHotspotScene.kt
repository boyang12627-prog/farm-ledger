package com.farmledger.app.ui.screens.farm

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.farmledger.app.R
import com.farmledger.app.domain.model.LedgerCategory

/**
 * A2b 底圖邏輯尺寸（docs/art/a2/hotspot_map.json size_px 1280×720）
 * 座標用 hotspots_normalized fx/fy（0–1 中心點）
 */
private const val RanchW = 1280f
private const val RanchH = 720f
/** 熱區點擊直徑（邏輯 px） */
private const val HotspotLogical = 150f
/** 漏記草可視圓略大於熱區 */
private const val GrassLogical = 180f

/**
 * 春季牧場主畫面：A2b `spring_ranch_iso`＋九分類熱區（正規化座標）。
 * 漏記：用同尺寸 `overlay_missed_grass` 對齊裁切疊喺熱區；有記唔顯示。
 * Tap 熱區 → 入帳預填分類（儲蓄＝TRANSFER）。
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
        val grassDp = with(density) { (GrassLogical * scaleX).toDp() }

        Image(
            painter = painterResource(R.drawable.spring_ranch_iso),
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
            val cxPx = spot.fx * RanchW * scaleX
            val cyPx = spot.fy * RanchH * scaleY
            val half = with(density) { hotspotDp.toPx() / 2f }
            val grassHalf = with(density) { grassDp.toPx() / 2f }

            if (spot.category in missedCategories) {
                // 對齊全圖 overlay：圓窗裁切，漏記草同底圖座標一致
                Box(
                    Modifier
                        .offset {
                            IntOffset((cxPx - grassHalf).toInt(), (cyPx - grassHalf).toInt())
                        }
                        .size(grassDp)
                        .clip(CircleShape)
                ) {
                    Image(
                        painter = painterResource(R.drawable.overlay_missed_grass),
                        contentDescription = "漏記長草",
                        modifier = Modifier
                            .requiredSize(boxW, boxH)
                            .offset {
                                IntOffset(-(cxPx - grassHalf).toInt(), -(cyPx - grassHalf).toInt())
                            },
                        contentScale = ContentScale.FillBounds
                    )
                }
            }

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
                // 透明可點熱區；可讀性靠 A2b 底圖道具
                Box(Modifier.fillMaxSize().alpha(0.01f))
            }
        }
    }
}

data class RanchHotspot(
    val category: LedgerCategory,
    /** 正規化中心 X（0–1），見 hotspot_map.json */
    val fx: Float,
    /** 正規化中心 Y（0–1） */
    val fy: Float,
)

/**
 * A2b 鎖定熱區（docs/art/a2/hotspot_map.json hotspots_normalized）
 */
val RanchHotspots: List<RanchHotspot> = listOf(
    RanchHotspot(LedgerCategory.FOOD, 0.42f, 0.40f),
    RanchHotspot(LedgerCategory.TRANSPORT, 0.28f, 0.48f),
    RanchHotspot(LedgerCategory.HOUSING, 0.55f, 0.36f),
    RanchHotspot(LedgerCategory.DAILY, 0.22f, 0.50f),
    RanchHotspot(LedgerCategory.ENTERTAINMENT, 0.18f, 0.72f),
    RanchHotspot(LedgerCategory.HEALTH, 0.30f, 0.62f),
    RanchHotspot(LedgerCategory.INCOME, 0.58f, 0.52f),
    RanchHotspot(LedgerCategory.SAVINGS, 0.72f, 0.62f),
    RanchHotspot(LedgerCategory.OTHER, 0.82f, 0.72f),
)

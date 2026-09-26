package com.farmledger.app.ui.screens.farm

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmledger.app.R
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.usecase.HotspotInteractionLogic
import com.farmledger.app.domain.usecase.HotspotVisualState
import com.farmledger.app.ui.components.WoodNameplate
import kotlinx.coroutines.delay

/** A2b 底圖邏輯尺寸（docs/art/a2/hotspot_map.json size_px 1280×720） */
private const val RanchW = 1280f
private const val RanchH = 720f
/** 物件精靈相對寬度比例 ~0.12–0.18 */
private const val SpriteScale = 0.15f
private val AmberRim = Color(0xFFE8A838)
private val ActiveGlow = Color(0xFFFFF3C4)
private val LabelBg = Color(0xFFF5E6C8)
private val LabelInk = Color(0xFF3D2A1A)
private val Wood = Color(0xFF6B4A2E)

/**
 * 春季牧場：iso 底圖＋九個**可見**物件層（idle／可點／按下／禁用）。
 * 漏記：堆疊草層 1–min(N,3)。點擊：≤200ms 縮放＋木名牌＋haptic，再回調入帳。
 * 浮標短 label 畫喺精靈之下、偏上，唔擋點擊。
 */
@Composable
fun RanchHotspotScene(
    modifier: Modifier = Modifier,
    missedCategories: Set<LedgerCategory> = LedgerCategory.entries.toSet(),
    weedStacks: Map<LedgerCategory, Int> = emptyMap(),
    loggedToday: Set<LedgerCategory> = emptySet(),
    activeAccountCount: Int = 2,
    latestSummaries: Map<LedgerCategory, String?> = emptyMap(),
    onHotspotTap: (LedgerCategory) -> Unit = {},
    showOverlayHint: Boolean = false,
) {
    var nameplateCat by remember { mutableStateOf<LedgerCategory?>(null) }

    LaunchedEffect(nameplateCat) {
        if (nameplateCat != null) {
            delay(1400)
            nameplateCat = null
        }
    }

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .aspectRatio(RanchW / RanchH)
    ) {
        val density = LocalDensity.current
        val boxW = maxWidth
        val scaleX = with(density) { boxW.toPx() / RanchW }
        val scaleY = with(density) { maxHeight.toPx() / RanchH }
        val spriteDp = boxW * SpriteScale
        val grassDp = spriteDp * 0.85f
        val halfPx = with(density) { spriteDp.toPx() / 2f }

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

        // 1) 浮標＋草（唔可點，畫喺精靈之下）
        RanchHotspots.forEach { spot ->
            val cxPx = spot.fx * RanchW * scaleX
            val cyPx = spot.fy * RanchH * scaleY
            val stacks = weedStacks[spot.category]
                ?: if (spot.category in missedCategories) 1 else 0

            if (stacks > 0) {
                val gHalf = with(density) { grassDp.toPx() / 2f }
                for (i in 0 until stacks) {
                    val ox = (i - (stacks - 1) / 2f) * 8f
                    val oy = i * 6f
                    Image(
                        painter = painterResource(R.drawable.overlay_missed_grass),
                        contentDescription = if (i == 0) "漏記長草×$stacks" else null,
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (cxPx - gHalf + ox).toInt(),
                                    (cyPx - gHalf + oy + halfPx * 0.35f).toInt()
                                )
                            }
                            .size(grassDp)
                            .alpha(0.85f - i * 0.12f),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            val label = HotspotInteractionLogic.floatingLabel(spot.category)
            val labelHalf = with(density) { 28.dp.toPx() }
            val labelY = cyPx - halfPx - with(density) { 20.dp.toPx() }
            Text(
                text = label,
                color = LabelInk,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .offset {
                        IntOffset((cxPx - labelHalf).toInt(), labelY.toInt())
                    }
                    .clip(RoundedCornerShape(6.dp))
                    .background(LabelBg.copy(alpha = 0.92f))
                    .border(1.dp, Wood, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // 2) 可見可點物件層（最上，接收點擊）
        RanchHotspots.forEach { spot ->
            val cxPx = spot.fx * RanchW * scaleX
            val cyPx = spot.fy * RanchH * scaleY
            HotspotObjectLayer(
                spot = spot,
                cxPx = cxPx,
                cyPx = cyPx,
                halfPx = halfPx,
                spriteDp = spriteDp,
                activeAccountCount = activeAccountCount,
                loggedToday = spot.category in loggedToday,
                onTap = {
                    nameplateCat = spot.category
                    onHotspotTap(spot.category)
                }
            )
        }

        nameplateCat?.let { cat ->
            val spot = RanchHotspots.find { it.category == cat } ?: return@let
            val cxPx = spot.fx * RanchW * scaleX
            val cyPx = spot.fy * RanchH * scaleY
            WoodNameplate(
                title = HotspotInteractionLogic.nameplateTitle(cat),
                subtitle = latestSummaries[cat],
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset {
                        IntOffset(
                            (cxPx - with(density) { 60.dp.toPx() }).toInt().coerceAtLeast(4),
                            (cyPx - halfPx - with(density) { 56.dp.toPx() }).toInt().coerceAtLeast(4)
                        )
                    }
            )
        }
    }
}

@Composable
private fun HotspotObjectLayer(
    spot: RanchHotspot,
    cxPx: Float,
    cyPx: Float,
    halfPx: Float,
    spriteDp: Dp,
    activeAccountCount: Int,
    loggedToday: Boolean,
    onTap: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val enabled = HotspotInteractionLogic.isEnabled(spot.category, activeAccountCount)
    val state = HotspotInteractionLogic.visualState(spot.category, activeAccountCount, pressed && enabled)
    val scale by animateFloatAsState(
        targetValue = when (state) {
            HotspotVisualState.PRESSED -> 0.92f
            HotspotVisualState.DISABLED -> 0.96f
            else -> 1f
        },
        animationSpec = tween(120),
        label = "hotspotScale"
    )
    val greyMatrix = remember {
        ColorMatrix().apply { setToSaturation(0.25f) }
    }

    Box(
        Modifier
            .offset { IntOffset((cxPx - halfPx).toInt(), (cyPx - halfPx).toInt()) }
            .size(spriteDp)
            .scale(scale)
            .semantics {
                contentDescription =
                    "${spot.category.nameZh}・${spot.category.farmObjectZh}" +
                        when (state) {
                            HotspotVisualState.DISABLED -> "（禁用）"
                            HotspotVisualState.PRESSED -> "（按下）"
                            HotspotVisualState.CLICKABLE -> "（可點）"
                            HotspotVisualState.IDLE -> "（可點）"
                        }
            }
            .then(
                when {
                    loggedToday && enabled ->
                        Modifier.border(2.dp, ActiveGlow, RoundedCornerShape(10.dp))
                    state == HotspotVisualState.PRESSED ->
                        Modifier.border(2.5.dp, AmberRim, RoundedCornerShape(10.dp))
                    else -> Modifier
                }
            )
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                onTap()
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(hotspotDrawable(spot.category)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            colorFilter = if (state == HotspotVisualState.DISABLED) {
                ColorFilter.colorMatrix(greyMatrix)
            } else null,
            alpha = if (state == HotspotVisualState.DISABLED) 0.55f else 1f
        )
        if (loggedToday && enabled) {
            Text(
                "✓",
                color = Wood,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

fun hotspotDrawable(category: LedgerCategory): Int = when (category) {
    LedgerCategory.FOOD -> R.drawable.hotspot_food_table_stove
    LedgerCategory.TRANSPORT -> R.drawable.hotspot_transit_path_bike
    LedgerCategory.HOUSING -> R.drawable.hotspot_home_porch
    LedgerCategory.DAILY -> R.drawable.hotspot_daily_crate
    LedgerCategory.ENTERTAINMENT -> R.drawable.hotspot_fun_garden_pond
    LedgerCategory.HEALTH -> R.drawable.hotspot_health_herbs
    LedgerCategory.INCOME -> R.drawable.hotspot_income_mail_basket
    LedgerCategory.SAVINGS -> R.drawable.hotspot_save_piggy
    LedgerCategory.OTHER -> R.drawable.hotspot_other_sign
}

data class RanchHotspot(
    val category: LedgerCategory,
    val fx: Float,
    val fy: Float,
)

/** A2b 鎖定熱區（docs/art/a2/hotspot_map.json hotspots_normalized） */
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

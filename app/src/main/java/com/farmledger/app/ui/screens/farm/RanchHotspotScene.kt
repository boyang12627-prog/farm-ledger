package com.farmledger.app.ui.screens.farm

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.farmledger.app.R
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.usecase.HotspotInteractionLogic
import com.farmledger.app.domain.usecase.HotspotUnlockLogic
import com.farmledger.app.domain.usecase.HotspotVisualState
import com.farmledger.app.ui.components.WhisperLabel
import kotlinx.coroutines.delay

/** A2c／A2b 底圖邏輯尺寸（hotspot_map.json 1280×720＝16:9 landscape） */
private const val RanchW = 1280f
private const val RanchH = 720f
private const val SpriteScale = 0.14f
/** A2c_landscape_label_spec §3 模式 B：長按 ≥400ms 先出 whisper */
private const val WhisperLongPressMs = 400L

/**
 * A2c 可組裝牧場（橫屏）：`spring_ranch_bare_day1`＋熱區三態 drawable
 * ＋ grass_layer 堆疊。默認**無常駐木牌**；長按出 whisper tip（同時最多 1）。
 * 可見性＝shop_day_reached AND owned（[HotspotUnlockLogic]／day1_empty.json）；
 * 開局 owned 空＝淨農地，唔畫灰桩。
 */
@Composable
fun RanchHotspotScene(
    modifier: Modifier = Modifier,
    /** 已購買熱區 id；開局 empty → 零物件 */
    ownedHotspotIds: Set<String> = emptySet(),
    /** 累計日結日（商店上架／渲染閘） */
    totalSettleDays: Int = 0,
    hasReconcileSuccess: Boolean = false,
    /** @deprecated 保留呼叫端相容；唔再用於可見性 */
    ranchDay: Int = 1,
    expenseEntryCount: Int = 0,
    hasSavingsOrReconcileSuccess: Boolean = false,
    missedCategories: Set<LedgerCategory> = LedgerCategory.entries.toSet(),
    weedStacks: Map<LedgerCategory, Int> = emptyMap(),
    loggedToday: Set<LedgerCategory> = emptySet(),
    activeAccountCount: Int = 2,
    onHotspotTap: (LedgerCategory) -> Unit = {},
    showOverlayHint: Boolean = false,
    /** 無障礙／首次教學：常駐細 whisper（默認 off＝模式 A） */
    showHotspotLabels: Boolean = false,
) {
    // 同時最多 1 個 tip
    var whisperCat by remember { mutableStateOf<LedgerCategory?>(null) }
    val shopCtx = remember(ownedHotspotIds, totalSettleDays, hasReconcileSuccess) {
        com.farmledger.app.domain.usecase.ShopContext(
            totalSettleDays = totalSettleDays,
            ownedIds = ownedHotspotIds,
            hasReconcileSuccess = hasReconcileSuccess
        )
    }
    val unlocked = remember(shopCtx) {
        HotspotUnlockLogic.unlockedCategories(shopCtx)
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val boxW = maxWidth
        val scaleX = with(density) { boxW.toPx() / RanchW }
        val scaleY = with(density) { maxHeight.toPx() / RanchH }
        val spriteDp = boxW * SpriteScale
        val halfPx = with(density) { spriteDp.toPx() / 2f }
        val grassDp = spriteDp * 1.05f

        Image(
            painter = painterResource(R.drawable.spring_ranch_bare_day1),
            contentDescription = "春季牧場（淨農地）",
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

        // 漏記草：只 UNLOCKED 熱區（HIDDEN 唔畫草）
        RanchHotspots.forEach { spot ->
            if (spot.category !in unlocked) return@forEach
            val cxPx = spot.fx * RanchW * scaleX
            val cyPx = spot.fy * RanchH * scaleY
            val stacks = weedStacks[spot.category]
                ?: if (spot.category in missedCategories) 1 else 0
            if (stacks > 0) {
                val gHalf = with(density) { grassDp.toPx() / 2f }
                val layers = listOf(
                    R.drawable.overlay_grass_layer_1,
                    R.drawable.overlay_grass_layer_2,
                    R.drawable.overlay_grass_layer_3
                )
                for (i in 0 until stacks.coerceAtMost(3)) {
                    Image(
                        painter = painterResource(layers[i]),
                        contentDescription = if (i == 0) "漏記長草×$stacks" else null,
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (cxPx - gHalf + i * 4f).toInt(),
                                    (cyPx - gHalf + halfPx * 0.25f + i * 5f).toInt()
                                )
                            }
                            .size(grassDp)
                            .alpha(0.82f + i * 0.06f),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // 物件層：HIDDEN 完全唔畫（唔灰桩）
        RanchHotspots.forEach { spot ->
            if (spot.category !in unlocked) return@forEach
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
                onTap = { onHotspotTap(spot.category) },
                onWhisperChange = { show ->
                    whisperCat = if (show) spot.category else {
                        if (whisperCat == spot.category) null else whisperCat
                    }
                }
            )
        }

        // 模式 B：長按 whisper（同時最多 1）；showHotspotLabels＝無障礙常駐細 tip
        if (showHotspotLabels && whisperCat == null) {
            RanchHotspots.forEach { spot ->
                if (spot.category !in unlocked) return@forEach
                val cxPx = spot.fx * RanchW * scaleX
                val cyPx = spot.fy * RanchH * scaleY
                val tipW = with(density) { 56.dp.toPx() }
                val tipH = with(density) { 26.dp.toPx() }
                WhisperLabel(
                    text = HotspotInteractionLogic.whisperShortName(spot.category),
                    modifier = Modifier.offset {
                        IntOffset(
                            (cxPx - tipW / 2f).toInt(),
                            (cyPx - halfPx - tipH - 2f).toInt().coerceAtLeast(2)
                        )
                    }
                )
            }
        } else {
            whisperCat?.let { cat ->
                if (cat !in unlocked) return@let
                val spot = RanchHotspots.find { it.category == cat } ?: return@let
                val cxPx = spot.fx * RanchW * scaleX
                val cyPx = spot.fy * RanchH * scaleY
                val tipW = with(density) { 56.dp.toPx() }
                val tipH = with(density) { 26.dp.toPx() }
                WhisperLabel(
                    text = HotspotInteractionLogic.whisperShortName(cat),
                    modifier = Modifier.offset {
                        IntOffset(
                            (cxPx - tipW / 2f).toInt(),
                            (cyPx - halfPx - tipH - 2f).toInt().coerceAtLeast(2)
                        )
                    }
                )
            }
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
    onTap: () -> Unit,
    onWhisperChange: (Boolean) -> Unit
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
    val greyMatrix = remember { ColorMatrix().apply { setToSaturation(0.2f) } }

    // 長按 ≥400ms → whisper；鬆開即消（同時最多 1 由父層）
    LaunchedEffect(pressed, enabled) {
        if (pressed && enabled) {
            delay(WhisperLongPressMs)
            onWhisperChange(true)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } else {
            onWhisperChange(false)
        }
    }

    val drawable = when {
        state == HotspotVisualState.PRESSED -> hotspotDrawable(spot.category, HotspotArtState.PRESSED)
        loggedToday && enabled -> hotspotDrawable(spot.category, HotspotArtState.ACTIVE)
        else -> hotspotDrawable(spot.category, HotspotArtState.IDLE)
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
                            else -> "（可點）"
                        }
            }
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
            painter = painterResource(drawable),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            colorFilter = if (state == HotspotVisualState.DISABLED) {
                ColorFilter.colorMatrix(greyMatrix)
            } else null,
            alpha = if (state == HotspotVisualState.DISABLED) 0.5f else 1f
        )
    }
}

enum class HotspotArtState { IDLE, PRESSED, ACTIVE }

fun hotspotDrawable(category: LedgerCategory, art: HotspotArtState = HotspotArtState.IDLE): Int {
    val stem = when (category) {
        LedgerCategory.FOOD -> "food_table"
        LedgerCategory.TRANSPORT -> "transport_bike"
        LedgerCategory.HOUSING -> "housing_porch"
        LedgerCategory.DAILY -> "daily_crates"
        LedgerCategory.ENTERTAINMENT -> "fun_garden_pond"
        LedgerCategory.HEALTH -> "health_herbs"
        LedgerCategory.INCOME -> "income_mail_basket"
        LedgerCategory.SAVINGS -> "savings_piggy"
        LedgerCategory.OTHER -> "other_notice"
    }
    return when (art) {
        HotspotArtState.IDLE -> when (stem) {
            "food_table" -> R.drawable.hotspot_food_table_idle
            "transport_bike" -> R.drawable.hotspot_transport_bike_idle
            "housing_porch" -> R.drawable.hotspot_housing_porch_idle
            "daily_crates" -> R.drawable.hotspot_daily_crates_idle
            "fun_garden_pond" -> R.drawable.hotspot_fun_garden_pond_idle
            "health_herbs" -> R.drawable.hotspot_health_herbs_idle
            "income_mail_basket" -> R.drawable.hotspot_income_mail_basket_idle
            "savings_piggy" -> R.drawable.hotspot_savings_piggy_idle
            else -> R.drawable.hotspot_other_notice_idle
        }
        HotspotArtState.PRESSED -> when (stem) {
            "food_table" -> R.drawable.hotspot_food_table_pressed
            "transport_bike" -> R.drawable.hotspot_transport_bike_pressed
            "housing_porch" -> R.drawable.hotspot_housing_porch_pressed
            "daily_crates" -> R.drawable.hotspot_daily_crates_pressed
            "fun_garden_pond" -> R.drawable.hotspot_fun_garden_pond_pressed
            "health_herbs" -> R.drawable.hotspot_health_herbs_pressed
            "income_mail_basket" -> R.drawable.hotspot_income_mail_basket_pressed
            "savings_piggy" -> R.drawable.hotspot_savings_piggy_pressed
            else -> R.drawable.hotspot_other_notice_pressed
        }
        HotspotArtState.ACTIVE -> when (stem) {
            "food_table" -> R.drawable.hotspot_food_table_active
            "transport_bike" -> R.drawable.hotspot_transport_bike_active
            "housing_porch" -> R.drawable.hotspot_housing_porch_active
            "daily_crates" -> R.drawable.hotspot_daily_crates_active
            "fun_garden_pond" -> R.drawable.hotspot_fun_garden_pond_active
            "health_herbs" -> R.drawable.hotspot_health_herbs_active
            "income_mail_basket" -> R.drawable.hotspot_income_mail_basket_active
            "savings_piggy" -> R.drawable.hotspot_savings_piggy_active
            else -> R.drawable.hotspot_other_notice_active
        }
    }
}

data class RanchHotspot(
    val category: LedgerCategory,
    val fx: Float,
    val fy: Float,
)

/** A2c／A2 鎖定熱區座標（docs/art/a2c/hotspot_map.json＝a2 同款 fx/fy） */
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

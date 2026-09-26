package com.farmledger.app.ui.screens.farm

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.farmledger.app.R
import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.CropTimers
import com.farmledger.app.domain.model.DayPhase
import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.model.GrowthSpendCosts
import com.farmledger.app.domain.model.InventoryFeedCosts
import com.farmledger.app.domain.model.InventoryItemKind
import com.farmledger.app.domain.model.PlotState
import com.farmledger.app.domain.model.RewardRules
import com.farmledger.app.domain.model.ShopCatalog
import com.farmledger.app.domain.usecase.DayLoopFocus
import com.farmledger.app.domain.usecase.DayLoopLogic
import com.farmledger.app.domain.usecase.FarmLogic
import com.farmledger.app.domain.usecase.FarmStageLogic
import com.farmledger.app.domain.usecase.InventoryLogic
import com.farmledger.app.domain.usecase.MissedCategoryLogic
import com.farmledger.app.ui.components.RanchTopBar
import com.farmledger.app.domain.usecase.HotspotInteractionLogic
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.screens.ledger.EntryEditScreen
import com.farmledger.app.ui.screens.ledger.formatMinor
import com.farmledger.app.ui.theme.FarmBg
import com.farmledger.app.ui.theme.FarmGrowth
import com.farmledger.app.ui.theme.FarmMudHighlight
import com.farmledger.app.ui.theme.FarmPlotEmpty
import com.farmledger.app.ui.theme.FarmPlotGrowing
import com.farmledger.app.ui.theme.FarmPlotReady
import com.farmledger.app.ui.theme.FarmSelected
import com.farmledger.app.ui.theme.FarmSoil
import com.farmledger.app.ui.theme.FarmStroke
import com.farmledger.app.ui.theme.FarmText
import java.time.LocalDate
import kotlinx.coroutines.delay

private val FarmPanelGrass = Color(0xFFC5D99A)
private val FarmPanelGrassDense = Color(0xFFA8C97A)

private enum class FarmOverlay { None, Ledger, Settle, Shop }

/** Ledger sheet inner page: list stays open; add/edit never leaves farm world. */
private sealed class LedgerSheetPage {
    data object List : LedgerSheetPage()
    data class Edit(val entryId: String?) : LedgerSheetPage()
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LegacyFarmDayLoopScreen(
    vm: AppViewModel,
    onOpenWeekly: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenEntry: (LedgerCategory?) -> Unit = {}
) {
    val plots by vm.plots.collectAsState()
    val progress by vm.progress.collectAsState()
    val pet by vm.pet.collectAsState()
    val decorations by vm.decorations.collectAsState()
    val gameDay by vm.gameDay.collectAsState()
    val inventory by vm.inventory.collectAsState()
    val entries by vm.entries.collectAsState()
    val allEntries by vm.allEntries.collectAsState()
    val accounts by vm.accounts.collectAsState()
    val message by vm.message.collectAsState()
    val petFeedbackUntilMs by vm.petFeedbackUntilMs.collectAsState()
    var selectedCrop by remember { mutableStateOf(CropKind.WHEAT) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var overlay by remember { mutableStateOf(FarmOverlay.None) }
    var ledgerPage by remember { mutableStateOf<LedgerSheetPage>(LedgerSheetPage.List) }

    val caps = remember(progress.totalSettleDays) {
        FarmStageLogic.capabilities(progress.totalSettleDays)
    }
    val nextUnlock = remember(progress.totalSettleDays) {
        FarmStageLogic.nextUnlock(progress.totalSettleDays)
    }
    val today = LocalDate.now().toString()
    val todaySettled = progress.lastSettleDate == today
    val dayGuide = remember(gameDay.phase, todaySettled) {
        DayLoopLogic.guide(gameDay.phase, todaySettled)
    }
    val panelGrass = if (caps.greenerDenser) FarmPanelGrassDense else FarmPanelGrass
    val seedTotal = remember(inventory) { InventoryLogic.totalSeeds(inventory) }
    val hkdBalanceMinor = remember(allEntries) {
        allEntries.filter { it.status == EntryStatus.ACTIVE }.fold(0L) { acc, e ->
            when (e.type) {
                EntryType.INCOME -> acc + e.amountMinor
                EntryType.EXPENSE -> acc - e.amountMinor
                else -> acc // TRANSFER / NO_TRADE 唔改總餘額
            }
        }
    }
    val cropTotal = remember(inventory) { InventoryLogic.totalCrops(inventory) }
    val selectedSeedQty = remember(inventory, selectedCrop) {
        InventoryLogic.seedQty(inventory, selectedCrop)
    }

    LaunchedEffect(Unit) {
        while (true) {
            vm.refreshFarm()
            nowMs = System.currentTimeMillis()
            delay(1_000)
        }
    }

    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars).padding(12.dp)) {
        // 頂欄：真實 Compose 木框 chip（季節／天氣時段／連續記帳／種子幣）；唔印死地圖
        RanchTopBar(
            seasonLine = "春・第 ${gameDay.gameDay} 日",
            weatherOrPhase = gameDay.phase.nameZh + "・" + caps.stage.nameZh,
            streakDays = progress.streakDays,
            seedCoins = progress.seedCoins,
            hkdMonthSummary = "HK$${formatMinor(hkdBalanceMinor)}"
        )

        if (progress.clockPaused) {
            Spacer(Modifier.height(6.dp))
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠️ 時間倒退，獎勵暫停。", Modifier.weight(1f), color = FarmText)
                    TextButton(onClick = { vm.clearClockPause() }) { Text("已校正") }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // M4 一日生活指引
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FarmBg),
            border = BorderStroke(1.5.dp, FarmStroke),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painterResource(dayLoopIconRes(dayGuide.focus)),
                    contentDescription = dayGuide.focus.nameZh,
                    modifier = Modifier.size(28.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.size(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        dayGuide.titleZh,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmText
                    )
                    Text(
                        dayGuide.hintZh,
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmSoil
                    )
                    Text(
                        DayLoopLogic.loopLabelsZh().joinToString(" → "),
                        style = MaterialTheme.typography.labelSmall,
                        color = FarmSoil
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    ledgerPage = LedgerSheetPage.List
                    overlay = FarmOverlay.Ledger
                },
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FarmGrowth, contentColor = FarmText)
            ) {
                Image(
                    painterResource(R.drawable.ic_ledger_book),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.size(4.dp))
                Text("今日帳")
            }
            val settlePrimary = DayLoopLogic.isSettlePreferred(gameDay.phase) && !todaySettled
            Button(
                onClick = { overlay = FarmOverlay.Settle },
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        todaySettled -> FarmBg
                        settlePrimary -> FarmSelected
                        else -> FarmSelected.copy(alpha = 0.85f)
                    },
                    contentColor = FarmText
                )
            ) {
                Image(
                    painterResource(R.drawable.ic_moon_settle),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    when {
                        todaySettled -> "今日已結清 ✓"
                        settlePrimary -> "夜結 +${RewardRules.DAILY_GROWTH_POINTS}"
                        else -> "日結 +${RewardRules.DAILY_GROWTH_POINTS}"
                    }
                )
            }
            val shopPrimary = DayLoopLogic.isShopPreferred(gameDay.phase)
            if (shopPrimary) {
                Button(
                    onClick = { overlay = FarmOverlay.Shop },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FarmSelected,
                        contentColor = FarmText
                    )
                ) {
                    Image(
                        painterResource(R.drawable.ic_shop_pole),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.size(4.dp))
                    Text("黃昏商店")
                }
            } else {
                OutlinedButton(
                    onClick = { overlay = FarmOverlay.Shop },
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Image(
                        painterResource(R.drawable.ic_shop_pole),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.size(4.dp))
                    Text("商店")
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { vm.waitNextPhase() },
                enabled = !progress.clockPaused && gameDay.phase != DayPhase.NIGHT,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    when (gameDay.phase) {
                        DayPhase.MORNING -> "等待・入晝"
                        DayPhase.NOON -> "等待・入昏"
                        DayPhase.EVENING -> "等待・入夜"
                        DayPhase.NIGHT -> "夜晚・請瞓覺"
                    }
                )
            }
            val sleepPrimary = DayLoopLogic.isSleepPreferred(gameDay.phase, todaySettled)
            Button(
                onClick = { vm.sleepToNextDay() },
                enabled = !progress.clockPaused && gameDay.phase == DayPhase.NIGHT,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (sleepPrimary) FarmSelected else FarmBg,
                    contentColor = FarmText
                )
            ) {
                Image(
                    painterResource(R.drawable.ic_day_sleep),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.size(4.dp))
                Text(if (sleepPrimary) "瞓覺・下一日" else "瞓覺過日")
            }
        }

        Spacer(Modifier.height(6.dp))
        Text("物品欄", color = FarmText, style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            inventory.filter { it.quantity > 0 }.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(inventoryIconRes(item.kind)),
                        contentDescription = item.kind.nameZh,
                        modifier = Modifier.size(26.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Text(
                        " ${item.kind.nameZh}×${item.quantity}",
                        style = MaterialTheme.typography.labelSmall,
                        color = FarmText
                    )
                }
            }
            if (inventory.none { it.quantity > 0 }) {
                Image(
                    painterResource(R.drawable.ic_buy_bag),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    contentScale = ContentScale.FillBounds
                )
                Text("（空・去商店買種子）", style = MaterialTheme.typography.labelSmall, color = FarmSoil)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text("選擇作物（消耗背包種子）：", color = FarmText, style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CropKind.entries.forEach { c ->
                val selected = selectedCrop == c
                val cost = CropTimers.seedCost(c)
                val have = InventoryLogic.seedQty(inventory, c)
                FilterChip(
                    selected = selected,
                    onClick = { selectedCrop = c },
                    leadingIcon = {
                        Image(
                            painterResource(cropSeedItemRes(c)),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            contentScale = ContentScale.FillBounds
                        )
                    },
                    label = {
                        Text("${FarmLogic.cropZh(c)} · 種$cost · 有$have · ${demoMinutesLabel(c)}")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = FarmBg,
                        labelColor = FarmText,
                        selectedContainerColor = FarmSelected,
                        selectedLabelColor = FarmText
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = FarmStroke,
                        selectedBorderColor = FarmSelected
                    )
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = panelGrass),
            border = BorderStroke(2.dp, FarmStroke),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                val missed = remember(allEntries, today) {
                    MissedCategoryLogic.missedCategories(allEntries, today)
                }
                val loggedToday = remember(allEntries, today) {
                    MissedCategoryLogic.usedCategoriesToday(allEntries, today)
                }
                val weedStacks = remember(allEntries, today) {
                    MissedCategoryLogic.weedStacksByCategory(allEntries, today)
                }
                val activeAccountCount = remember(accounts) {
                    accounts.count { !it.archived }
                }
                val latestSummaries = remember(allEntries) {
                    LedgerCategory.entries.associateWith { cat ->
                        HotspotInteractionLogic.latestEntrySummary(allEntries, cat)
                    }
                }
                RanchHotspotScene(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                    missedCategories = missed,
                    weedStacks = weedStacks,
                    loggedToday = loggedToday,
                    activeAccountCount = activeAccountCount,
                    latestSummaries = latestSummaries,
                    onHotspotTap = { cat ->
                        vm.prefillEntryCategory(cat)
                        onOpenEntry(cat)
                    }
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(FarmMudHighlight)
                )
                val visiblePlots = plots.filter { FarmStageLogic.isPlotUnlocked(progress.totalSettleDays, it.index) }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (caps.unlockedPlotCount <= 2) 2 else 3),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(FarmMudHighlight)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(visiblePlots, key = { it.index }) { plot ->
                        val cardColor = when (plot.state) {
                            PlotState.EMPTY -> FarmPlotEmpty
                            PlotState.GROWING -> FarmPlotGrowing
                            PlotState.READY -> FarmPlotReady
                        }
                        val iconRes = when (plot.state) {
                            PlotState.EMPTY -> R.drawable.tile_soil_empty
                            PlotState.GROWING -> plot.crop?.let { cropGrowRes(it) } ?: R.drawable.tile_soil_empty
                            PlotState.READY -> plot.crop?.let { cropReadyRes(it) } ?: R.drawable.tile_soil_empty
                        }
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            border = BorderStroke(1.5.dp, FarmStroke),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("田 #${plot.index + 1}", style = MaterialTheme.typography.labelMedium, color = FarmText)
                                Image(
                                    painterResource(iconRes),
                                    contentDescription = plot.state.name,
                                    modifier = Modifier.size(44.dp),
                                    contentScale = ContentScale.FillBounds
                                )
                                Text(
                                    when (plot.state) {
                                        PlotState.EMPTY -> "空地"
                                        PlotState.GROWING -> if (plot.watered) {
                                            "成長中：${plot.crop?.let { FarmLogic.cropZh(it) }}"
                                        } else {
                                            "待澆水：${plot.crop?.let { FarmLogic.cropZh(it) }}"
                                        }
                                        PlotState.READY -> "可收成：${plot.crop?.let { FarmLogic.cropZh(it) }}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = FarmText
                                )
                                when (plot.state) {
                                    PlotState.EMPTY -> Button(
                                        onClick = { vm.plant(plot.index, selectedCrop) },
                                        enabled = selectedSeedQty >= CropTimers.seedCost(selectedCrop),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = FarmSoil,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Image(
                                            painterResource(R.drawable.ic_tool_hoe),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            contentScale = ContentScale.FillBounds
                                        )
                                        Spacer(Modifier.size(4.dp))
                                        Text("種植 -${CropTimers.seedCost(selectedCrop)}種")
                                    }
                                    PlotState.READY -> Button(
                                        onClick = { vm.harvest(plot.index) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = FarmGrowth,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Image(
                                            painterResource(R.drawable.fx_sparkle_ready),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            contentScale = ContentScale.FillBounds
                                        )
                                        Spacer(Modifier.size(4.dp))
                                        Image(
                                            painterResource(
                                                plot.crop?.let { cropHarvestItemRes(it) }
                                                    ?: R.drawable.btn_harvest
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            contentScale = ContentScale.FillBounds
                                        )
                                        Spacer(Modifier.size(4.dp))
                                        Text("收成入庫")
                                    }
                                    PlotState.GROWING -> {
                                        if (!plot.watered) {
                                            Button(
                                                onClick = { vm.water(plot.index) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = FarmSelected,
                                                    contentColor = FarmText
                                                )
                                            ) {
                                                Image(
                                                    painterResource(R.drawable.ic_tool_water),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    contentScale = ContentScale.FillBounds
                                                )
                                                Spacer(Modifier.size(4.dp))
                                                Text("澆水")
                                            }
                                        } else {
                                            val remainMs = ((plot.readyAtEpochMs ?: nowMs) - nowMs).coerceAtLeast(0L)
                                            Text(
                                                formatRemain(remainMs),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = FarmSoil
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val feedHave = InventoryLogic.feedQty(inventory)
            OutlinedButton(
                onClick = { vm.feedPet() },
                enabled = caps.petFeedSlots > 0 && feedHave >= InventoryFeedCosts.FEED_PER_MEAL,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painterResource(R.drawable.item_feed),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    when {
                        caps.petFeedSlots <= 0 -> "餵食（萌芽解鎖）"
                        else -> "餵食 -${InventoryFeedCosts.FEED_PER_MEAL}（有$feedHave）"
                    }
                )
            }
            val placedDecor = decorations.count { it.placed }
            val decorRemain = (caps.decorSlots - placedDecor).coerceAtLeast(0)
            OutlinedButton(
                onClick = { vm.buildDecorInScene() },
                enabled = caps.decorSlots > 0 &&
                    decorRemain > 0 &&
                    progress.growthPoints >= GrowthSpendCosts.BUILD_DECOR,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painterResource(
                        when {
                            caps.decorSlots <= 0 -> R.drawable.build_plot_empty
                            decorRemain <= 0 -> R.drawable.decor_sign
                            else -> R.drawable.build_blueprint
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    when {
                        caps.decorSlots <= 0 -> "佈置（安家解鎖）"
                        decorRemain <= 0 -> "佈置已滿"
                        else -> "佈置 -${GrowthSpendCosts.BUILD_DECOR}（剩$decorRemain）"
                    }
                )
            }
        }
        if (caps.animalSlots >= 2) {
            Text("第二動物欄已開放（累計 ${progress.totalSettleDays} 日）", style = MaterialTheme.typography.bodySmall, color = FarmGrowth)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onOpenWeekly) { Text("週回顧") }
            Text(
                "${pet.name} · 親密 ${pet.affection}",
                style = MaterialTheme.typography.bodySmall,
                color = FarmText,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            TextButton(onClick = onOpenSettings) { Text("設定") }
        }

        message?.let {
            Text(it, color = FarmText, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { vm.consumeMessage() }) { Text("清除") }
        }
    }

    if (overlay == FarmOverlay.Ledger) {
        ModalBottomSheet(
            onDismissRequest = {
                overlay = FarmOverlay.None
                ledgerPage = LedgerSheetPage.List
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            when (val page = ledgerPage) {
                is LedgerSheetPage.List -> LedgerOverlayContent(
                    vm = vm,
                    onAdd = { ledgerPage = LedgerSheetPage.Edit(null) },
                    onEdit = { id -> ledgerPage = LedgerSheetPage.Edit(id) },
                    onGoSettle = {
                        ledgerPage = LedgerSheetPage.List
                        overlay = FarmOverlay.Settle
                    }
                )
                is LedgerSheetPage.Edit -> EntryEditScreen(
                    vm = vm,
                    entryId = page.entryId,
                    onDone = { ledgerPage = LedgerSheetPage.List }
                )
            }
        }
    }
    if (overlay == FarmOverlay.Settle) {
        ModalBottomSheet(
            onDismissRequest = { overlay = FarmOverlay.None },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            SettleOverlayContent(
                vm = vm,
                hasActivity = entries.any { it.status == EntryStatus.ACTIVE },
                phase = gameDay.phase,
                onClose = { overlay = FarmOverlay.None }
            )
        }
    }
    if (overlay == FarmOverlay.Shop) {
        ModalBottomSheet(
            onDismissRequest = { overlay = FarmOverlay.None },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ShopOverlayContent(
                vm = vm,
                phase = gameDay.phase,
                onClose = { overlay = FarmOverlay.None }
            )
        }
    }
}

@Composable
private fun ShopOverlayContent(
    vm: AppViewModel,
    phase: DayPhase,
    onClose: () -> Unit
) {
    val inventory by vm.inventory.collectAsState()
    val progress by vm.progress.collectAsState()
    val message by vm.message.collectAsState()
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(R.drawable.shop_stall),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.FillBounds
            )
            Spacer(Modifier.size(6.dp))
            Image(
                painterResource(R.drawable.npc_vendor_idle),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                contentScale = ContentScale.FillBounds
            )
            Spacer(Modifier.size(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (phase == DayPhase.EVENING) "黃昏商店・攤位" else "農場商店・攤位",
                    style = MaterialTheme.typography.headlineSmall,
                    color = FarmText
                )
                Text(
                    if (phase == DayPhase.EVENING) "一日節奏：牧場買賣（種子幣；唔入真帳）"
                    else "隨時可買；黃昏最合適。",
                    style = MaterialTheme.typography.bodySmall,
                    color = FarmSoil
                )
                Text(
                    "種子幣 ${progress.seedCoins}（≠港幣）",
                    style = MaterialTheme.typography.labelMedium,
                    color = FarmText,
                    fontWeight = FontWeight.Bold
                )
            }
            Image(
                painterResource(R.drawable.shop_sign),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.FillBounds
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "牧場內經濟：種子幣買賣，唔入真港幣帳；唔發成長點。必須經背包。",
            style = MaterialTheme.typography.bodySmall,
            color = FarmSoil
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(R.drawable.ic_coin_minus),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                contentScale = ContentScale.FillBounds
            )
            Spacer(Modifier.size(6.dp))
            Text("買種子", fontWeight = FontWeight.Bold, color = FarmText)
            Spacer(Modifier.size(8.dp))
            Image(
                painterResource(R.drawable.ui_price_tag),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                contentScale = ContentScale.FillBounds
            )
        }
        CropKind.entries.forEach { crop ->
            val price = ShopCatalog.seedBuyPriceCoins(crop)
            val have = InventoryLogic.seedQty(inventory, crop)
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painterResource(R.drawable.ui_shop_slot),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            contentScale = ContentScale.FillBounds
                        )
                        Image(
                            painterResource(cropSeedItemRes(crop)),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    Spacer(Modifier.size(6.dp))
                    Text(
                        "${FarmLogic.cropZh(crop)}種子 · $price 種子幣／袋 · 有 $have",
                        color = FarmText
                    )
                }
                Button(onClick = { vm.buySeeds(crop, 1) }) {
                    Image(
                        painterResource(R.drawable.ic_buy_bag),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.size(4.dp))
                    Text("買 1")
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(R.drawable.item_feed),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                contentScale = ContentScale.FillBounds
            )
            Spacer(Modifier.size(6.dp))
            Text("買飼料", fontWeight = FontWeight.Bold, color = FarmText)
        }
        run {
            val price = ShopCatalog.FEED_BUY_PRICE_COINS
            val have = InventoryLogic.feedQty(inventory)
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.item_feed),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        "飼料 · $price 種子幣／袋 · 有 $have",
                        color = FarmText
                    )
                }
                Button(onClick = { vm.buyFeed(1) }) {
                    Image(
                        painterResource(R.drawable.ic_buy_bag),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.size(4.dp))
                    Text("買 1")
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(R.drawable.ic_coin_plus),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                contentScale = ContentScale.FillBounds
            )
            Spacer(Modifier.size(6.dp))
            Text("賣收成", fontWeight = FontWeight.Bold, color = FarmText)
        }
        CropKind.entries.forEach { crop ->
            val price = ShopCatalog.cropSellPriceCoins(crop)
            val have = InventoryLogic.cropQty(inventory, crop)
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painterResource(R.drawable.ui_shop_slot),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            contentScale = ContentScale.FillBounds
                        )
                        Image(
                            painterResource(cropHarvestItemRes(crop)),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    Spacer(Modifier.size(6.dp))
                    Text(
                        "${FarmLogic.cropZh(crop)} · $price 種子幣／個 · 有 $have",
                        color = FarmText
                    )
                }
                Button(
                    onClick = { vm.sellHarvest(crop, 1) },
                    enabled = have > 0
                ) {
                    Image(
                        painterResource(R.drawable.ic_sell_basket),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.size(4.dp))
                    Text("賣 1")
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("背包一覽", fontWeight = FontWeight.SemiBold, color = FarmText)
        inventory.filter { it.quantity > 0 }.forEach { item ->
            Row(
                Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painterResource(inventoryIconRes(item.kind)),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.size(6.dp))
                Text("${item.kind.nameZh} ×${item.quantity}", color = FarmText)
            }
        }
        if (inventory.none { it.quantity > 0 }) {
            Text("（空）", color = FarmSoil)
        }
        message?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = FarmText)
            TextButton(onClick = { vm.consumeMessage() }) { Text("清除提示") }
        }
        TextButton(onClick = onClose) { Text("關閉") }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LedgerOverlayContent(
    vm: AppViewModel,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onGoSettle: () -> Unit
) {
    val entries by vm.entries.collectAsState()
    val allEntries by vm.allEntries.collectAsState()
    val date by vm.selectedDate.collectAsState()
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(R.drawable.ic_ledger_book),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.FillBounds
            )
            Spacer(Modifier.size(8.dp))
            Text("今日帳簿", style = MaterialTheme.typography.headlineSmall, color = FarmText)
        }
        Text(date, style = MaterialTheme.typography.bodyMedium, color = FarmText)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAdd) { Text("新增一筆") }
            TextButton(onClick = onGoSettle) { Text("去結算 →") }
        }
        Spacer(Modifier.height(8.dp))
        entries.forEach { e ->
            val voided = e.status == EntryStatus.VOIDED
            Card(
                onClick = { if (!voided) onEdit(e.id) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                enabled = !voided
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "${e.type.name} · ${(e.amountMinor / 100.0)}",
                        fontWeight = FontWeight.SemiBold,
                        color = if (voided) FarmSoil else FarmText
                    )
                    if (e.note.isNotBlank()) Text(e.note, style = MaterialTheme.typography.bodySmall)
                    if (voided) Text("已作廢", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        if (entries.isEmpty()) {
            Text("尚無港幣帳。請去「入帳」記真帳，或標記無交易日後結算。商店買賣唔入真帳。", color = FarmSoil)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettleOverlayContent(
    vm: AppViewModel,
    hasActivity: Boolean,
    phase: DayPhase,
    onClose: () -> Unit
) {
    val progress by vm.progress.collectAsState()
    val message by vm.message.collectAsState()
    val ceremony by vm.settleCeremonyAwarded.collectAsState()
    val today = LocalDate.now().toString()
    val already = progress.lastSettleDate == today
    val moonAlpha by animateFloatAsState(
        targetValue = if (ceremony || already) 1f else if (phase == DayPhase.NIGHT) 0.85f else 0.55f,
        animationSpec = tween(900),
        label = "moon"
    )
    var moonFrame by remember { mutableIntStateOf(0) }
    LaunchedEffect(ceremony, phase) {
        if (ceremony || phase == DayPhase.NIGHT) {
            for (i in 0..2) {
                moonFrame = i
                delay(280)
            }
            moonFrame = 2
        } else {
            moonFrame = 0
        }
    }
    val moonRes = when (moonFrame) {
        1 -> R.drawable.moon_rise_f1
        2 -> R.drawable.moon_rise_f2
        else -> R.drawable.moon_rise_f0
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("夜結儀式", style = MaterialTheme.typography.headlineSmall, color = FarmText)
        Spacer(Modifier.height(8.dp))
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (phase == DayPhase.NIGHT) Color(0xFF2A3A68) else MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painterResource(moonRes),
                    contentDescription = "日結月亮升起",
                    modifier = Modifier.size(72.dp).alpha(moonAlpha),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "固定 +${RewardRules.DAILY_GROWTH_POINTS} 成長點",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (phase == DayPhase.NIGHT) Color(0xFFF4D078) else FarmGrowth
                )
                Text(
                    "與筆數／金額無關 · 每日一次 · 編輯唔會再發獎",
                    color = if (phase == DayPhase.NIGHT) Color(0xFFEDE6D9) else FarmText,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "買賣／睡覺唔發獎。時鐘倒退暫停獎勵。",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (phase == DayPhase.NIGHT) Color(0xFFC8BCA8) else FarmSoil
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("今日帳本活動：${if (hasActivity) "是" else "否"}", fontWeight = FontWeight.SemiBold, color = FarmText)
        Text("今日已結算：${if (already) "是" else "否"}", color = FarmText)
        Text("累計結算日：${progress.totalSettleDays}", color = FarmText)
        if (phase != DayPhase.NIGHT) {
            Text("提示：夜晚做結算儀式最有氣氛（規則全日相同）。", style = MaterialTheme.typography.bodySmall, color = FarmSoil)
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { vm.settleToday() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !progress.clockPaused
        ) {
            Image(
                painterResource(R.drawable.ic_moon_settle),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.FillBounds
            )
            Spacer(Modifier.size(6.dp))
            Text(if (already) "今日已結清（唔會再發獎）" else "升起月亮・執行結算")
        }
        AnimatedVisibility(
            visible = ceremony,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut()
        ) {
            Column(
                Modifier.padding(top = 12.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painterResource(R.drawable.settle_banner),
                    contentDescription = "今日結清",
                    modifier = Modifier.size(width = 160.dp, height = 48.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.height(8.dp))
                Image(
                    painterResource(R.drawable.ic_settle_stamp),
                    contentDescription = "結清印章",
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.ic_growth_point),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        "＋${RewardRules.DAILY_GROWTH_POINTS} 成長點入袋",
                        fontWeight = FontWeight.Bold,
                        color = FarmGrowth
                    )
                }
                TextButton(onClick = { vm.consumeSettleCeremony() }) { Text("收下") }
            }
        }
        message?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = FarmText)
            TextButton(onClick = { vm.consumeMessage() }) { Text("清除提示") }
        }
        TextButton(onClick = onClose) { Text("關閉") }
        Spacer(Modifier.height(24.dp))
    }
}

private fun dayLoopIconRes(focus: DayLoopFocus) = when (focus) {
    DayLoopFocus.WAKE -> R.drawable.ic_day_wake
    DayLoopFocus.WORK -> R.drawable.ic_day_work
    DayLoopFocus.SHOP -> R.drawable.ic_day_shop
    DayLoopFocus.SETTLE -> R.drawable.ic_moon_settle
    DayLoopFocus.SLEEP -> R.drawable.ic_day_sleep
}

private fun cropSeedItemRes(kind: CropKind) = when (kind) {
    CropKind.WHEAT -> R.drawable.item_seed_wheat
    CropKind.CARROT -> R.drawable.item_seed_carrot
    CropKind.TOMATO -> R.drawable.item_seed_tomato
}

private fun cropHarvestItemRes(kind: CropKind) = when (kind) {
    CropKind.WHEAT -> R.drawable.item_harvest_wheat
    CropKind.CARROT -> R.drawable.item_harvest_carrot
    CropKind.TOMATO -> R.drawable.item_harvest_tomato
}

private fun inventoryIconRes(kind: InventoryItemKind) = when (kind) {
    InventoryItemKind.SEED_WHEAT -> R.drawable.item_seed_wheat
    InventoryItemKind.SEED_CARROT -> R.drawable.item_seed_carrot
    InventoryItemKind.SEED_TOMATO -> R.drawable.item_seed_tomato
    InventoryItemKind.CROP_WHEAT -> R.drawable.item_harvest_wheat
    InventoryItemKind.CROP_CARROT -> R.drawable.item_harvest_carrot
    InventoryItemKind.CROP_TOMATO -> R.drawable.item_harvest_tomato
    InventoryItemKind.FEED -> R.drawable.item_feed
    InventoryItemKind.MATERIAL -> R.drawable.item_material
}

private fun cropGrowRes(kind: CropKind) = when (kind) {
    CropKind.WHEAT -> R.drawable.crop_wheat_grow
    CropKind.CARROT -> R.drawable.crop_carrot_grow
    CropKind.TOMATO -> R.drawable.crop_tomato_grow
}

private fun cropReadyRes(kind: CropKind) = when (kind) {
    CropKind.WHEAT -> R.drawable.crop_wheat_ready
    CropKind.CARROT -> R.drawable.crop_carrot_ready
    CropKind.TOMATO -> R.drawable.crop_tomato_ready
}

private fun demoMinutesLabel(kind: CropKind): String {
    val min = CropTimers.growMs(kind) / 60_000L
    return "${min}分"
}

private fun formatRemain(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}

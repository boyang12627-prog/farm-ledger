package com.farmledger.app.ui.screens.farm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.GrowthSpendCosts
import com.farmledger.app.domain.model.PlotState
import com.farmledger.app.domain.model.RewardRules
import com.farmledger.app.domain.usecase.FarmLogic
import com.farmledger.app.domain.usecase.FarmStageLogic
import com.farmledger.app.ui.AppViewModel
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

private enum class FarmOverlay { None, Ledger, Settle }

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FarmScreen(
    vm: AppViewModel,
    onOpenWeekly: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onEditEntry: (String) -> Unit = {},
    onAddEntry: () -> Unit = {}
) {
    val plots by vm.plots.collectAsState()
    val progress by vm.progress.collectAsState()
    val pet by vm.pet.collectAsState()
    val decorations by vm.decorations.collectAsState()
    val entries by vm.entries.collectAsState()
    val message by vm.message.collectAsState()
    var selectedCrop by remember { mutableStateOf(CropKind.WHEAT) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var overlay by remember { mutableStateOf(FarmOverlay.None) }

    val caps = remember(progress.totalSettleDays) {
        FarmStageLogic.capabilities(progress.totalSettleDays)
    }
    val nextUnlock = remember(progress.totalSettleDays) {
        FarmStageLogic.nextUnlock(progress.totalSettleDays)
    }
    val today = LocalDate.now().toString()
    val todaySettled = progress.lastSettleDate == today
    val panelGrass = if (caps.greenerDenser) FarmPanelGrassDense else FarmPanelGrass

    LaunchedEffect(Unit) {
        while (true) {
            vm.refreshFarm()
            nowMs = System.currentTimeMillis()
            delay(1_000)
        }
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        // HUD
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "農場・${caps.stage.nameZh}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = FarmText,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "累計結算 ${progress.totalSettleDays} 日" +
                        (nextUnlock?.let { " → ${it.second}（${it.first}）" } ?: "（滿階）"),
                    style = MaterialTheme.typography.bodySmall,
                    color = FarmSoil
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(R.drawable.ic_growth_point),
                    null,
                    Modifier.size(22.dp),
                    contentScale = ContentScale.FillBounds
                )
                Text(" ${progress.growthPoints}", color = FarmText, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(10.dp))
                Image(
                    painterResource(R.drawable.ic_seed),
                    null,
                    Modifier.size(22.dp),
                    contentScale = ContentScale.FillBounds
                )
                Text(" ${progress.seeds}", color = FarmText)
            }
        }

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

        // Overlay CTAs — 記帳／結算為場景上的 sheet，非獨立首頁分頁
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { overlay = FarmOverlay.Ledger },
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FarmGrowth, contentColor = FarmText)
            ) { Text("記帳") }
            Button(
                onClick = { overlay = FarmOverlay.Settle },
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (todaySettled) FarmBg else FarmSelected,
                    contentColor = FarmText
                )
            ) {
                Text(if (todaySettled) "今日已結算 ✓" else "每日結算 +${RewardRules.DAILY_GROWTH_POINTS}")
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            "成長點只由每日結算獲得；在場景內花點種植／餵食／佈置。",
            style = MaterialTheme.typography.bodySmall,
            color = FarmSoil
        )

        Spacer(Modifier.height(6.dp))
        Text("選擇作物（花費成長點）：", color = FarmText, style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CropKind.entries.forEach { c ->
                val selected = selectedCrop == c
                val cost = GrowthSpendCosts.plant(c)
                FilterChip(
                    selected = selected,
                    onClick = { selectedCrop = c },
                    label = {
                        Text("${FarmLogic.cropZh(c)} · ${cost}點 · ${demoMinutesLabel(c)}")
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
                FarmSceneLayer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                    interactive = caps.animalSlots > 0,
                    capabilities = caps,
                    placedDecorIds = decorations.filter { it.placed }.map { it.id }.toSet(),
                    onPetTap = {
                        if (caps.petFeedSlots > 0) vm.feedPet()
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
                                        PlotState.GROWING -> "成長中：${plot.crop?.let { FarmLogic.cropZh(it) }}"
                                        PlotState.READY -> "可收成：${plot.crop?.let { FarmLogic.cropZh(it) }}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = FarmText
                                )
                                when (plot.state) {
                                    PlotState.EMPTY -> Button(
                                        onClick = { vm.plant(plot.index, selectedCrop) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = FarmSoil,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("種植 -${GrowthSpendCosts.plant(selectedCrop)}")
                                    }
                                    PlotState.READY -> Button(
                                        onClick = { vm.harvest(plot.index) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = FarmGrowth,
                                            contentColor = Color.White
                                        )
                                    ) { Text("收成") }
                                    PlotState.GROWING -> {
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

        // In-scene spend actions
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { vm.feedPet() },
                enabled = caps.petFeedSlots > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (caps.petFeedSlots > 0) "餵食 -${GrowthSpendCosts.FEED_PET}"
                    else "餵食（萌芽解鎖）"
                )
            }
            OutlinedButton(
                onClick = { vm.buildDecorInScene() },
                enabled = caps.decorSlots > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (caps.decorSlots > 0) "佈置 -${GrowthSpendCosts.BUILD_DECOR}"
                    else "佈置（安家解鎖）"
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
            onDismissRequest = { overlay = FarmOverlay.None },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            LedgerOverlayContent(
                vm = vm,
                onAdd = {
                    overlay = FarmOverlay.None
                    onAddEntry()
                },
                onEdit = { id ->
                    overlay = FarmOverlay.None
                    onEditEntry(id)
                },
                onGoSettle = { overlay = FarmOverlay.Settle }
            )
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
                onClose = { overlay = FarmOverlay.None }
            )
        }
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
    val date by vm.selectedDate.collectAsState()
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("今日帳簿", style = MaterialTheme.typography.headlineSmall, color = FarmText)
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
            Text("尚無紀錄。可記一筆或標記無交易日後結算。", color = FarmSoil)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettleOverlayContent(
    vm: AppViewModel,
    hasActivity: Boolean,
    onClose: () -> Unit
) {
    val progress by vm.progress.collectAsState()
    val message by vm.message.collectAsState()
    val today = LocalDate.now().toString()
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("每日結算", style = MaterialTheme.typography.headlineSmall, color = FarmText)
        Spacer(Modifier.height(8.dp))
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "固定 +${RewardRules.DAILY_GROWTH_POINTS} 成長點",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = FarmGrowth
                )
                Text("與筆數／金額無關 · 每日一次", color = FarmText)
                Text("無交易日亦計入。編輯唔會再發獎。", style = MaterialTheme.typography.bodySmall, color = FarmText)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("今日帳本活動：${if (hasActivity) "是" else "否"}", fontWeight = FontWeight.SemiBold, color = FarmText)
        Text("今日已結算：${if (progress.lastSettleDate == today) "是" else "否"}", color = FarmText)
        Text("累計結算日：${progress.totalSettleDays}", color = FarmText)
        Spacer(Modifier.height(12.dp))
        Button(onClick = { vm.settleToday() }, modifier = Modifier.fillMaxWidth()) {
            Text("執行結算")
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

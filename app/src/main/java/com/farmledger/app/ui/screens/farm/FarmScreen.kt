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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.farmledger.app.domain.model.PlotState
import com.farmledger.app.domain.usecase.FarmLogic
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
import kotlinx.coroutines.delay

/** Soft sage wash under the scene; mud highlight binds scene to plot grid. */
private val FarmPanelGrass = Color(0xFFC5D99A) // between LGREEN / SAGE

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FarmScreen(vm: AppViewModel) {
    val plots by vm.plots.collectAsState()
    val progress by vm.progress.collectAsState()
    val message by vm.message.collectAsState()
    var selectedCrop by remember { mutableStateOf(CropKind.WHEAT) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            vm.refreshFarm()
            nowMs = System.currentTimeMillis()
            delay(1_000)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("農田", style = MaterialTheme.typography.headlineMedium, color = FarmText)
            Spacer(Modifier.size(8.dp))
            Image(painterResource(R.drawable.ic_seed), null, Modifier.size(24.dp), contentScale = ContentScale.FillBounds)
            Text(" ${progress.seeds}", color = FarmText)
            Spacer(Modifier.size(12.dp))
            Image(painterResource(R.drawable.ic_growth_point), null, Modifier.size(24.dp), contentScale = ContentScale.FillBounds)
            Text(" ${progress.growthPoints}", color = FarmText)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "示範節奏：作物成長約 1～3 分鐘（小麥 1 分／紅蘿蔔 2 分／番茄 3 分），非真實農作時間。",
            style = MaterialTheme.typography.bodySmall,
            color = FarmSoil
        )
        Spacer(Modifier.height(8.dp))
        Text("選擇作物：", color = FarmText)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CropKind.entries.forEach { c ->
                val selected = selectedCrop == c
                FilterChip(
                    selected = selected,
                    onClick = { selectedCrop = c },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painterResource(cropReadyRes(c)),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                contentScale = ContentScale.FillBounds
                            )
                            Spacer(Modifier.size(4.dp))
                            Text("${FarmLogic.cropZh(c)}（示範節奏 ${demoMinutesLabel(c)}）")
                        }
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
        Spacer(Modifier.height(8.dp))
        // Unified farm panel: scene + plot grid share background, rim, and tight spacing.
        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FarmPanelGrass),
            border = BorderStroke(2.dp, FarmStroke),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                FarmSceneLayer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                    interactive = true
                )
                // Soft dirt blend strip — visually extends scene ground into the plot area.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(FarmMudHighlight)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(FarmMudHighlight)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(plots, key = { it.index }) { plot ->
                        // Cream boards + solid mud stroke (#6B4A2E) — same as scene outline
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
                            Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("田 #${plot.index + 1}", style = MaterialTheme.typography.labelMedium, color = FarmText)
                                Image(
                                    painterResource(iconRes),
                                    contentDescription = plot.state.name,
                                    modifier = Modifier.size(48.dp),
                                    contentScale = ContentScale.FillBounds
                                )
                                Spacer(Modifier.height(4.dp))
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
                                        colors = ButtonDefaults.buttonColors(containerColor = FarmSoil, contentColor = Color.White)
                                    ) {
                                        Image(painterResource(R.drawable.btn_plant), null, Modifier.size(18.dp), contentScale = ContentScale.FillBounds)
                                        Spacer(Modifier.size(4.dp))
                                        Text("種植")
                                    }
                                    PlotState.READY -> Button(
                                        onClick = { vm.harvest(plot.index) },
                                        colors = ButtonDefaults.buttonColors(containerColor = FarmGrowth, contentColor = Color.White)
                                    ) {
                                        Image(painterResource(R.drawable.btn_harvest), null, Modifier.size(18.dp), contentScale = ContentScale.FillBounds)
                                        Spacer(Modifier.size(4.dp))
                                        Text("收成")
                                    }
                                    PlotState.GROWING -> {
                                        val remainMs = ((plot.readyAtEpochMs ?: nowMs) - nowMs).coerceAtLeast(0L)
                                        Text(
                                            "示範節奏剩餘 ${formatRemain(remainMs)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = FarmSoil,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        message?.let {
            Text(it, color = FarmText)
            TextButton(onClick = { vm.consumeMessage() }) { Text("清除") }
        }
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
    return "${min} 分"
}

private fun formatRemain(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}

package com.farmledger.app.ui.screens.farm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.farmledger.app.domain.model.CropKind
import com.farmledger.app.domain.model.PlotState
import com.farmledger.app.domain.usecase.FarmLogic
import com.farmledger.app.ui.AppViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FarmScreen(vm: AppViewModel) {
    val plots by vm.plots.collectAsState()
    val progress by vm.progress.collectAsState()
    val message by vm.message.collectAsState()
    var selectedCrop by remember { mutableStateOf(CropKind.WHEAT) }

    LaunchedEffect(Unit) {
        while (true) {
            vm.refreshFarm()
            delay(5_000)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("農田", style = MaterialTheme.typography.headlineMedium)
        Text("種子：${progress.seeds}　成長點：${progress.growthPoints}")
        Spacer(Modifier.height(8.dp))
        Text("選擇作物：")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CropKind.entries.forEach { c ->
                FilterChip(
                    selected = selectedCrop == c,
                    onClick = { selectedCrop = c },
                    label = { Text(FarmLogic.cropZh(c)) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(plots, key = { it.index }) { plot ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(8.dp)) {
                        Text("田 #${plot.index + 1}")
                        Text(
                            when (plot.state) {
                                PlotState.EMPTY -> "空地"
                                PlotState.GROWING -> "成長中：${plot.crop?.let { FarmLogic.cropZh(it) }}"
                                PlotState.READY -> "可收成：${plot.crop?.let { FarmLogic.cropZh(it) }}"
                            }
                        )
                        when (plot.state) {
                            PlotState.EMPTY -> Button(onClick = { vm.plant(plot.index, selectedCrop) }) {
                                Text("種植")
                            }
                            PlotState.READY -> Button(onClick = { vm.harvest(plot.index) }) {
                                Text("收成")
                            }
                            PlotState.GROWING -> Text("等待…", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        message?.let {
            Text(it)
            TextButton(onClick = { vm.consumeMessage() }) { Text("清除") }
        }
    }
}

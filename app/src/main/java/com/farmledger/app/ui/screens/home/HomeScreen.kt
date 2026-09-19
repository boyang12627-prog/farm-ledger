package com.farmledger.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.farmledger.app.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: AppViewModel,
    onLedger: () -> Unit,
    onSettle: () -> Unit,
    onFarm: () -> Unit,
    onPet: () -> Unit,
    onDecor: () -> Unit,
    onWeekly: () -> Unit,
    onSettings: () -> Unit
) {
    val progress by vm.progress.collectAsState()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("主頁", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        if (progress.clockPaused) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("⚠️ 偵測到系統時間倒退，每日／作物獎勵已暫停。帳本保留。")
                    TextButton(onClick = { vm.clearClockPause() }) { Text("時間已校正，恢復獎勵") }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("成長點：${progress.growthPoints}")
                Text("種子：${progress.seeds}")
                Text("連續結算：${progress.streakDays} 日")
                Text("上次結算：${progress.lastSettleDate ?: "尚未"}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    progress.unlockedStreakRewards.sorted().forEach {
                        AssistChip(onClick = {}, label = { Text("連續$it") })
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onLedger, modifier = Modifier.fillMaxWidth()) { Text("今日帳簿") }
        OutlinedButton(onClick = onSettle, modifier = Modifier.fillMaxWidth()) { Text("每日結算") }
        OutlinedButton(onClick = onFarm, modifier = Modifier.fillMaxWidth()) { Text("農田（6 格）") }
        OutlinedButton(onClick = onPet, modifier = Modifier.fillMaxWidth()) { Text("寵物") }
        OutlinedButton(onClick = onDecor, modifier = Modifier.fillMaxWidth()) { Text("家居裝飾") }
        OutlinedButton(onClick = onWeekly, modifier = Modifier.fillMaxWidth()) { Text("週回顧") }
        OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text("設定／匯出") }
    }
}

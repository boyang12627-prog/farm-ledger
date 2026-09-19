package com.farmledger.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.farmledger.app.R
import com.farmledger.app.domain.model.PlotState
import com.farmledger.app.domain.model.RewardRules
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.theme.FarmGrowth
import com.farmledger.app.ui.theme.FarmSelected
import com.farmledger.app.ui.theme.FarmText

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
    val plots by vm.plots.collectAsState()
    val pet by vm.pet.collectAsState()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("主頁", style = MaterialTheme.typography.headlineMedium, color = FarmText)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.ic_growth_point),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.size(8.dp))
                    Text("成長點：${progress.growthPoints}", color = FarmText)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.ic_seed),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.size(8.dp))
                    Text("種子：${progress.seeds}", color = FarmText)
                }
                Text("連續結算：${progress.streakDays} 日", color = FarmText)
                Text("上次結算：${progress.lastSettleDate ?: "尚未"}", color = FarmText)
            }
        }
        Spacer(Modifier.height(12.dp))
        StreakProgressCard(
            streakDays = progress.streakDays,
            unlocked = progress.unlockedStreakRewards
        )
        Spacer(Modifier.height(12.dp))
        FarmPetPreviewStrip(
            growingCount = plots.count { it.state == PlotState.GROWING },
            readyCount = plots.count { it.state == PlotState.READY },
            emptyCount = plots.count { it.state == PlotState.EMPTY },
            petName = pet.name,
            petAffection = pet.affection,
            onFarm = onFarm,
            onPet = onPet
        )
        Spacer(Modifier.height(16.dp))
        Text("今日要事", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FarmText)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onLedger,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FarmGrowth, contentColor = FarmText)
        ) {
            Image(
                painterResource(R.drawable.ic_cta_ledger),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.FillBounds
            )
            Spacer(Modifier.size(10.dp))
            Text("今日帳簿", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onSettle,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FarmSelected, contentColor = FarmText)
        ) {
            Image(
                painterResource(R.drawable.ic_cta_settle),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.FillBounds
            )
            Spacer(Modifier.size(10.dp))
            Text("每日結算", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(16.dp))
        Text("其他", style = MaterialTheme.typography.titleSmall, color = FarmText.copy(alpha = 0.7f))
        Spacer(Modifier.height(4.dp))
        OutlinedButton(onClick = onFarm, modifier = Modifier.fillMaxWidth()) { Text("農田（6 格）") }
        OutlinedButton(onClick = onWeekly, modifier = Modifier.fillMaxWidth()) { Text("週回顧") }
        OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text("設定／匯出") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = onPet) { Text("寵物") }
            TextButton(onClick = onDecor) { Text("家居裝飾") }
        }
    }
}

@Composable
private fun StreakProgressCard(streakDays: Int, unlocked: Set<Int>) {
    val milestones = RewardRules.STREAK_MILESTONES
    val next = milestones.firstOrNull { it > streakDays }
    val prev = milestones.lastOrNull { it <= streakDays } ?: 0
    val target = next ?: milestones.last()
    val base = if (next == null) milestones.getOrElse(milestones.lastIndex - 1) { 0 } else prev
    val fraction = when {
        next == null && streakDays >= milestones.last() -> 1f
        target == base -> 1f
        else -> ((streakDays - base).toFloat() / (target - base).toFloat()).coerceIn(0f, 1f)
    }
    val streakIcons = mapOf(
        3 to R.drawable.ic_streak_3,
        5 to R.drawable.ic_streak_5,
        7 to R.drawable.ic_streak_7
    )
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("連續結算進度（3／5／7）", style = MaterialTheme.typography.titleSmall, color = FarmText)
            LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth(), color = FarmGrowth)
            Text(
                if (next == null) "已達成全部里程碑（${streakDays} 日）"
                else "目前 ${streakDays} 日 → 下一目標 ${next} 日",
                style = MaterialTheme.typography.bodySmall,
                color = FarmText
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                milestones.forEach { m ->
                    val done = streakDays >= m || m in unlocked
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painterResource(streakIcons.getValue(m)),
                            contentDescription = "連續 $m 日",
                            modifier = Modifier.size(32.dp),
                            contentScale = ContentScale.FillBounds,
                            alpha = if (done) 1f else 0.4f
                        )
                        Text(if (done) "✓ $m" else "$m", style = MaterialTheme.typography.labelSmall, color = FarmText)
                    }
                }
            }
        }
    }
}

@Composable
private fun FarmPetPreviewStrip(
    growingCount: Int,
    readyCount: Int,
    emptyCount: Int,
    petName: String,
    petAffection: Int,
    onFarm: () -> Unit,
    onPet: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(Modifier.weight(1f).clickable(onClick = onFarm)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(R.drawable.ic_preview_farm),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.size(8.dp))
                Column {
                    Text("農田預覽", style = MaterialTheme.typography.titleSmall, color = FarmText)
                    Text("空 $emptyCount · 成長 $growingCount · 可收 $readyCount", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Card(Modifier.weight(1f).clickable(onClick = onPet)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(R.drawable.ic_preview_pet),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.size(8.dp))
                Column {
                    Text(petName, style = MaterialTheme.typography.titleSmall, color = FarmText)
                    Text("親密度 $petAffection／100", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

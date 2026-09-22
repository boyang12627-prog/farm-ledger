package com.farmledger.app.ui.screens.settle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmledger.app.R
import com.farmledger.app.domain.model.DayPhase
import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.RewardRules
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.theme.FarmGrowth
import com.farmledger.app.ui.theme.FarmSoil
import com.farmledger.app.ui.theme.FarmText
import java.time.LocalDate

@Composable
fun SettleScreen(vm: AppViewModel, onBack: () -> Unit) {
    val progress by vm.progress.collectAsState()
    val message by vm.message.collectAsState()
    val entries by vm.entries.collectAsState()
    val gameDay by vm.gameDay.collectAsState()
    val ceremony by vm.settleCeremonyAwarded.collectAsState()
    val today = LocalDate.now().toString()
    val hasActivity = entries.any { it.status == EntryStatus.ACTIVE }
    val already = progress.lastSettleDate == today
    val phase = gameDay.phase
    val moonAlpha by animateFloatAsState(
        targetValue = if (ceremony || already) 1f else if (phase == DayPhase.NIGHT) 0.9f else 0.6f,
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
        } else moonFrame = 0
    }
    val moonRes = when (moonFrame) {
        1 -> R.drawable.moon_rise_f1
        2 -> R.drawable.moon_rise_f2
        else -> R.drawable.moon_rise_f0
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("夜結儀式", style = MaterialTheme.typography.headlineMedium, color = FarmText)
        Spacer(Modifier.height(8.dp))
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (phase == DayPhase.NIGHT) Color(0xFF2A3A68) else MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                Modifier.padding(20.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painterResource(moonRes),
                    contentDescription = "日結月亮升起",
                    modifier = Modifier.size(80.dp).alpha(moonAlpha),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "固定 +${RewardRules.DAILY_GROWTH_POINTS} 成長點",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (phase == DayPhase.NIGHT) Color(0xFFF4D078) else FarmGrowth,
                    fontSize = 28.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "與筆數／金額無關 · 每日一次",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (phase == DayPhase.NIGHT) Color(0xFFEDE6D9) else FarmText
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "有帳本活動（含「無交易日」）即可結算。編輯後唔會再發獎。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (phase == DayPhase.NIGHT) Color(0xFFC8BCA8) else FarmText
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "今日已有帳本活動：${if (hasActivity) "是" else "否"}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = FarmText
        )
        Text("今日已結算：${if (already) "是" else "否"}", color = FarmText)
        Text("時段：${phase.nameZh}", color = FarmSoil)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { vm.settleToday() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !progress.clockPaused
        ) {
            Image(
                painterResource(R.drawable.ic_moon_settle),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
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
                Modifier.padding(top = 16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painterResource(R.drawable.settle_banner),
                    contentDescription = "今日結清",
                    modifier = Modifier.size(width = 180.dp, height = 52.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.height(8.dp))
                Image(
                    painterResource(R.drawable.ic_settle_stamp),
                    contentDescription = "結清印章",
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.ic_growth_point),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        "＋${RewardRules.DAILY_GROWTH_POINTS} 成長點",
                        fontWeight = FontWeight.Bold,
                        color = FarmGrowth,
                        fontSize = 22.sp
                    )
                }
                TextButton(onClick = { vm.consumeSettleCeremony() }) { Text("收下") }
            }
        }
        message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = FarmText)
            TextButton(onClick = { vm.consumeMessage() }) { Text("清除提示") }
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onBack) { Text("返回") }
    }
}

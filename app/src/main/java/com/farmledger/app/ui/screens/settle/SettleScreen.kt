package com.farmledger.app.ui.screens.settle

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import com.farmledger.app.R
import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.RewardRules
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.theme.FarmGrowth
import com.farmledger.app.ui.theme.FarmText
import java.time.LocalDate

@Composable
fun SettleScreen(vm: AppViewModel, onBack: () -> Unit) {
    val progress by vm.progress.collectAsState()
    val message by vm.message.collectAsState()
    val entries by vm.entries.collectAsState()
    val today = LocalDate.now().toString()
    val hasActivity = entries.any { it.status == EntryStatus.ACTIVE }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("每日結算", style = MaterialTheme.typography.headlineMedium, color = FarmText)
        Spacer(Modifier.height(8.dp))
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                Modifier.padding(20.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painterResource(R.drawable.ic_growth_point),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "固定 +${RewardRules.DAILY_GROWTH_POINTS} 成長點",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = FarmGrowth,
                    fontSize = 28.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "與筆數／金額無關 · 每日一次",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = FarmText
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "有帳本活動（含「無交易日」）即可結算。編輯後唔會再發獎。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FarmText
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
        Text("今日已結算：${if (progress.lastSettleDate == today) "是" else "否"}", color = FarmText)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { vm.settleToday() }, modifier = Modifier.fillMaxWidth()) {
            Text("執行結算")
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

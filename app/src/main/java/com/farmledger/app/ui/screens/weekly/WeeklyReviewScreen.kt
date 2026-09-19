package com.farmledger.app.ui.screens.weekly

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.farmledger.app.domain.model.RewardRules
import com.farmledger.app.ui.AppViewModel

@Composable
fun WeeklyReviewScreen(vm: AppViewModel, onBack: () -> Unit) {
    val progress by vm.progress.collectAsState()
    val message by vm.message.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("週回顧", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("根據本週結算天數發放成長點，上限 ${RewardRules.WEEKLY_REVIEW_CAP_POINTS} 點。")
        Text("已領取週次：${progress.weeklyReviewClaimedWeekId ?: "無"}")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { vm.claimWeekly() }, modifier = Modifier.fillMaxWidth()) {
            Text("領取本週回顧獎勵")
        }
        message?.let {
            Spacer(Modifier.height(8.dp))
            Text(it)
            TextButton(onClick = { vm.consumeMessage() }) { Text("清除") }
        }
        TextButton(onClick = onBack) { Text("返回") }
    }
}

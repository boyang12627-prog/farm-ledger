package com.farmledger.app.ui.screens.settle

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
import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.ui.AppViewModel
import java.time.LocalDate

@Composable
fun SettleScreen(vm: AppViewModel, onBack: () -> Unit) {
    val progress by vm.progress.collectAsState()
    val message by vm.message.collectAsState()
    val entries by vm.entries.collectAsState()
    val today = LocalDate.now().toString()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("每日結算", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("規則：有帳本活動（含「無交易日」）即可結算，固定獲得 1 成長點，每日一次。")
        Text("金額／筆數不影響；編輯後唔會再發獎。")
        Spacer(Modifier.height(8.dp))
        Text("今日有效筆數：${entries.count { it.status == EntryStatus.ACTIVE }}")
        Text("今日已結算：${if (progress.lastSettleDate == today) "是" else "否"}")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { vm.settleToday() }, modifier = Modifier.fillMaxWidth()) {
            Text("執行結算")
        }
        message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it)
            TextButton(onClick = { vm.consumeMessage() }) { Text("清除提示") }
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onBack) { Text("返回") }
    }
}

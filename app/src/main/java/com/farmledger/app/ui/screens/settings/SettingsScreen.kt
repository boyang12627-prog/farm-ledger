package com.farmledger.app.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.farmledger.app.ui.AppViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val message by vm.message.collectAsState()
    val progress by vm.progress.collectAsState()

    val createJson = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = vm.exportJson()
            context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
            vm.consumeMessage()
        }
    }
    val createCsv = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = vm.exportCsv()
            context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
        }
    }
    val openJson = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes().toString(Charsets.UTF_8)
            } ?: return@launch
            vm.importJson(text)
        }
    }
    val openCsv = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes().toString(Charsets.UTF_8)
            } ?: return@launch
            vm.importCsv(text)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("設定", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("完全離線・無 INTERNET 權限・無廣告／分析／登入")
        Text("版本 0.1.0-mvp")
        Spacer(Modifier.height(16.dp))
        Text("資料匯出／匯入（SAF）", style = MaterialTheme.typography.titleMedium)
        Button(onClick = { createJson.launch("farm-ledger-backup.json") }, modifier = Modifier.fillMaxWidth()) {
            Text("匯出 JSON")
        }
        Button(onClick = { createCsv.launch("farm-ledger-entries.csv") }, modifier = Modifier.fillMaxWidth()) {
            Text("匯出 CSV（帳目）")
        }
        OutlinedButton(
            onClick = { openJson.launch(arrayOf("application/json", "text/*", "*/*")) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("匯入 JSON") }
        OutlinedButton(
            onClick = { openCsv.launch(arrayOf("text/csv", "text/*", "*/*")) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("匯入 CSV") }
        Spacer(Modifier.height(16.dp))
        if (progress.clockPaused) {
            Text("時鐘倒退保護：開啟中")
            TextButton(onClick = { vm.clearClockPause() }) { Text("手動恢復獎勵") }
        }
        message?.let {
            Text(it)
            TextButton(onClick = { vm.consumeMessage() }) { Text("清除提示") }
        }
        Spacer(Modifier.height(24.dp))
        Text("開源授權見 THIRD_PARTY_LICENSES.md（專案根目錄）。", style = MaterialTheme.typography.bodySmall)
    }
}

package com.farmledger.app.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.farmledger.app.ui.AppViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    vm: AppViewModel,
    onOpenLegacyFarm: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val message by vm.message.collectAsState()
    val progress by vm.progress.collectAsState()
    val accounts by vm.accounts.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var renameId by remember { mutableStateOf<String?>(null) }
    var nameDraft by remember { mutableStateOf("") }

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
            .windowInsetsPadding(WindowInsets.navigationBars)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack) { Text("← 返回") }
        }
        Text("設定", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("完全離線・無 INTERNET 權限・無廣告／分析／登入")
        Text("版本 0.5.4e-bare")
        Spacer(Modifier.height(12.dp))
        Text("牧場進階", style = MaterialTheme.typography.titleMedium)
        Text(
            "舊一日循環（種田／日結／睡覺）已移出主牧場；僅進階使用。",
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedButton(
            onClick = onOpenLegacyFarm,
            modifier = Modifier.fillMaxWidth()
        ) { Text("牧場進階／舊農場") }
        Spacer(Modifier.height(16.dp))

        Text("帳戶（真港幣・免費唔鎖）", style = MaterialTheme.typography.titleMedium)
        Text("新增／改名；流水保留。轉帳喺入帳揀「轉帳」。", style = MaterialTheme.typography.bodySmall)
        accounts.filter { !it.archived }.forEach { a ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(a.nameZh, Modifier.weight(1f))
                TextButton(onClick = {
                    renameId = a.id
                    nameDraft = a.nameZh
                }) { Text("改名") }
            }
        }
        OutlinedButton(onClick = { showAdd = true; nameDraft = "" }, modifier = Modifier.fillMaxWidth()) {
            Text("＋新增帳戶")
        }
        if (showAdd) {
            AlertDialog(
                onDismissRequest = { showAdd = false },
                title = { Text("新增帳戶") },
                text = {
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it },
                        label = { Text("名稱（PayMe／信用卡／銀行…）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (nameDraft.isNotBlank()) {
                            vm.addAccount(nameDraft)
                            showAdd = false
                        }
                    }) { Text("新增") }
                },
                dismissButton = { TextButton(onClick = { showAdd = false }) { Text("取消") } }
            )
        }
        renameId?.let { id ->
            AlertDialog(
                onDismissRequest = { renameId = null },
                title = { Text("改名帳戶") },
                text = {
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it },
                        label = { Text("新名稱") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        vm.renameAccount(id, nameDraft)
                        renameId = null
                    }) { Text("儲存") }
                },
                dismissButton = { TextButton(onClick = { renameId = null }) { Text("取消") } }
            )
        }

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

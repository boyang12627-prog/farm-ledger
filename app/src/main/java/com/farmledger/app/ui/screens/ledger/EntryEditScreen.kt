package com.farmledger.app.ui.screens.ledger

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.ui.AppViewModel

@Composable
fun EntryEditScreen(vm: AppViewModel, entryId: String?, onDone: () -> Unit) {
    val existing = entryId?.let { vm.entryById(it) }
    var type by remember { mutableStateOf(existing?.type ?: EntryType.EXPENSE) }
    var amountText by remember {
        mutableStateOf(
            existing?.let { if (it.amountMinor == 0L) "" else (it.amountMinor / 100.0).toString() } ?: ""
        )
    }
    var note by remember { mutableStateOf(existing?.note ?: "") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (existing == null) "新增帳目" else "編輯帳目", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("金額唔會影響成長點獎勵；編輯亦唔會再次發獎。", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        androidx.compose.foundation.layout.Row {
            listOf(EntryType.INCOME, EntryType.EXPENSE, EntryType.NO_TRADE).forEach { t ->
                FilterChip(
                    selected = type == t,
                    onClick = { type = t },
                    label = { Text(typeZh(t)) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
        if (type != EntryType.NO_TRADE) {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("金額（元）") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("備註") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val major = amountText.toDoubleOrNull() ?: 0.0
                val minor = (major * 100).toLong()
                if (existing == null) vm.addEntry(type, minor, note) else vm.updateEntry(existing.id, type, minor, note)
                onDone()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("儲存") }
        if (existing != null) {
            TextButton(onClick = { vm.voidEntry(existing.id); onDone() }) {
                Text("作廢此筆")
            }
        }
        TextButton(onClick = onDone) { Text("取消") }
    }
}

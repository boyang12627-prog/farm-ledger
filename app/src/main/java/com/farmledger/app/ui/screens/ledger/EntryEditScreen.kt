package com.farmledger.app.ui.screens.ledger

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.farmledger.app.R
import com.farmledger.app.domain.model.DefaultAccounts
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.screens.entry.AccountPillRow
import com.farmledger.app.ui.theme.FarmText

@Composable
fun EntryEditScreen(vm: AppViewModel, entryId: String?, onDone: () -> Unit) {
    val existing = entryId?.let { vm.entryById(it) }
    val accounts by vm.accounts.collectAsState()
    val isEdit = existing != null
    var type by remember(entryId) { mutableStateOf(existing?.type ?: EntryType.EXPENSE) }
    var amountText by remember(entryId) {
        mutableStateOf(
            existing?.let { if (it.amountMinor == 0L) "" else (it.amountMinor / 100.0).toString() } ?: ""
        )
    }
    var note by remember(entryId) { mutableStateOf(existing?.note ?: "") }
    var category by remember(entryId) {
        mutableStateOf(
            LedgerCategory.fromStorage(existing?.category) ?: LedgerCategory.FOOD
        )
    }
    var accountId by remember(entryId) {
        mutableStateOf(existing?.accountId ?: DefaultAccounts.CASH_ID)
    }
    var transferToId by remember(entryId) {
        mutableStateOf(existing?.transferAccountId)
    }
    var showAdd by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    val active = accounts.filter { !it.archived }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(if (!isEdit) "新增帳目" else "編輯帳目", style = MaterialTheme.typography.headlineMedium, color = FarmText)
        Spacer(Modifier.height(8.dp))
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(R.drawable.ic_growth_point),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.size(10.dp))
                Column {
                    Text(
                        if (isEdit) "更新不會再發成長點"
                        else "獎勵與金額／筆數無關",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmText
                    )
                    Text(
                        if (isEdit) "儲存編輯後唔會再次發放成長點；每日結算仍固定 1 點（與筆數／金額無關）。"
                        else "金額唔會影響成長點；結算固定 1 點／日。之後若編輯帳目亦唔會再發獎。",
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmText
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row {
            listOf(EntryType.INCOME, EntryType.EXPENSE, EntryType.TRANSFER, EntryType.NO_TRADE).forEach { t ->
                FilterChip(
                    selected = type == t,
                    onClick = { type = t },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painterResource(entryTypeIcon(t)),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                contentScale = ContentScale.FillBounds
                            )
                            Spacer(Modifier.size(4.dp))
                            Text(typeZh(t))
                        }
                    },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
        if (type != EntryType.NO_TRADE) {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("金額（港幣 HK\$）") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (type != EntryType.NO_TRADE && type != EntryType.TRANSFER) {
            Text("分類・${category.farmObjectZh}", color = FarmText, style = MaterialTheme.typography.labelLarge)
            Row {
                val cats = if (type == EntryType.INCOME) {
                    listOf(LedgerCategory.INCOME, LedgerCategory.OTHER)
                } else {
                    LedgerCategory.expenseChips
                }
                cats.take(5).forEach { c ->
                    FilterChip(
                        selected = category == c,
                        onClick = { category = c },
                        label = { Text(c.nameZh) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
            Text("帳戶", color = FarmText, style = MaterialTheme.typography.labelLarge)
            AccountPillRow(
                accounts = active,
                selectedId = accountId,
                onSelect = { accountId = it },
                onAdd = { showAdd = true }
            )
        }
        if (type == EntryType.TRANSFER) {
            Text("由帳戶", color = FarmText, style = MaterialTheme.typography.labelLarge)
            AccountPillRow(
                accounts = active,
                selectedId = accountId,
                onSelect = { accountId = it },
                onAdd = { showAdd = true }
            )
            Text("到帳戶", color = FarmText, style = MaterialTheme.typography.labelLarge)
            AccountPillRow(
                accounts = active.filter { it.id != accountId },
                selectedId = transferToId,
                onSelect = { transferToId = it },
                onAdd = { showAdd = true },
                allowEmptyHint = "揀對方帳戶"
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
                val catName = when (type) {
                    EntryType.NO_TRADE, EntryType.TRANSFER -> null
                    else -> category.name
                }
                if (existing == null) {
                    vm.addEntry(
                        type, minor, note,
                        category = catName,
                        accountId = accountId,
                        transferAccountId = if (type == EntryType.TRANSFER) transferToId else null
                    )
                } else {
                    vm.updateEntry(
                        existing.id, type, minor, note,
                        category = catName,
                        accountId = accountId,
                        transferAccountId = if (type == EntryType.TRANSFER) transferToId else null
                    )
                }
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
        Spacer(Modifier.height(24.dp))
    }

    if (showAdd) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("新增帳戶") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("名稱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        vm.addAccount(newName)
                        newName = ""
                        showAdd = false
                    }
                }) { Text("新增") }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false }) { Text("取消") }
            }
        )
    }
}

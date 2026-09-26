package com.farmledger.app.ui.screens.ledger

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.farmledger.app.R
import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerAccount
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.model.LedgerEntry
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.theme.FarmExpense
import com.farmledger.app.ui.theme.FarmIncome
import com.farmledger.app.ui.theme.FarmSoil
import com.farmledger.app.ui.theme.FarmText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    vm: AppViewModel,
    onAdd: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val all by vm.allEntries.collectAsState()
    val accounts by vm.accounts.collectAsState()
    val message by vm.message.collectAsState()
    var query by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<String?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    val accountNames = remember(accounts) {
        accounts.associate { it.id to it.nameZh }
    }
    val filtered = remember(all, query, accountNames) {
        filterEntries(all, query, accountNames)
    }

    if (showEditor) {
        EntryEditScreen(
            vm = vm,
            entryId = editingId,
            onDone = {
                showEditor = false
                editingId = null
            }
        )
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingId = null
                showEditor = true
                onAdd()
            }) {
                Icon(Icons.Default.Add, contentDescription = "新增")
            }
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp)
        ) {
            Text("帳簿", style = MaterialTheme.typography.headlineMedium, color = FarmText)
            Text("真港幣跨日列表 · 商店買賣唔在此", style = MaterialTheme.typography.bodySmall, color = FarmSoil)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("搜尋備註／金額／分類／帳戶") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            TextButton(onClick = onOpenSettings) { Text("設定／匯出") }
            message?.let { msg ->
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(msg, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                        TextButton(onClick = { vm.consumeMessage() }) { Text("清除提示") }
                    }
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { e ->
                    Card(
                        onClick = {
                            if (e.status == EntryStatus.ACTIVE) {
                                editingId = e.id
                                showEditor = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(e.localDate, style = MaterialTheme.typography.labelMedium, color = FarmSoil)
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painterResource(entryTypeIcon(e.type)),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        contentScale = ContentScale.FillBounds
                                    )
                                    Spacer(Modifier.size(8.dp))
                                    Column {
                                        Text(
                                            typeZh(e.type),
                                            color = amountColor(e.type),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        val cat = LedgerCategory.fromStorage(e.category)
                                        if (cat != null) {
                                            Text(
                                                "${cat.nameZh}・${cat.farmObjectZh}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = FarmSoil
                                            )
                                        }
                                        Text(
                                            accountLine(e, accountNames),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FarmSoil
                                        )
                                    }
                                }
                                Text(
                                    when (e.type) {
                                        EntryType.NO_TRADE -> "—"
                                        EntryType.INCOME -> "+$${formatMinor(e.amountMinor)}"
                                        EntryType.EXPENSE -> "-$${formatMinor(e.amountMinor)}"
                                        EntryType.TRANSFER -> "↔$${formatMinor(e.amountMinor)}"
                                    },
                                    color = amountColor(e.type),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (e.note.isNotBlank()) Text(e.note, style = MaterialTheme.typography.bodySmall)
                            if (e.status == EntryStatus.VOIDED) {
                                Text("（已作廢）", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            if (filtered.isEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("冇符合嘅帳目。去「入帳」記一筆真港幣。", color = FarmSoil)
            }
        }
    }
}

fun filterEntries(
    entries: List<LedgerEntry>,
    query: String,
    accountNames: Map<String, String> = emptyMap()
): List<LedgerEntry> {
    val q = query.trim()
    if (q.isEmpty()) return entries
    val qLower = q.lowercase()
    return entries.filter { e ->
        e.note.lowercase().contains(qLower) ||
            e.category?.lowercase()?.contains(qLower) == true ||
            LedgerCategory.fromStorage(e.category)?.nameZh?.contains(q) == true ||
            formatMinor(e.amountMinor).contains(q) ||
            e.amountMinor.toString().contains(q) ||
            e.localDate.contains(q) ||
            typeZh(e.type).contains(q) ||
            accountNames[e.accountId]?.contains(q) == true ||
            accountNames[e.transferAccountId]?.contains(q) == true ||
            e.accountId.contains(qLower) ||
            (e.transferAccountId?.contains(qLower) == true)
    }
}

fun accountLine(e: LedgerEntry, accountNames: Map<String, String>): String {
    val from = accountNames[e.accountId] ?: e.accountId
    return when (e.type) {
        EntryType.TRANSFER -> {
            val to = e.transferAccountId?.let { accountNames[it] ?: it } ?: "?"
            "轉帳 $from → $to"
        }
        else -> "帳戶・$from"
    }
}

fun entryTypeIcon(type: EntryType): Int = when (type) {
    EntryType.INCOME -> R.drawable.ic_income
    EntryType.EXPENSE -> R.drawable.ic_expense
    EntryType.NO_TRADE -> R.drawable.ic_no_trade
    EntryType.TRANSFER -> R.drawable.ic_ledger_book
}

/** 收入＝綠；支出＝磚紅；無交易日／轉帳＝文字色 */
fun amountColor(type: EntryType): Color = when (type) {
    EntryType.INCOME -> FarmIncome
    EntryType.EXPENSE -> FarmExpense
    EntryType.NO_TRADE, EntryType.TRANSFER -> FarmText
}

fun typeZh(t: EntryType) = when (t) {
    EntryType.INCOME -> "收入"
    EntryType.EXPENSE -> "支出"
    EntryType.NO_TRADE -> "無交易日"
    EntryType.TRANSFER -> "轉帳"
}

fun formatMinor(minor: Long): String {
    val major = minor / 100.0
    return "%.2f".format(major)
}

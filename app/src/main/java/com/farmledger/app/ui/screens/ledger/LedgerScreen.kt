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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.theme.FarmExpense
import com.farmledger.app.ui.theme.FarmIncome
import com.farmledger.app.ui.theme.FarmText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    vm: AppViewModel,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onSettle: () -> Unit
) {
    val entries by vm.entries.collectAsState()
    val date by vm.selectedDate.collectAsState()
    val message by vm.message.collectAsState()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
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
            Text("今日帳簿", style = MaterialTheme.typography.headlineMedium, color = FarmText)
            Text(date, style = MaterialTheme.typography.bodyMedium, color = FarmText)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onSettle) { Text("去結算 →") }
            message?.let { msg ->
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(msg, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                        TextButton(onClick = { vm.consumeMessage() }) { Text("清除提示") }
                    }
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries, key = { it.id }) { e ->
                    Card(
                        onClick = { if (e.status == EntryStatus.ACTIVE) onEdit(e.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
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
                                    Text(
                                        typeZh(e.type),
                                        color = amountColor(e.type),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    when (e.type) {
                                        EntryType.NO_TRADE -> "—"
                                        EntryType.INCOME -> "+${formatMinor(e.amountMinor)}"
                                        EntryType.EXPENSE -> "-${formatMinor(e.amountMinor)}"
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
        }
    }
}

fun entryTypeIcon(type: EntryType): Int = when (type) {
    EntryType.INCOME -> R.drawable.ic_income
    EntryType.EXPENSE -> R.drawable.ic_expense
    EntryType.NO_TRADE -> R.drawable.ic_no_trade
}

/** 收入＝綠；支出＝磚紅；無交易日＝文字色 */
fun amountColor(type: EntryType): Color = when (type) {
    EntryType.INCOME -> FarmIncome
    EntryType.EXPENSE -> FarmExpense
    EntryType.NO_TRADE -> FarmText
}

fun typeZh(t: EntryType) = when (t) {
    EntryType.INCOME -> "收入"
    EntryType.EXPENSE -> "支出"
    EntryType.NO_TRADE -> "無交易日"
}

fun formatMinor(minor: Long): String {
    val major = minor / 100.0
    return "%.2f".format(major)
}

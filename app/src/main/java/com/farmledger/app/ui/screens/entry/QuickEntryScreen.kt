package com.farmledger.app.ui.screens.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.screens.ledger.formatMinor
import com.farmledger.app.ui.theme.FarmSoil
import com.farmledger.app.ui.theme.FarmText

/**
 * M5 極速入帳：分類 chip → 金額 → 一鍵儲存（目標 ≤3 tap）。
 * 金額為真港幣分位 amountMinor；獎勵只跟有記／記全，唔跟金額。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickEntryScreen(
    vm: AppViewModel,
    onSaved: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    var type by remember { mutableStateOf(EntryType.EXPENSE) }
    var category by remember { mutableStateOf(LedgerCategory.FOOD) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("入帳", style = MaterialTheme.typography.headlineMedium, color = FarmText, fontWeight = FontWeight.Bold)
        Text("真港幣 · 獎勵唔跟金額 · ≤3 步完成", style = MaterialTheme.typography.bodySmall, color = FarmSoil)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = type == EntryType.EXPENSE,
                onClick = {
                    type = EntryType.EXPENSE
                    if (category == LedgerCategory.INCOME) category = LedgerCategory.FOOD
                },
                label = { Text("支出") }
            )
            FilterChip(
                selected = type == EntryType.INCOME,
                onClick = {
                    type = EntryType.INCOME
                    category = LedgerCategory.INCOME
                },
                label = { Text("收入") }
            )
            FilterChip(
                selected = type == EntryType.NO_TRADE,
                onClick = { type = EntryType.NO_TRADE },
                label = { Text("無交易日") }
            )
        }
        if (type != EntryType.NO_TRADE) {
            Spacer(Modifier.height(8.dp))
            Text("分類（牧場物件：${category.farmObjectZh}）", color = FarmText, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val chips = if (type == EntryType.INCOME) {
                    listOf(LedgerCategory.INCOME, LedgerCategory.SAVINGS, LedgerCategory.OTHER)
                } else {
                    LedgerCategory.expenseChips
                }
                chips.forEach { c ->
                    FilterChip(
                        selected = category == c,
                        onClick = { category = c },
                        label = { Text(c.nameZh) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("金額（港幣）") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    val major = amountText.toDoubleOrNull() ?: 0.0
                    val minor = (major * 100).toLong()
                    Text("將存 \$${formatMinor(minor)}（amountMinor=$minor）")
                }
            )
        } else {
            Spacer(Modifier.height(8.dp))
            Text("標記今日無交易＝有記，可結算發固定種子幣／成長點。", color = FarmSoil)
        }
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("備註（可空）") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val major = amountText.toDoubleOrNull() ?: 0.0
                val minor = (major * 100).toLong()
                val cat = when (type) {
                    EntryType.NO_TRADE -> null
                    EntryType.INCOME -> category.name
                    else -> category.name
                }
                vm.addEntry(type, minor, note, category = cat)
                amountText = ""
                note = ""
                onSaved()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = type == EntryType.NO_TRADE || (amountText.toDoubleOrNull() ?: 0.0) > 0.0
        ) {
            Text(if (type == EntryType.NO_TRADE) "標記無交易日" else "儲存", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
            Text("設定／匯出")
        }
    }
}

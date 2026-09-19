package com.farmledger.app.ui.screenshot

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.farmledger.app.ui.theme.FarmLedgerTheme
import com.farmledger.app.ui.theme.WarmCream
import org.junit.Rule
import org.junit.Test

/**
 * JVM Paparazzi snapshots of the four primary Traditional Chinese screens
 * with representative sample state (no emulator / lockscreen required).
 */
class ScreenScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(softButtons = false),
        theme = "android:Theme.Material3.Light.NoActionBar",
        maxPercentDifference = 0.01
    )

    @Test
    fun home() {
        paparazzi.snapshot(name = "01_home") {
            FarmLedgerTheme { HomeFixture() }
        }
    }

    @Test
    fun ledger() {
        paparazzi.snapshot(name = "02_ledger") {
            FarmLedgerTheme { LedgerFixture() }
        }
    }

    @Test
    fun settle() {
        paparazzi.snapshot(name = "03_settle") {
            FarmLedgerTheme { SettleFixture() }
        }
    }

    @Test
    fun farm() {
        paparazzi.snapshot(name = "04_farm") {
            FarmLedgerTheme { FarmFixture() }
        }
    }
}

@Composable
private fun HomeFixture() {
    Column(
        Modifier
            .fillMaxSize()
            .background(WarmCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("主頁", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("成長點：12")
                Text("種子：5")
                Text("連續結算：3 日")
                Text("上次結算：2026-09-18")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text("連續3") })
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("今日帳簿") }
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("每日結算") }
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("農田（6 格）") }
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("寵物") }
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("家居裝飾") }
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("週回顧") }
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("設定／匯出") }
    }
}

@Composable
private fun LedgerFixture() {
    data class EntryRow(val type: String, val amount: String, val note: String)
    val sample = listOf(
        EntryRow("收入", "+1500.00", "賣菜收入"),
        EntryRow("支出", "-32.00", "肥料"),
        EntryRow("無交易日", "—", "")
    )
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(Icons.Default.Add, contentDescription = "新增")
            }
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .background(WarmCream)
                .padding(pad)
                .padding(16.dp)
        ) {
            Text("今日帳簿", style = MaterialTheme.typography.headlineMedium)
            Text("2026-09-19", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = {}) { Text("去結算 →") }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sample.forEach { e ->
                    Card(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(e.type)
                                Text(e.amount)
                            }
                            if (e.note.isNotBlank()) {
                                Text(e.note, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettleFixture() {
    Column(
        Modifier
            .fillMaxSize()
            .background(WarmCream)
            .padding(16.dp)
    ) {
        Text("每日結算", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("規則：有帳本活動（含「無交易日」）即可結算，固定獲得 1 成長點，每日一次。")
        Text("金額／筆數不影響；編輯後唔會再發獎。")
        Spacer(Modifier.height(8.dp))
        Text("今日有效筆數：2")
        Text("今日已結算：否")
        Spacer(Modifier.height(16.dp))
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("執行結算") }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = {}) { Text("返回") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FarmFixture() {
    data class PlotUi(val index: Int, val label: String, val action: String?)
    val plots = listOf(
        PlotUi(0, "空地", "種植"),
        PlotUi(1, "成長中：小麥", null),
        PlotUi(2, "可收成：紅蘿蔔", "收成"),
        PlotUi(3, "空地", "種植"),
        PlotUi(4, "成長中：番茄", null),
        PlotUi(5, "空地", "種植")
    )
    val crops = listOf("小麥", "紅蘿蔔", "番茄")
    Column(
        Modifier
            .fillMaxSize()
            .background(WarmCream)
            .padding(16.dp)
    ) {
        Text("農田", style = MaterialTheme.typography.headlineMedium)
        Text("種子：5　成長點：12")
        Spacer(Modifier.height(8.dp))
        Text("選擇作物：")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            crops.forEachIndexed { i, c ->
                FilterChip(
                    selected = i == 0,
                    onClick = {},
                    label = { Text(c) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(plots, key = { it.index }) { plot ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(8.dp)) {
                        Text("田 #${plot.index + 1}")
                        Text(plot.label)
                        when (plot.action) {
                            "種植" -> Button(onClick = {}) { Text("種植") }
                            "收成" -> Button(onClick = {}) { Text("收成") }
                            else -> Text("等待…", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

package com.farmledger.app.ui.screenshot

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.farmledger.app.R
import com.farmledger.app.ui.screens.farm.FarmSceneLayer
import com.farmledger.app.domain.model.RewardRules
import com.farmledger.app.ui.theme.FarmBg
import com.farmledger.app.ui.theme.FarmExpense
import com.farmledger.app.ui.theme.FarmGrowth
import com.farmledger.app.ui.theme.FarmIncome
import com.farmledger.app.ui.theme.FarmLedgerTheme
import com.farmledger.app.ui.theme.FarmSelected
import com.farmledger.app.ui.theme.FarmSoil
import com.farmledger.app.ui.theme.FarmStroke
import com.farmledger.app.ui.theme.FarmText
import com.farmledger.app.ui.theme.WarmCream
import org.junit.Rule
import org.junit.Test

/**
 * JVM Paparazzi snapshots of the four primary Traditional Chinese screens
 * with representative sample state reflecting the playtest UX refresh
 * (primary CTAs, settle activity yes/no + fixed +1, farm demo-pace colors,
 * ledger income/expense colors). Reward rules unchanged.
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
        Text("主頁", style = MaterialTheme.typography.headlineMedium, color = FarmText)
        Spacer(Modifier.height(8.dp))
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FarmSelected.copy(alpha = 0.35f)),
            border = BorderStroke(2.dp, FarmSelected),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.ic_cta_settle),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "今日未結算",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = FarmText
                        )
                        Text(
                            "記帳後結算，固定 +${RewardRules.DAILY_GROWTH_POINTS} 成長點（每日一次）",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FarmText
                        )
                    }
                }
                Text(
                    "保持連續 3 日，唔好斷線呀！",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = FarmStroke
                )
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FarmSelected,
                        contentColor = FarmText
                    )
                ) {
                    Text("去結算，領今日成長點", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.ic_growth_point),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.size(8.dp))
                    Text("成長點：12", color = FarmText)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.ic_seed),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.size(8.dp))
                    Text("種子：5", color = FarmText)
                }
                Text("連續結算：3 日", color = FarmText)
                Text("上次結算：2026-09-18", color = FarmText)
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("連續結算進度（3／5／7）", style = MaterialTheme.typography.titleSmall, color = FarmText)
                LinearProgressIndicator(
                    progress = { 0f },
                    modifier = Modifier.fillMaxWidth(),
                    color = FarmGrowth
                )
                Text("目前 3 日 → 下一目標 5 日", style = MaterialTheme.typography.bodySmall, color = FarmText)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        3 to R.drawable.ic_streak_3,
                        5 to R.drawable.ic_streak_5,
                        7 to R.drawable.ic_streak_7
                    ).forEach { (m, res) ->
                        val done = m == 3
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painterResource(res),
                                contentDescription = "連續 $m 日",
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.FillBounds,
                                alpha = if (done) 1f else 0.4f
                            )
                            Text(
                                if (done) "✓ $m" else "$m",
                                style = MaterialTheme.typography.labelSmall,
                                color = FarmText
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(Modifier.weight(1f)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.ic_preview_farm),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.size(8.dp))
                    Column {
                        Text("農田預覽", style = MaterialTheme.typography.titleSmall, color = FarmText)
                        Text("空 3 · 成長 2 · 可收 1", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Card(Modifier.weight(1f)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.ic_preview_pet),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(Modifier.size(8.dp))
                    Column {
                        Text("小芽", style = MaterialTheme.typography.titleSmall, color = FarmText)
                        Text("親密度 40／100", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "今日要事",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = FarmText
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FarmGrowth, contentColor = FarmText)
        ) {
            Image(
                painterResource(R.drawable.ic_cta_ledger),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.FillBounds
            )
            Spacer(Modifier.size(10.dp))
            Text("今日帳簿", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FarmSelected, contentColor = FarmText)
        ) {
            Image(
                painterResource(R.drawable.ic_cta_settle),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.FillBounds
            )
            Spacer(Modifier.size(10.dp))
            Text("每日結算", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(16.dp))
        Text("其他", style = MaterialTheme.typography.titleSmall, color = FarmText.copy(alpha = 0.7f))
        Spacer(Modifier.height(4.dp))
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("農田（6 格）") }
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("週回顧") }
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("設定／匯出") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = {}) { Text("寵物") }
            TextButton(onClick = {}) { Text("家居裝飾") }
        }
    }
}

@Composable
private fun LedgerFixture() {
    data class EntryRow(val type: String, val amount: String, val note: String, val color: Color, val icon: Int)
    val sample = listOf(
        EntryRow("收入", "+1500.00", "賣菜收入", FarmIncome, R.drawable.ic_income),
        EntryRow("支出", "-32.00", "肥料", FarmExpense, R.drawable.ic_expense),
        EntryRow("無交易日", "—", "", FarmText, R.drawable.ic_no_trade)
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
            Text("今日帳簿", style = MaterialTheme.typography.headlineMedium, color = FarmText)
            Text("2026-09-19", style = MaterialTheme.typography.bodyMedium, color = FarmText)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = {}) { Text("去結算 →") }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sample.forEach { e ->
                    Card(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painterResource(e.icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        contentScale = ContentScale.FillBounds
                                    )
                                    Spacer(Modifier.size(8.dp))
                                    Text(e.type, color = e.color, fontWeight = FontWeight.SemiBold)
                                }
                                Text(e.amount, color = e.color, fontWeight = FontWeight.Bold)
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
        Text("每日結算", style = MaterialTheme.typography.headlineMedium, color = FarmText)
        Spacer(Modifier.height(8.dp))
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                Modifier.padding(20.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painterResource(R.drawable.ic_growth_point),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.FillBounds
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "固定 +${RewardRules.DAILY_GROWTH_POINTS} 成長點",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = FarmGrowth,
                    fontSize = 28.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "與筆數／金額無關 · 每日一次",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = FarmText
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "有帳本活動（含「無交易日」）即可結算。編輯後唔會再發獎。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FarmText
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "今日已有帳本活動：是",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = FarmText
        )
        Text("今日已結算：否", color = FarmText)
        Spacer(Modifier.height(16.dp))
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("執行結算") }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = {}) { Text("返回") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FarmFixture() {
    data class PlotUi(
        val index: Int,
        val label: String,
        val action: String?,
        val remain: String?,
        val cardColor: Color,
        val icon: Int
    )
    val plots = listOf(
        PlotUi(0, "空地", "種植", null, Color(0xFFE8D9B5), R.drawable.tile_soil_empty),
        PlotUi(1, "成長中：小麥", null, "示範節奏剩餘 0:42", Color(0xFFD4E8C0), R.drawable.crop_wheat_grow),
        PlotUi(2, "可收成：紅蘿蔔", "收成", null, Color(0xFFF5D9A0), R.drawable.crop_carrot_ready),
        PlotUi(3, "空地", "種植", null, Color(0xFFE8D9B5), R.drawable.tile_soil_empty),
        PlotUi(4, "成長中：番茄", null, "示範節奏剩餘 1:18", Color(0xFFD4E8C0), R.drawable.crop_tomato_grow),
        PlotUi(5, "空地", "種植", null, Color(0xFFE8D9B5), R.drawable.tile_soil_empty)
    )
    val crops = listOf(
        Triple("小麥（示範節奏 1 分）", R.drawable.crop_wheat_ready, true),
        Triple("紅蘿蔔（示範節奏 2 分）", R.drawable.crop_carrot_ready, false),
        Triple("番茄（示範節奏 3 分）", R.drawable.crop_tomato_ready, false)
    )
    Column(
        Modifier
            .fillMaxSize()
            .background(WarmCream)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("農田", style = MaterialTheme.typography.headlineMedium, color = FarmText)
            Spacer(Modifier.size(8.dp))
            Image(
                painterResource(R.drawable.ic_seed),
                null,
                Modifier.size(24.dp),
                contentScale = ContentScale.FillBounds
            )
            Text(" 5", color = FarmText)
            Spacer(Modifier.size(12.dp))
            Image(
                painterResource(R.drawable.ic_growth_point),
                null,
                Modifier.size(24.dp),
                contentScale = ContentScale.FillBounds
            )
            Text(" 12", color = FarmText)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "示範節奏：作物成長約 1～3 分鐘（小麥 1 分／紅蘿蔔 2 分／番茄 3 分），非真實農作時間。",
            style = MaterialTheme.typography.bodySmall,
            color = FarmSoil
        )
        Spacer(Modifier.height(8.dp))
        Text("選擇作物：", color = FarmText)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            crops.forEach { (label, icon, selected) ->
                FilterChip(
                    selected = selected,
                    onClick = {},
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painterResource(icon),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                contentScale = ContentScale.FillBounds
                            )
                            Spacer(Modifier.size(4.dp))
                            Text(label)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = FarmBg,
                        labelColor = FarmText,
                        selectedContainerColor = FarmSelected,
                        selectedLabelColor = FarmText
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = FarmSoil,
                        selectedBorderColor = FarmSelected
                    )
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        val farmPanelGrass = Color(0xFFC5D99A)
        val farmPanelDirt = Color(0xFFD9C48A)
        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = farmPanelGrass),
            border = BorderStroke(2.dp, FarmStroke.copy(alpha = 0.65f)),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                FarmSceneLayer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                    interactive = false
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(farmPanelDirt)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(farmPanelDirt)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
            items(plots, key = { it.index }) { plot ->
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = plot.cardColor),
                    border = BorderStroke(1.dp, FarmStroke.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "田 #${plot.index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = FarmText
                        )
                        Image(
                            painterResource(plot.icon),
                            contentDescription = plot.label,
                            modifier = Modifier.size(48.dp),
                            contentScale = ContentScale.FillBounds
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            plot.label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = FarmText
                        )
                        when (plot.action) {
                            "種植" -> Button(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FarmSoil,
                                    contentColor = Color.White
                                )
                            ) {
                                Image(
                                    painterResource(R.drawable.btn_plant),
                                    null,
                                    Modifier.size(18.dp),
                                    contentScale = ContentScale.FillBounds
                                )
                                Spacer(Modifier.size(4.dp))
                                Text("種植")
                            }
                            "收成" -> Button(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FarmGrowth,
                                    contentColor = Color.White
                                )
                            ) {
                                Image(
                                    painterResource(R.drawable.btn_harvest),
                                    null,
                                    Modifier.size(18.dp),
                                    contentScale = ContentScale.FillBounds
                                )
                                Spacer(Modifier.size(4.dp))
                                Text("收成")
                            }
                            else -> Text(
                                plot.remain ?: "等待…",
                                style = MaterialTheme.typography.bodySmall,
                                color = FarmSoil,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
            }
        }
    }
}

package com.farmledger.app.ui.screens.diary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.farmledger.app.R
import com.farmledger.app.domain.model.EntryStatus
import com.farmledger.app.domain.usecase.MissedCategoryLogic
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.theme.FarmSoil
import com.farmledger.app.ui.theme.FarmText
import java.time.LocalDate

private val Parchment = Color(0xFFF5E6C8)
private val Wood = Color(0xFF6B4A2E)
private val StampRed = Color(0xFFC45C4A)

/**
 * M5 日記殼：A2 cover／page 紙感；今日有記／未記、漏記分類長草標記、週回顧入口。
 * 完整日記編輯器留 M6–M7。
 */
@Composable
fun DiaryScreen(
    vm: AppViewModel,
    onOpenWeekly: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val allEntries by vm.allEntries.collectAsState()
    val progress by vm.progress.collectAsState()
    val today = LocalDate.now().toString()
    val todayActive = remember(allEntries, today) {
        allEntries.filter { it.localDate == today && it.status == EntryStatus.ACTIVE }
    }
    val hasRecorded = todayActive.isNotEmpty()
    val settled = progress.lastSettleDate == today
    val missed = remember(allEntries, today) {
        MissedCategoryLogic.missedExpenseChips(allEntries, today)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Parchment)
    ) {
        Image(
            painter = painterResource(R.drawable.diary_cover),
            contentDescription = "日記封面",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.35f
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "日記",
                style = MaterialTheme.typography.headlineMedium,
                color = FarmText,
                fontWeight = FontWeight.Bold
            )
            Text("殼層｜完整編輯器之後再做", style = MaterialTheme.typography.bodySmall, color = FarmSoil)
            Spacer(Modifier.height(12.dp))

            // A2 日記內頁紙感
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Wood, RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.diary_page),
                    contentDescription = "日記內頁",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Parchment.copy(alpha = 0.82f))
                        .padding(16.dp)
                ) {
                    Text(
                        when {
                            settled -> "今日已記並結清 ✓"
                            hasRecorded -> "今日有記（尚未結算）"
                            else -> "今日未記"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmText
                    )
                    Text(
                        "有效帳 ${todayActive.size} 筆 · 結算獎勵只跟有記／記全，唔跟金額",
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmSoil
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("牧場物件・漏記長草", style = MaterialTheme.typography.titleSmall, color = FarmText)
                    if (missed.isEmpty()) {
                        Text("常用分類今日都有影子，牧場冇長草。", color = FarmSoil)
                    } else {
                        missed.forEach { c ->
                            Text("🌿 ${c.farmObjectZh}（${c.nameZh}）旁長草", color = FarmSoil)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onOpenWeekly,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Wood,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("週回顧")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.5.dp, StampRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("設定／匯出", color = StampRed)
            }
        }
    }
}

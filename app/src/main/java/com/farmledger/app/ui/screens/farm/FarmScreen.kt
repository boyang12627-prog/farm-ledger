package com.farmledger.app.ui.screens.farm

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.farmledger.app.R
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.usecase.FarmStageLogic
import com.farmledger.app.domain.usecase.MissedCategoryLogic
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.components.RanchTopBar
import com.farmledger.app.ui.theme.FarmText
import java.time.LocalDate
import kotlinx.coroutines.delay

private val SoftRanchSky = Color(0xFFB8D4A8)

/**
 * 預設牧場主畫面＝全屏 A2c 橫屏組裝（浮空頂欄 chip＋熱區底圖）。
 * Weekly review lives on Diary tab；設定 → 頂欄齒輪。
 * 舊一日循環種田殼已搬去 [LegacyFarmDayLoopScreen]。
 * 見 docs/art/a2c/A2c_landscape_label_spec.md
 */
@Composable
fun FarmScreen(
    vm: AppViewModel,
    onOpenSettings: () -> Unit = {},
    onOpenEntry: (LedgerCategory?) -> Unit = {}
) {
    val progress by vm.progress.collectAsState()
    val gameDay by vm.gameDay.collectAsState()
    val allEntries by vm.allEntries.collectAsState()
    val accounts by vm.accounts.collectAsState()
    val message by vm.message.collectAsState()

    val caps = remember(progress.totalSettleDays) {
        FarmStageLogic.capabilities(progress.totalSettleDays)
    }
    val today = LocalDate.now().toString()
    val missed = remember(allEntries, today) {
        MissedCategoryLogic.missedCategories(allEntries, today)
    }
    val loggedToday = remember(allEntries, today) {
        MissedCategoryLogic.usedCategoriesToday(allEntries, today)
    }
    val weedStacks = remember(allEntries, today) {
        MissedCategoryLogic.weedStacksByCategory(allEntries, today)
    }
    val activeAccountCount = remember(accounts) {
        accounts.count { !it.archived }
    }

    LaunchedEffect(Unit) {
        while (true) {
            vm.refreshFarm()
            delay(2_000)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(SoftRanchSky)
    ) {
        Image(
            painter = painterResource(R.drawable.spring_ranch_base),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.35f),
            contentScale = ContentScale.Crop
        )

        Column(Modifier.fillMaxSize()) {
            // seasonLine / weatherOrPhase → contentDescription only（頂欄圖 only；連續牌只 streak）
            RanchTopBar(
                seasonLine = "春・第 ${gameDay.gameDay} 日",
                weatherOrPhase = gameDay.phase.nameZh + "・" + caps.stage.nameZh,
                streakDays = progress.streakDays,
                seedCoins = progress.seedCoins,
                onOpenSettings = onOpenSettings
            )

            if (progress.clockPaused) {
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️ 時間倒退，獎勵暫停。", Modifier.weight(1f), color = FarmText)
                    TextButton(onClick = { vm.clearClockPause() }) { Text("已校正") }
                }
            }

            RanchHotspotScene(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                missedCategories = missed,
                weedStacks = weedStacks,
                loggedToday = loggedToday,
                activeAccountCount = activeAccountCount,
                onHotspotTap = { cat ->
                    vm.prefillEntryCategory(cat)
                    onOpenEntry(cat)
                }
            )

            message?.let {
                Text(
                    it,
                    color = FarmText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
                TextButton(onClick = { vm.consumeMessage() }) { Text("清除") }
            }
        }
    }
}

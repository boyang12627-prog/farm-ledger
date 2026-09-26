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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.farmledger.app.domain.usecase.HotspotUnlockLogic
import com.farmledger.app.domain.usecase.MissedCategoryLogic
import com.farmledger.app.domain.usecase.RanchBuildShopLogic
import com.farmledger.app.domain.usecase.ShopContext
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.components.RanchTopBar
import com.farmledger.app.ui.theme.FarmText
import java.time.LocalDate
import kotlinx.coroutines.delay

private val SoftRanchSky = Color(0xFFB8D4A8)

/**
 * 預設牧場主畫面＝全屏 A2c 橫屏組裝（浮空頂欄 chip＋淨農地底圖）。
 * 開局 owned 空＝零熱區；建造商店日結後開門。
 * 舊一日循環種田殼已搬去 [LegacyFarmDayLoopScreen]。
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    val hasReconcile = remember(allEntries) {
        HotspotUnlockLogic.hasReconcileSuccess(allEntries)
    }
    val shopCtx = remember(
        progress.totalSettleDays,
        progress.ownedHotspotIds,
        hasReconcile
    ) {
        ShopContext(
            totalSettleDays = progress.totalSettleDays,
            ownedIds = progress.ownedHotspotIds,
            hasReconcileSuccess = hasReconcile
        )
    }
    val shopOpen = RanchBuildShopLogic.isShopOpen(progress.totalSettleDays)
    var showBuildShop by remember { mutableStateOf(false) }

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
            painter = painterResource(R.drawable.spring_ranch_bare_day1),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.35f),
            contentScale = ContentScale.Crop
        )

        Column(Modifier.fillMaxSize()) {
            RanchTopBar(
                seasonLine = "春・第 ${gameDay.gameDay} 日",
                weatherOrPhase = gameDay.phase.nameZh + "・" + caps.stage.nameZh,
                ranchDay = gameDay.gameDay,
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
                ownedHotspotIds = progress.ownedHotspotIds,
                totalSettleDays = progress.totalSettleDays,
                hasReconcileSuccess = hasReconcile,
                ranchDay = gameDay.gameDay,
                missedCategories = missed,
                weedStacks = weedStacks,
                loggedToday = loggedToday,
                activeAccountCount = activeAccountCount,
                onHotspotTap = { cat ->
                    vm.prefillEntryCategory(cat)
                    onOpenEntry(cat)
                }
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!shopOpen) {
                    Text(
                        "日結後可建造",
                        color = FarmText.copy(alpha = 0.55f),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                TextButton(
                    onClick = { showBuildShop = true },
                    enabled = shopOpen
                ) {
                    Text(if (shopOpen) "建造" else "建造（日結後）")
                }
            }

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

    if (showBuildShop) {
        ModalBottomSheet(
            onDismissRequest = { showBuildShop = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            BuildShopSheet(
                seedCoins = progress.seedCoins,
                ctx = shopCtx,
                onBuy = { id ->
                    vm.purchaseRanchBuild(id)
                },
                onClose = { showBuildShop = false }
            )
        }
    }
}

@Composable
private fun BuildShopSheet(
    seedCoins: Int,
    ctx: ShopContext,
    onBuy: (String) -> Unit,
    onClose: () -> Unit
) {
    val listed = remember(ctx) { RanchBuildShopLogic.listedItems(ctx) }
    Column(Modifier.padding(16.dp)) {
        Text("建造商店（種子幣）", color = FarmText)
        Text("持有 $seedCoins 種子幣 · 唔入港幣帳", color = FarmText.copy(alpha = 0.7f))
        Spacer(Modifier.height(8.dp))
        if (listed.isEmpty()) {
            Text("暫時未有可建造項目。", color = FarmText)
        } else {
            LazyColumn(Modifier.height(280.dp)) {
                items(listed, key = { it.id }) { item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(item.nameZh, color = FarmText)
                            Text("${item.priceCoins} 種子幣", color = FarmText.copy(alpha = 0.65f))
                        }
                        TextButton(
                            onClick = { onBuy(item.id) },
                            enabled = RanchBuildShopLogic.canAfford(item.id, seedCoins)
                        ) {
                            Text("建造")
                        }
                    }
                }
            }
        }
        TextButton(onClick = onClose, modifier = Modifier.align(Alignment.End)) {
            Text("關閉")
        }
    }
}

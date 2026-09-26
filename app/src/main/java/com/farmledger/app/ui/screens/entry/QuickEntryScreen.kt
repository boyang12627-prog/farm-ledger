package com.farmledger.app.ui.screens.entry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmledger.app.R
import com.farmledger.app.domain.model.DefaultAccounts
import com.farmledger.app.domain.model.EntryType
import com.farmledger.app.domain.model.LedgerAccount
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.domain.usecase.EntryPrefillLogic
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.theme.FarmBg
import com.farmledger.app.ui.theme.FarmSelected
import com.farmledger.app.ui.theme.FarmSoil
import com.farmledger.app.ui.theme.FarmStroke
import com.farmledger.app.ui.theme.FarmText

private val Parchment = Color(0xFFF5E6C8)
private val WoodBtn = Color(0xFF8B6914)
private val WoodBtnSel = Color(0xFF6B4A2E)

/**
 * 入帳＝木夾板流（對齊 ref_user page-05）：
 * 類型 → HK$ 金額 → 分類格 → 帳戶 pill →「記入牧場」
 * 轉帳：由帳戶→到帳戶＋金額（隱藏分類）。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickEntryScreen(
    vm: AppViewModel,
    onSaved: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val accounts by vm.accounts.collectAsState()
    val pending by vm.pendingEntryCategory.collectAsState()

    var type by remember { mutableStateOf(EntryType.EXPENSE) }
    var category by remember { mutableStateOf(LedgerCategory.FOOD) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showNote by remember { mutableStateOf(false) }
    var accountId by remember { mutableStateOf(DefaultAccounts.CASH_ID) }
    var transferToId by remember { mutableStateOf<String?>(null) }
    var showAddAccount by remember { mutableStateOf(false) }
    var newAccountName by remember { mutableStateOf("") }

    LaunchedEffect(pending) {
        val c = pending ?: return@LaunchedEffect
        vm.consumePendingEntryCategory()
        category = c
        type = EntryPrefillLogic.entryTypeForCategory(c)
        if (c == LedgerCategory.SAVINGS) {
            // 撲滿：現金 → 儲蓄戶轉帳（唔當收入）
            accountId = DefaultAccounts.CASH_ID
            transferToId = DefaultAccounts.SAVINGS_ID
        }
    }

    LaunchedEffect(accounts) {
        if (accounts.none { it.id == accountId }) {
            accountId = accounts.firstOrNull()?.id ?: DefaultAccounts.CASH_ID
        }
        if (transferToId != null && accounts.none { it.id == transferToId }) {
            transferToId = null
        }
    }

    val activeAccounts = accounts.filter { !it.archived }

    Column(
        Modifier
            .fillMaxSize()
            .background(FarmBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // A2 木夾板：entry_clipboard 做背景，保留 Compose 入帳功能
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, WoodBtnSel, RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.entry_clipboard),
                contentDescription = "木夾板",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Parchment.copy(alpha = 0.92f))
                    .border(1.5.dp, FarmStroke, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "今日入帳",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = FarmText
                )
                Text("真港幣 · 獎勵唔跟金額", style = MaterialTheme.typography.bodySmall, color = FarmSoil)
                Spacer(Modifier.height(12.dp))

                // 1. 類型
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(
                        EntryType.EXPENSE to "支出",
                        EntryType.INCOME to "收入",
                        EntryType.TRANSFER to "轉帳"
                    ).forEach { (t, label) ->
                        val sel = type == t
                        Box(
                            Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (sel) WoodBtnSel else WoodBtn.copy(alpha = 0.55f))
                                .clickable {
                                    type = t
                                    if (t == EntryType.INCOME && category !in listOf(
                                            LedgerCategory.INCOME, LedgerCategory.OTHER
                                        )
                                    ) {
                                        category = LedgerCategory.INCOME
                                    }
                                    if (t == EntryType.EXPENSE && category == LedgerCategory.INCOME) {
                                        category = LedgerCategory.FOOD
                                    }
                                    if (t == EntryType.TRANSFER && transferToId == null) {
                                        transferToId = DefaultAccounts.SAVINGS_ID
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 2. 金額大字 HK$
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("金額") },
                    prefix = {
                        Text(
                            "HK$ ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = FarmText
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = FarmText,
                        textAlign = TextAlign.Start
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("0.00", fontSize = 22.sp) }
                )

                if (type != EntryType.TRANSFER) {
                    Spacer(Modifier.height(12.dp))
                    Text("分類・${category.farmObjectZh}", color = FarmText, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    // 3. 分類格
                    val chips = if (type == EntryType.INCOME) {
                        listOf(LedgerCategory.INCOME, LedgerCategory.OTHER)
                    } else {
                        LedgerCategory.expenseChips
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chips.forEach { c ->
                            val sel = category == c
                            Box(
                                Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (sel) FarmSelected else Color.White.copy(alpha = 0.7f))
                                    .border(
                                        1.5.dp,
                                        if (sel) WoodBtnSel else FarmStroke,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { category = c },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(categoryEmoji(c), fontSize = 20.sp)
                                    Text(
                                        c.nameZh,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FarmText,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text("🐾", modifier = Modifier.align(Alignment.CenterHorizontally))

                if (type == EntryType.TRANSFER) {
                    Spacer(Modifier.height(8.dp))
                    Text("由帳戶", color = FarmText, style = MaterialTheme.typography.labelLarge)
                    AccountPillRow(
                        accounts = activeAccounts,
                        selectedId = accountId,
                        onSelect = { accountId = it },
                        onAdd = { showAddAccount = true }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("到帳戶", color = FarmText, style = MaterialTheme.typography.labelLarge)
                    AccountPillRow(
                        accounts = activeAccounts.filter { it.id != accountId },
                        selectedId = transferToId,
                        onSelect = { transferToId = it },
                        onAdd = { showAddAccount = true },
                        allowEmptyHint = "揀對方帳戶"
                    )
                } else {
                    Spacer(Modifier.height(8.dp))
                    // 4. 帳戶 pill
                    Text("帳戶", color = FarmText, style = MaterialTheme.typography.labelLarge)
                    AccountPillRow(
                        accounts = activeAccounts,
                        selectedId = accountId,
                        onSelect = { accountId = it },
                        onAdd = { showAddAccount = true }
                    )
                }

                if (showNote) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("備註（可空）") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    TextButton(onClick = { showNote = true }) { Text("＋備註") }
                }

                Spacer(Modifier.height(12.dp))
                // 5. 記入牧場
                val canSave = when (type) {
                    EntryType.TRANSFER ->
                        (amountText.toDoubleOrNull() ?: 0.0) > 0.0 &&
                            transferToId != null &&
                            transferToId != accountId
                    else -> (amountText.toDoubleOrNull() ?: 0.0) > 0.0
                }
                Button(
                    onClick = {
                        val major = amountText.toDoubleOrNull() ?: 0.0
                        val minor = (major * 100).toLong()
                        when (type) {
                            EntryType.TRANSFER -> {
                                vm.addEntry(
                                    type = EntryType.TRANSFER,
                                    amountMinor = minor,
                                    note = note,
                                    category = null,
                                    accountId = accountId,
                                    transferAccountId = transferToId
                                )
                            }
                            else -> {
                                vm.addEntry(
                                    type = type,
                                    amountMinor = minor,
                                    note = note,
                                    category = category.name,
                                    accountId = accountId
                                )
                            }
                        }
                        amountText = ""
                        note = ""
                        showNote = false
                        onSaved()
                    },
                    enabled = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WoodBtnSel,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🌿 記入牧場 🌿", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Text(
                    "目標約 8 秒入一筆 · 種子幣獎勵唔跟金額",
                    style = MaterialTheme.typography.labelSmall,
                    color = FarmSoil,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onOpenSettings) { Text("設定／帳戶管理／匯出") }
    }

    if (showAddAccount) {
        AlertDialog(
            onDismissRequest = { showAddAccount = false },
            title = { Text("新增帳戶") },
            text = {
                OutlinedTextField(
                    value = newAccountName,
                    onValueChange = { newAccountName = it },
                    label = { Text("名稱（例：PayMe／信用卡／銀行）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newAccountName.isNotBlank()) {
                        vm.addAccount(newAccountName)
                        newAccountName = ""
                        showAddAccount = false
                    }
                }) { Text("新增") }
            },
            dismissButton = {
                TextButton(onClick = { showAddAccount = false }) { Text("取消") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountPillRow(
    accounts: List<LedgerAccount>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit,
    allowEmptyHint: String? = null
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (accounts.isEmpty() && allowEmptyHint != null) {
            Text(allowEmptyHint, color = FarmSoil, style = MaterialTheme.typography.bodySmall)
        }
        accounts.forEach { a ->
            val sel = a.id == selectedId
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (sel) FarmSelected else Color.White.copy(alpha = 0.75f))
                    .border(1.dp, if (sel) WoodBtnSel else FarmStroke, RoundedCornerShape(20.dp))
                    .clickable { onSelect(a.id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    a.nameZh,
                    color = FarmText,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.5f))
                .border(1.dp, FarmStroke, RoundedCornerShape(20.dp))
                .clickable(onClick = onAdd)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text("＋新增", color = FarmSoil, style = MaterialTheme.typography.labelLarge)
        }
    }
}

fun categoryEmoji(c: LedgerCategory): String = when (c) {
    LedgerCategory.FOOD -> "🍚"
    LedgerCategory.TRANSPORT -> "🚲"
    LedgerCategory.HOUSING -> "🏠"
    LedgerCategory.DAILY -> "📦"
    LedgerCategory.ENTERTAINMENT -> "🌸"
    LedgerCategory.HEALTH -> "🌿"
    LedgerCategory.INCOME -> "📮"
    LedgerCategory.SAVINGS -> "🐷"
    LedgerCategory.OTHER -> "🪧"
}

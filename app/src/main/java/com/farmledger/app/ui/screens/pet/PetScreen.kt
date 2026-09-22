package com.farmledger.app.ui.screens.pet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.farmledger.app.R
import com.farmledger.app.domain.model.InventoryFeedCosts
import com.farmledger.app.domain.usecase.FarmStageLogic
import com.farmledger.app.domain.usecase.InventoryLogic
import com.farmledger.app.ui.AppViewModel

@Composable
fun PetScreen(vm: AppViewModel) {
    val pet by vm.pet.collectAsState()
    val progress by vm.progress.collectAsState()
    val inventory by vm.inventory.collectAsState()
    val message by vm.message.collectAsState()
    val petFeedbackUntilMs by vm.petFeedbackUntilMs.collectAsState()
    var name by remember(pet.name) { mutableStateOf(pet.name) }
    val caps = remember(progress.totalSettleDays) {
        FarmStageLogic.capabilities(progress.totalSettleDays)
    }
    val feedHave = InventoryLogic.feedQty(inventory)
    val showingEat = System.currentTimeMillis() < petFeedbackUntilMs

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("寵物", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(
                    when {
                        showingEat -> R.drawable.pet_eat
                        pet.affection >= 50 -> R.drawable.pet_happy
                        else -> R.drawable.pet_idle
                    }
                ),
                contentDescription = pet.name,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.FillBounds
            )
            Spacer(Modifier.size(12.dp))
            Column {
                Text(pet.name, style = MaterialTheme.typography.titleLarge)
                Text("飽腹：${100 - pet.hunger}/100　飢餓值：${pet.hunger}")
                Text("親密度：${pet.affection}/100")
                Text("飼料：${feedHave} 袋", style = MaterialTheme.typography.bodySmall)
            }
            if (showingEat) {
                Image(
                    painterResource(R.drawable.fx_eat),
                    contentDescription = "開心進食",
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.FillBounds
                )
            } else if (pet.affection >= 50) {
                Image(
                    painterResource(R.drawable.fx_heart),
                    contentDescription = "親密度",
                    modifier = Modifier.size(28.dp),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("改名") },
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = { vm.renamePet(name) }) { Text("儲存名字") }
        Button(
            onClick = { vm.feedPet() },
            enabled = caps.petFeedSlots > 0 && feedHave >= InventoryFeedCosts.FEED_PER_MEAL,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painterResource(R.drawable.item_feed),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                contentScale = ContentScale.FillBounds
            )
            Spacer(Modifier.size(6.dp))
            Text(
                when {
                    caps.petFeedSlots <= 0 -> "餵食（萌芽解鎖）"
                    else -> "餵食（消耗飼料 ${InventoryFeedCosts.FEED_PER_MEAL}）"
                }
            )
        }
        Button(onClick = { vm.interactPet() }, modifier = Modifier.fillMaxWidth()) { Text("互動／撫摸") }
        message?.let {
            Spacer(Modifier.height(8.dp))
            Text(it)
            TextButton(onClick = { vm.consumeMessage() }) { Text("清除") }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "餵食改用背包飼料（商店可買＝支出入帳，唔發成長點）。連續結算 3／5／7 日可解鎖更多互動與裝飾。",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

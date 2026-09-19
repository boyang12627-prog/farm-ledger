package com.farmledger.app.ui.screens.pet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.farmledger.app.ui.AppViewModel

@Composable
fun PetScreen(vm: AppViewModel) {
    val pet by vm.pet.collectAsState()
    val message by vm.message.collectAsState()
    var name by remember(pet.name) { mutableStateOf(pet.name) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("寵物", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("🐾 ${pet.name}", style = MaterialTheme.typography.titleLarge)
        Text("飽腹：${100 - pet.hunger}/100（數字越低越餓）　顯示飢餓值：${pet.hunger}")
        Text("親密度：${pet.affection}/100")
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("改名") },
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = { vm.renamePet(name) }) { Text("儲存名字") }
        Button(onClick = { vm.feedPet() }, modifier = Modifier.fillMaxWidth()) { Text("餵食") }
        Button(onClick = { vm.interactPet() }, modifier = Modifier.fillMaxWidth()) { Text("互動／撫摸") }
        message?.let {
            Spacer(Modifier.height(8.dp))
            Text(it)
            TextButton(onClick = { vm.consumeMessage() }) { Text("清除") }
        }
        Spacer(Modifier.height(16.dp))
        Text("連續結算 3／5／7 日可解鎖更多互動與裝飾（見家居）。", style = MaterialTheme.typography.bodySmall)
    }
}

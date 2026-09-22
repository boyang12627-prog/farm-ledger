package com.farmledger.app.ui.screens.decor

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.farmledger.app.R
import com.farmledger.app.ui.AppViewModel

@Composable
fun DecorScreen(vm: AppViewModel, onBack: () -> Unit) {
    val decors by vm.decorations.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("家居裝飾", style = MaterialTheme.typography.headlineMedium)
        Text("暖色像素風。連續結算可解鎖更多；佈置耗成長點、唔發獎。", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items(decors, key = { it.id }) { d ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painterResource(decorIconRes(d.id)),
                            contentDescription = d.nameZh,
                            modifier = Modifier.size(36.dp),
                            contentScale = ContentScale.FillBounds
                        )
                        Spacer(Modifier.size(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(d.nameZh)
                            Text(if (d.unlocked) "已解鎖" else "未解鎖", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = d.placed,
                            onCheckedChange = { if (d.unlocked) vm.toggleDecor(d.id) },
                            enabled = d.unlocked
                        )
                    }
                }
            }
        }
        TextButton(onClick = onBack) { Text("返回") }
    }
}

private fun decorIconRes(id: String): Int = when (id) {
    "lantern" -> R.drawable.decor_lamp
    "flowerbed" -> R.drawable.decor_flowerbox
    "scarecrow" -> R.drawable.decor_scarecrow
    else -> R.drawable.decor_sign
}

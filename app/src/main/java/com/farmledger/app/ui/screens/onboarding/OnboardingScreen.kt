package com.farmledger.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.farmledger.app.ui.theme.WarmCream

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(WarmCream)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🌾 農帳養成", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "每日記帳（收入／支出／無交易日），結算可獲固定 1 成長點。\n" +
                "金額唔影響獎勵。種田、養寵物、佈置小屋，全部離線進行。\n" +
                "資料只存本機，無廣告、無登入、無網絡權限。",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onDone) { Text("開始耕作") }
    }
}

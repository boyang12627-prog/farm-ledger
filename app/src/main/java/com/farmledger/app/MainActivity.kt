package com.farmledger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.AppViewModelFactory
import com.farmledger.app.ui.navigation.Routes
import com.farmledger.app.ui.screens.diary.DiaryScreen
import com.farmledger.app.ui.screens.entry.QuickEntryScreen
import com.farmledger.app.ui.screens.farm.FarmScreen
import com.farmledger.app.ui.screens.ledger.LedgerScreen
import com.farmledger.app.ui.screens.onboarding.OnboardingScreen
import com.farmledger.app.ui.screens.settings.SettingsScreen
import com.farmledger.app.ui.screens.weekly.WeeklyReviewScreen
import com.farmledger.app.ui.theme.FarmGrowth
import com.farmledger.app.ui.theme.FarmLedgerTheme
import com.farmledger.app.ui.theme.FarmText

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as FarmLedgerApp
        setContent {
            FarmLedgerTheme {
                val vm: AppViewModel = viewModel(factory = AppViewModelFactory(app.container.repository))
                FarmLedgerNav(vm)
            }
        }
    }
}

@Composable
fun FarmLedgerNav(vm: AppViewModel) {
    val nav = rememberNavController()
    val progress by vm.progress.collectAsState()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: ""

    LaunchedEffect(Unit) { vm.syncClock() }

    val showBottom = progress.onboardingDone &&
        route !in listOf(Routes.ONBOARDING) &&
        route != Routes.WEEKLY &&
        route != Routes.SETTINGS

    val start = if (progress.onboardingDone) Routes.FARM else Routes.ONBOARDING

    fun goTab(target: String) {
        nav.navigate(target) {
            launchSingleTop = true
            popUpTo(Routes.FARM) { inclusive = false }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottom) {
                // 四 Tab + 中央「＋」預留位（概念冊 page-01／page-05）
                Box(Modifier.fillMaxWidth()) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = route == Routes.FARM,
                            onClick = { goTab(Routes.FARM) },
                            icon = {
                                Icon(
                                    painterResource(R.drawable.nav_ranch),
                                    contentDescription = "牧場",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text("牧場") }
                        )
                        NavigationBarItem(
                            selected = route == Routes.ENTRY,
                            onClick = { goTab(Routes.ENTRY) },
                            icon = {
                                Icon(
                                    painterResource(R.drawable.nav_entry),
                                    contentDescription = "入帳",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text("入帳") }
                        )
                        // 中央預留：空位唔搶焦點，上面疊細 FAB
                        NavigationBarItem(
                            selected = false,
                            onClick = { goTab(Routes.ENTRY) },
                            icon = { Box(Modifier.size(24.dp)) },
                            label = { Text(" ") },
                            enabled = true
                        )
                        NavigationBarItem(
                            selected = route == Routes.LEDGER,
                            onClick = { goTab(Routes.LEDGER) },
                            icon = {
                                Icon(
                                    painterResource(R.drawable.nav_ledger),
                                    contentDescription = "帳簿",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text("帳簿") }
                        )
                        NavigationBarItem(
                            selected = route == Routes.DIARY,
                            onClick = { goTab(Routes.DIARY) },
                            icon = {
                                Icon(
                                    painterResource(R.drawable.nav_diary),
                                    contentDescription = "日記",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text("日記") }
                        )
                    }
                    FloatingActionButton(
                        onClick = { goTab(Routes.ENTRY) },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                            .size(48.dp),
                        shape = CircleShape,
                        containerColor = FarmGrowth,
                        contentColor = FarmText
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "快速入帳")
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = start,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(onDone = {
                    vm.completeOnboarding()
                    nav.navigate(Routes.FARM) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                })
            }
            composable(Routes.FARM) {
                FarmScreen(
                    vm = vm,
                    onOpenWeekly = { nav.navigate(Routes.WEEKLY) },
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                    onOpenEntry = { cat: LedgerCategory? ->
                        if (cat != null) vm.prefillEntryCategory(cat)
                        goTab(Routes.ENTRY)
                    }
                )
            }
            composable(Routes.ENTRY) {
                QuickEntryScreen(
                    vm = vm,
                    onSaved = { /* stay for more entries */ },
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.LEDGER) {
                LedgerScreen(
                    vm = vm,
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.DIARY) {
                DiaryScreen(
                    vm = vm,
                    onOpenWeekly = { nav.navigate(Routes.WEEKLY) },
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.WEEKLY) {
                WeeklyReviewScreen(vm = vm, onBack = { nav.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(vm = vm)
            }
        }
    }
}

package com.farmledger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.farmledger.app.ui.navigation.Routes
import com.farmledger.app.ui.screens.decor.DecorScreen
import com.farmledger.app.ui.screens.farm.FarmScreen
import com.farmledger.app.ui.screens.home.HomeScreen
import com.farmledger.app.ui.screens.ledger.EntryEditScreen
import com.farmledger.app.ui.screens.ledger.LedgerScreen
import com.farmledger.app.ui.screens.onboarding.OnboardingScreen
import com.farmledger.app.ui.screens.pet.PetScreen
import com.farmledger.app.ui.screens.settings.SettingsScreen
import com.farmledger.app.ui.screens.settle.SettleScreen
import com.farmledger.app.ui.screens.weekly.WeeklyReviewScreen
import com.farmledger.app.ui.theme.FarmLedgerTheme
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.AppViewModelFactory

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

    val showBottom = progress.onboardingDone && route !in listOf(Routes.ONBOARDING) &&
        !route.startsWith("entry_edit")

    val start = if (progress.onboardingDone) Routes.HOME else Routes.ONBOARDING

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottom) {
                NavigationBar {
                    NavigationBarItem(
                        selected = route == Routes.HOME,
                        onClick = { nav.navigate(Routes.HOME) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("主頁") }
                    )
                    NavigationBarItem(
                        selected = route == Routes.LEDGER || route.startsWith("entry_edit"),
                        onClick = { nav.navigate(Routes.LEDGER) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.MenuBook, null) },
                        label = { Text("帳簿") }
                    )
                    NavigationBarItem(
                        selected = route == Routes.FARM,
                        onClick = { nav.navigate(Routes.FARM) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Agriculture, null) },
                        label = { Text("農田") }
                    )
                    NavigationBarItem(
                        selected = route == Routes.PET,
                        onClick = { nav.navigate(Routes.PET) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Pets, null) },
                        label = { Text("寵物") }
                    )
                    NavigationBarItem(
                        selected = route == Routes.SETTINGS,
                        onClick = { nav.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text("設定") }
                    )
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
                    nav.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                })
            }
            composable(Routes.HOME) {
                HomeScreen(
                    vm = vm,
                    onLedger = { nav.navigate(Routes.LEDGER) },
                    onSettle = { nav.navigate(Routes.SETTLE) },
                    onFarm = { nav.navigate(Routes.FARM) },
                    onPet = { nav.navigate(Routes.PET) },
                    onDecor = { nav.navigate(Routes.DECOR) },
                    onWeekly = { nav.navigate(Routes.WEEKLY) },
                    onSettings = { nav.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.LEDGER) {
                LedgerScreen(
                    vm = vm,
                    onAdd = { nav.navigate(Routes.entryEdit(null)) },
                    onEdit = { id -> nav.navigate(Routes.entryEdit(id)) },
                    onSettle = { nav.navigate(Routes.SETTLE) }
                )
            }
            composable(
                route = "entry_edit?id={id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType; defaultValue = "" })
            ) { entry ->
                val id = entry.arguments?.getString("id").orEmpty().ifBlank { null }
                EntryEditScreen(
                    vm = vm,
                    entryId = id,
                    onDone = { nav.popBackStack() }
                )
            }
            composable(Routes.SETTLE) {
                SettleScreen(vm = vm, onBack = { nav.popBackStack() })
            }
            composable(Routes.FARM) {
                FarmScreen(vm = vm)
            }
            composable(Routes.PET) {
                PetScreen(vm = vm)
            }
            composable(Routes.DECOR) {
                DecorScreen(vm = vm, onBack = { nav.popBackStack() })
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

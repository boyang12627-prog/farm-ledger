package com.farmledger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.farmledger.app.domain.model.LedgerCategory
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.AppViewModelFactory
import com.farmledger.app.ui.components.RanchBottomBar
import com.farmledger.app.ui.components.RanchTab
import com.farmledger.app.ui.navigation.Routes
import com.farmledger.app.ui.screens.diary.DiaryScreen
import com.farmledger.app.ui.screens.entry.QuickEntryScreen
import com.farmledger.app.ui.screens.farm.FarmScreen
import com.farmledger.app.ui.screens.ledger.LedgerScreen
import com.farmledger.app.ui.screens.onboarding.OnboardingScreen
import com.farmledger.app.ui.screens.settings.SettingsScreen
import com.farmledger.app.ui.screens.weekly.WeeklyReviewScreen
import com.farmledger.app.ui.theme.FarmLedgerTheme

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

    val selectedTab = when (route) {
        Routes.FARM -> RanchTab.RANCH
        Routes.ENTRY -> RanchTab.ENTRY
        Routes.LEDGER -> RanchTab.LEDGER
        Routes.DIARY -> RanchTab.DIARY
        else -> RanchTab.RANCH
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottom) {
                RanchBottomBar(
                    selected = selectedTab,
                    onSelect = { tab ->
                        when (tab) {
                            RanchTab.RANCH -> goTab(Routes.FARM)
                            RanchTab.ENTRY -> goTab(Routes.ENTRY)
                            RanchTab.LEDGER -> goTab(Routes.LEDGER)
                            RanchTab.DIARY -> goTab(Routes.DIARY)
                        }
                    },
                    onCenterFab = { goTab(Routes.ENTRY) }
                )
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
                    onSaved = {
                        // 夾板流：確認後收板回牧場（≤3tap≤8s）
                        goTab(Routes.FARM)
                    },
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

package com.farmledger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
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
import com.farmledger.app.ui.AppViewModel
import com.farmledger.app.ui.AppViewModelFactory
import com.farmledger.app.ui.navigation.Routes
import com.farmledger.app.ui.screens.farm.FarmScreen
import com.farmledger.app.ui.screens.ledger.EntryEditScreen
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
        !route.startsWith("entry_edit") &&
        route != Routes.WEEKLY

    val start = if (progress.onboardingDone) Routes.FARM else Routes.ONBOARDING

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottom) {
                NavigationBar {
                    NavigationBarItem(
                        selected = route == Routes.FARM,
                        onClick = {
                            nav.navigate(Routes.FARM) {
                                launchSingleTop = true
                                popUpTo(Routes.FARM) { inclusive = false }
                            }
                        },
                        icon = { Icon(Icons.Default.Agriculture, contentDescription = null) },
                        label = { Text("農場") }
                    )
                    NavigationBarItem(
                        selected = route == Routes.SETTINGS,
                        onClick = { nav.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
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
                    nav.navigate(Routes.FARM) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                })
            }
            composable(Routes.FARM) {
                FarmScreen(
                    vm = vm,
                    onOpenWeekly = { nav.navigate(Routes.WEEKLY) },
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                    onEditEntry = { id -> nav.navigate(Routes.entryEdit(id)) },
                    onAddEntry = { nav.navigate(Routes.entryEdit(null)) }
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
            composable(Routes.WEEKLY) {
                WeeklyReviewScreen(vm = vm, onBack = { nav.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(vm = vm)
            }
        }
    }
}

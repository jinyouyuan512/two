package com.example.healthapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import com.example.healthapp.ui.components.HealthBottomBar
import com.example.healthapp.ui.components.BottomItem
import com.example.healthapp.ui.components.FloatingAiButton
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.healthapp.ui.screens.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthapp.viewmodel.AuthViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun BottomNavBar(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    LaunchedEffect(Unit) { authViewModel.restoreSession(context) }

    val hideBars = currentRoute?.startsWith("auth/") == true || currentRoute == "import"
    Scaffold(
        floatingActionButton = {
            FloatingAiButton(visible = !hideBars && currentRoute != "ai") {
                navController.navigate("ai") {
                    navController.graph.startDestinationRoute?.let { r ->
                        popUpTo(r) { saveState = true }
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            if (!hideBars) {
                val items = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Data,
                    BottomNavItem.Health,
                    BottomNavItem.Profile
                )
                HealthBottomBar(
                    items = items.map { BottomItem(it.route, it.title, it.icon) },
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        navController.navigate(route) {
                            navController.graph.startDestinationRoute?.let { r ->
                                popUpTo(r) { saveState = true }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        if (authViewModel.isRestoring) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            return@Scaffold
        }
        Box(modifier = Modifier.padding(innerPadding)) {
            val start = if (authViewModel.isLoggedIn) BottomNavItem.Home.route else "auth/login"
            NavHost(navController = navController, startDestination = start) {
                composable("auth/login") { LoginScreen(navController) }
                composable("auth/register") { RegisterScreen(navController) }
                composable(BottomNavItem.Home.route) {
                    HomeScreen(onAiClick = {
                        navController.navigate("ai") {
                            navController.graph.startDestinationRoute?.let { r ->
                                popUpTo(r) { saveState = true }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }, onRecordClick = {
                        navController.navigate("import")
                    }, onNewsClick = {
                        navController.navigate("news/list")
                    })
                }
                composable(BottomNavItem.Data.route) { DataScreen(navController) }
                composable(BottomNavItem.Health.route) { HealthHubScreen(navController) }
                composable("import") { ImportDataScreen(navController) }
                composable("ai") { AIHealthAssistantScreen(navController) }
                composable(BottomNavItem.Profile.route) { ProfileScreen(navController) }
                composable("news/list") { HealthNewsListScreen(navController) }
            }
        }
    }
}

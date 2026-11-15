package com.gmail.nthai.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gmail.nthai.presentation.screens.AutoSmsScreen
import com.gmail.nthai.presentation.screens.CustomerListScreen
import com.gmail.nthai.presentation.screens.HomeScreen
import com.gmail.nthai.presentation.screens.ManagementScreen
import com.gmail.nthai.presentation.screens.ReportScreen
import com.gmail.nthai.presentation.screens.SendingProgressScreen
import com.gmail.nthai.presentation.screens.ZaloScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val screens = listOf(
        Screen.Home,
        Screen.AutoSms,
        Screen.AutoZalo,
        Screen.Management
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            NavigationBar(
                containerColor = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                contentColor = androidx.compose.ui.graphics.Color(0xFF888888),
                tonalElevation = 0.dp,
                modifier = Modifier.padding(top = 1.dp)
            ) {
                screens.forEach { screen ->
                    val isSelected = when (screen) {
                        Screen.Home -> currentRoute == Screen.Home.route
                        Screen.AutoSms -> currentRoute == Screen.AutoSms.route || currentRoute?.startsWith(Screen.Customers.BASE_ROUTE) == true
                        Screen.AutoZalo -> currentRoute == Screen.AutoZalo.route
                        Screen.Management -> currentRoute == Screen.Management.route
                        else -> currentRoute == screen.route
                    }

                    val targetRoute = screen.route

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (screen == Screen.Home) {
                                if (currentRoute != Screen.Home.route &&
                                    !navController.popBackStack(Screen.Home.route, inclusive = false)
                                ) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            } else if (!isSelected) {
                                navController.navigate(targetRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = androidx.compose.ui.graphics.Color(0xFF171717),
                            selectedTextColor = androidx.compose.ui.graphics.Color(0xFF171717),
                            indicatorColor = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                            unselectedIconColor = androidx.compose.ui.graphics.Color(0xFF888888),
                            unselectedTextColor = androidx.compose.ui.graphics.Color(0xFF888888)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onSelectSim = { slot -> navController.navigate(Screen.Customers.route(slot)) },
                    onNavigateToCustomers = { navController.navigate(Screen.AutoSms.route) },
                    onNavigateToProgress = { navController.navigate(Screen.Progress.route) },
                    onNavigateToManagement = { _ -> navController.navigate(Screen.Management.route) },
                    onNavigateToAutoZalo = { navController.navigate(Screen.AutoZalo.route) }
                )
            }
            composable(
                route = Screen.Customers.route,
                arguments = listOf(
                    navArgument("simSlot") {
                        type = NavType.IntType
                        defaultValue = 1
                    }
                )
            ) { entry ->
                CustomerListScreen(
                    simSlot = entry.arguments?.getInt("simSlot") ?: 1,
                    onBack = { navController.popBackStack() },
                    onStartSending = { navController.navigate(Screen.Progress.route) }
                )
            }
            composable(Screen.Progress.route) {
                SendingProgressScreen()
            }
            composable(Screen.Management.route) {
                ManagementScreen(
                    onNavigateToAutoZalo = { navController.navigate(Screen.AutoZalo.route) }
                )
            }

            composable(Screen.AutoSms.route) {
                AutoSmsScreen()
            }
            composable(Screen.AutoZalo.route) {
                ZaloScreen(
                    showTopBarBackIcon = false,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Report.route) {
                ReportScreen()
            }
        }
    }
}

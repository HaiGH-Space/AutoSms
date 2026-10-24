package com.gmail.nthai.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gmail.nthai.presentation.screens.CustomerListScreen
import com.gmail.nthai.presentation.screens.ReportScreen
import com.gmail.nthai.presentation.screens.SendingProgressScreen
import com.gmail.nthai.presentation.screens.SimAndRuleConfigScreen
import com.gmail.nthai.presentation.screens.TemplateEditorScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val screens = listOf(
        Screen.Customers,
        Screen.Template,
        Screen.Config,
        Screen.Progress,
        Screen.Report
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                tonalElevation = 0.dp
            ) {
                screens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
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
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Customers.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Customers.route) {
                CustomerListScreen()
            }
            composable(Screen.Template.route) {
                TemplateEditorScreen()
            }
            composable(Screen.Config.route) {
                SimAndRuleConfigScreen()
            }
            composable(Screen.Progress.route) {
                SendingProgressScreen()
            }
            composable(Screen.Report.route) {
                ReportScreen()
            }
        }
    }
}

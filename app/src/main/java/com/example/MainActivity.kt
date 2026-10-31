package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.*
import com.example.ui.theme.QuickBillTheme

class MainActivity : ComponentActivity() {
    private val viewModel: PosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuickBillTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_navigation")
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == "pos",
                                onClick = { viewModel.setScreen("pos") },
                                icon = { Icon(Icons.Default.PointOfSale, contentDescription = "POS") },
                                label = { Text("POS") },
                                modifier = Modifier.testTag("nav_pos")
                            )
                            NavigationBarItem(
                                selected = currentScreen == "dashboard",
                                onClick = { viewModel.setScreen("dashboard") },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Dashboard") },
                                modifier = Modifier.testTag("nav_dashboard")
                            )
                            NavigationBarItem(
                                selected = currentScreen == "history",
                                onClick = { viewModel.setScreen("history") },
                                icon = { Icon(Icons.Default.History, contentDescription = "History") },
                                label = { Text("History") },
                                modifier = Modifier.testTag("nav_history")
                            )
                            NavigationBarItem(
                                selected = currentScreen == "menu",
                                onClick = { viewModel.setScreen("menu") },
                                icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu") },
                                label = { Text("Menu") },
                                modifier = Modifier.testTag("nav_menu")
                            )
                            NavigationBarItem(
                                selected = currentScreen == "settings",
                                onClick = { viewModel.setScreen("settings") },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings") },
                                modifier = Modifier.testTag("nav_settings")
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentScreen) {
                            "pos" -> PosScreen(viewModel)
                            "dashboard" -> DashboardScreen(viewModel)
                            "history" -> HistoryScreen(viewModel)
                            "menu" -> MenuScreen(viewModel)
                            "settings" -> SettingsScreen(viewModel)
                            else -> PosScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}

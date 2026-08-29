package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.AppNavHost
import com.example.ui.navigation.Screen
import com.example.ui.theme.MealyTheme
import com.example.ui.theme.SageContainer
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.viewmodel.KitchenViewModel
import com.example.ui.viewmodel.MainTab

class MainActivity : ComponentActivity() {

    private val viewModel: KitchenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MealyTheme {
                MealyApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MealyApp(viewModel: KitchenViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route ?: Screen.Home.route
    val activeCookingRecipe by viewModel.activeCookingRecipe.collectAsState()

    // Sync ViewModel tab state with Jetpack Navigation
    LaunchedEffect(viewModel.currentTab) {
        viewModel.currentTab.collect { tab ->
            val targetRoute = when (tab) {
                MainTab.HOME -> Screen.Home.route
                MainTab.SCAN -> Screen.Scan.route
                MainTab.INVENTORY -> Screen.Inventory.route
                MainTab.RECIPES -> Screen.Recipes.route
                MainTab.COOKING -> Screen.Cooking.route
                MainTab.ASK_MEALY -> Screen.AskMealy.route
                MainTab.PROFILE -> Screen.Profile.route
            }
            if (currentDestination != targetRoute) {
                navController.navigate(targetRoute) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                NavigationItem(
                    selected = currentDestination == Screen.Home.route,
                    onClick = { viewModel.selectTab(MainTab.HOME) },
                    icon = Icons.Default.Home,
                    label = "Home",
                    testTag = "nav_home"
                )
                NavigationItem(
                    selected = currentDestination == Screen.Scan.route,
                    onClick = { viewModel.selectTab(MainTab.SCAN) },
                    icon = Icons.Default.PhotoCamera,
                    label = "Vision",
                    testTag = "nav_scan"
                )
                NavigationItem(
                    selected = currentDestination == Screen.Inventory.route,
                    onClick = { viewModel.selectTab(MainTab.INVENTORY) },
                    icon = Icons.Default.Kitchen,
                    label = "Pantry",
                    testTag = "nav_inventory"
                )
                NavigationItem(
                    selected = currentDestination == Screen.Recipes.route,
                    onClick = { viewModel.selectTab(MainTab.RECIPES) },
                    icon = Icons.Default.RestaurantMenu,
                    label = "Recipes",
                    testTag = "nav_recipes"
                )
                if (activeCookingRecipe != null) {
                    NavigationItem(
                        selected = currentDestination == Screen.Cooking.route,
                        onClick = { viewModel.selectTab(MainTab.COOKING) },
                        icon = Icons.Default.OutdoorGrill,
                        label = "Cooking",
                        testTag = "nav_cooking"
                    )
                }
                NavigationItem(
                    selected = currentDestination == Screen.AskMealy.route,
                    onClick = { viewModel.selectTab(MainTab.ASK_MEALY) },
                    icon = Icons.Default.AutoAwesome,
                    label = "Ask AI",
                    testTag = "nav_ask"
                )
                NavigationItem(
                    selected = currentDestination == Screen.Profile.route,
                    onClick = { viewModel.selectTab(MainTab.PROFILE) },
                    icon = Icons.Default.Psychology,
                    label = "Memory",
                    testTag = "nav_profile"
                )
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun RowScope.NavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    testTag: String
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp)
            )
        },
        label = { Text(text = label, style = MaterialTheme.typography.labelSmall) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = SageGreenPrimary,
            selectedTextColor = SageGreenPrimary,
            indicatorColor = SageContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.testTag(testTag)
    )
}

package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ui.screens.*
import com.example.ui.viewmodel.KitchenViewModel
import com.example.ui.viewmodel.MainTab

/**
 * Jetpack Navigation NavHost providing explicit entry points for:
 * - Home
 * - Scan
 * - Inventory
 * - Recipes
 * - Cooking
 * - AskMealy
 * - Profile / Memory
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: KitchenViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(viewModel = viewModel)
        }
        composable(Screen.Scan.route) {
            ScanScreen(viewModel = viewModel)
        }
        composable(Screen.Inventory.route) {
            InventoryScreen(viewModel = viewModel)
        }
        composable(Screen.Recipes.route) {
            RecipesScreen(viewModel = viewModel)
        }
        composable(Screen.Cooking.route) {
            CookingModeScreen(viewModel = viewModel)
        }
        composable(Screen.AskMealy.route) {
            AskMealyScreen(viewModel = viewModel)
        }
        composable(Screen.Profile.route) {
            ProfileAndMemoryScreen(viewModel = viewModel)
        }
        composable(Screen.Settings.route) {
            ProfileSettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.selectTab(MainTab.PROFILE) }
            )
        }
    }
}

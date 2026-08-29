package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Type-safe navigation destination definitions for Mealy AI.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    object Home : Screen("home", "Home", Icons.Default.Home, "nav_home")
    object Scan : Screen("scan", "Vision", Icons.Default.PhotoCamera, "nav_scan")
    object Inventory : Screen("inventory", "Pantry", Icons.Default.Kitchen, "nav_inventory")
    object Recipes : Screen("recipes", "Recipes", Icons.Default.RestaurantMenu, "nav_recipes")
    object Cooking : Screen("cooking", "Cooking", Icons.Default.OutdoorGrill, "nav_cooking")
    object AskMealy : Screen("ask_mealy", "Ask AI", Icons.Default.AutoAwesome, "nav_ask")
    object Profile : Screen("profile", "Memory", Icons.Default.Psychology, "nav_profile")
    object Settings : Screen("settings", "Settings", Icons.Default.Tune, "nav_settings")

    companion object {
        val bottomNavItems = listOf(
            Home,
            Scan,
            Inventory,
            Recipes,
            AskMealy,
            Profile
        )
    }
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.KitchenContext
import com.example.data.ai.LocalRuleBasedAIProvider
import com.example.data.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Mealio AI`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Mealio AI", appName)
    }

    @Test
    fun `inventory item expiry calculation`() {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        val item = InventoryItem(
            name = "Spinach",
            category = IngredientCategory.PRODUCE,
            quantity = 200.0,
            unit = "g",
            expiryTimestamp = now + (1 * dayMs)
        )
        assertTrue(item.isExpiringSoon())
    }

    @Test
    fun `rule based AI generates recommendations with ranking and adapts to preferences`() = runBlocking {
        val aiProvider = LocalRuleBasedAIProvider()
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        val context = KitchenContext(
            inventory = listOf(
                InventoryItem(name = "Chicken Breast", category = IngredientCategory.PROTEINS, quantity = 500.0, unit = "g", expiryTimestamp = now + (2 * dayMs)),
                InventoryItem(name = "Fresh Baby Spinach", category = IngredientCategory.PRODUCE, quantity = 200.0, unit = "g", expiryTimestamp = now + (1 * dayMs)),
                InventoryItem(name = "Jasmine Rice", category = IngredientCategory.PANTRY_GRAINS, quantity = 1.0, unit = "kg", expiryTimestamp = now + (180 * dayMs))
            ),
            expiringItems = listOf(
                InventoryItem(name = "Fresh Baby Spinach", category = IngredientCategory.PRODUCE, quantity = 200.0, unit = "g", expiryTimestamp = now + (1 * dayMs))
            ),
            preferences = UserPreferences(
                favoriteCuisines = listOf("Mediterranean"),
                spicePreference = "Spicy",
                dietaryGoal = "High Protein & Balanced",
                dislikedIngredients = listOf("cilantro"),
                maxCookingTimeMin = 30
            ),
            memories = listOf(
                KitchenMemoryLog(category = "TASTE", insight = "Prefers spicy flavors", actionTaken = "Adjusted spice level")
            )
        )

        val recipes = aiProvider.generateRecipeRecommendations(context)
        assertTrue(recipes.isNotEmpty())
        assertTrue(recipes.first().matchPercentage >= 70)
        assertEquals(2, recipes.first().spiceLevel)
    }

    @Test
    fun `audio permission service reports permission correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = com.example.util.AudioPermissionService(context)
        // Verify service initializes and checks permission without crashing
        service.hasAudioPermission()
    }
}

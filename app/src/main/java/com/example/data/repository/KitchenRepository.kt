package com.example.data.repository

import com.example.data.ai.AIProvider
import com.example.data.ai.GeminiAIProvider
import com.example.data.ai.KitchenContext
import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class KitchenRepository(
    private val database: AppDatabase,
    private val aiProvider: AIProvider = GeminiAIProvider()
) {
    val activeInventory: Flow<List<InventoryItem>> = database.inventoryDao().getActiveInventory()
    val allInventory: Flow<List<InventoryItem>> = database.inventoryDao().getAllInventory()
    val allRecipes: Flow<List<Recipe>> = database.recipeDao().getAllRecipes()
    val favoriteRecipes: Flow<List<Recipe>> = database.recipeDao().getFavoriteRecipes()
    val userPreferences: Flow<UserPreferences?> = database.preferencesDao().getPreferences()
    val cookingSessions: Flow<List<CookingSession>> = database.cookingSessionDao().getAllSessions()
    val kitchenMemories: Flow<List<KitchenMemoryLog>> = database.kitchenMemoryDao().getAllMemories()

    suspend fun addInventoryItem(item: InventoryItem): Long {
        return database.inventoryDao().insertItem(item)
    }

    suspend fun addInventoryItems(items: List<InventoryItem>) {
        database.inventoryDao().insertAll(items)
    }

    suspend fun updateInventoryItem(item: InventoryItem) {
        database.inventoryDao().updateItem(item)
    }

    suspend fun updateItemState(id: Long, newState: FoodState) {
        database.inventoryDao().updateState(id, newState)
    }

    suspend fun deleteInventoryItem(id: Long) {
        database.inventoryDao().deleteById(id)
    }

    suspend fun savePreferences(prefs: UserPreferences) {
        database.preferencesDao().savePreferences(prefs)
    }

    suspend fun toggleFavoriteRecipe(id: Long, isFav: Boolean) {
        database.recipeDao().toggleFavorite(id, isFav)
    }

    suspend fun completeCookingSession(session: CookingSession) {
        database.cookingSessionDao().insertSession(session)
        // Log to kitchen memory
        val memory = KitchenMemoryLog(
            category = "COMPLETED_RECIPE",
            insight = "Cooked '${session.recipeTitle}' with ${session.rating}★ rating. Note: ${session.feedbackNote}",
            actionTaken = "Mealy AI updated taste profile and prioritized similar flavor profiles"
        )
        database.kitchenMemoryDao().insertMemory(memory)
    }

    suspend fun addKitchenMemory(memory: KitchenMemoryLog) {
        database.kitchenMemoryDao().insertMemory(memory)
    }

    suspend fun buildKitchenContext(activeRecipe: Recipe? = null, activeStep: RecipeStep? = null): KitchenContext {
        val inv = database.inventoryDao().getActiveInventoryList()
        val expiring = inv.filter { it.isExpiringSoon() }
        val prefs = database.preferencesDao().getPreferencesDirect() ?: UserPreferences()
        val memories = database.kitchenMemoryDao().getAllMemories().firstOrNull() ?: emptyList()
        return KitchenContext(
            inventory = inv,
            expiringItems = expiring,
            preferences = prefs,
            memories = memories,
            activeRecipe = activeRecipe,
            activeStep = activeStep
        )
    }

    suspend fun scanFridgeVision(imageDescOrLabels: String): List<DetectedFoodItem> {
        return aiProvider.analyzeFridgeImage(imageDescOrLabels)
    }

    suspend fun getAdaptiveRecipes(query: String? = null): List<Recipe> {
        val context = buildKitchenContext()
        val recommendations = aiProvider.generateRecipeRecommendations(context, query)
        if (recommendations.isNotEmpty()) {
            database.recipeDao().insertAll(recommendations)
        }
        return recommendations
    }

    suspend fun getSubstitutionAdvice(missingIngredient: String): SubstitutionAdvice {
        val context = buildKitchenContext()
        return aiProvider.getSubstitutionAdvice(missingIngredient, context)
    }

    suspend fun chatWithChef(userMessage: String, history: List<ChatMessage>): String {
        val context = buildKitchenContext()
        return aiProvider.chatWithChef(userMessage, context, history)
    }
}

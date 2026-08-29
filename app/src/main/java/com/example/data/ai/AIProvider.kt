package com.example.data.ai

import com.example.data.model.*

data class KitchenContext(
    val inventory: List<InventoryItem>,
    val expiringItems: List<InventoryItem>,
    val preferences: UserPreferences,
    val memories: List<KitchenMemoryLog>,
    val activeRecipe: Recipe? = null,
    val activeStep: RecipeStep? = null
)

interface AIProvider {
    suspend fun analyzeFridgeImage(imageDescriptionOrLabels: String): List<DetectedFoodItem>
    suspend fun getRecipeRecommendations(ingredients: List<String>): List<Recipe>
    suspend fun generateRecipeRecommendations(
        context: KitchenContext,
        customQuery: String? = null
    ): List<Recipe>
    suspend fun getSubstitutionAdvice(
        missingIngredient: String,
        context: KitchenContext
    ): SubstitutionAdvice
    suspend fun chatWithChef(
        userMessage: String,
        context: KitchenContext,
        conversationHistory: List<ChatMessage>
    ): String
}

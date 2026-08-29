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
            actionTaken = "Mealio AI updated taste profile and prioritized similar flavor profiles"
        )
        database.kitchenMemoryDao().insertMemory(memory)
        getAdaptiveRecipes()
    }

    suspend fun addKitchenMemory(memory: KitchenMemoryLog) {
        database.kitchenMemoryDao().insertMemory(memory)
        getAdaptiveRecipes()
    }

    suspend fun generateAutonomousMealPlan(): AutonomousMealPlan {
        val context = buildKitchenContext()
        val recipes = getAdaptiveRecipes()
        val expiringNames = context.expiringItems.map { it.name }

        val breakfast = recipes.find { it.title.contains("Scramble", ignoreCase = true) || it.title.contains("Egg", ignoreCase = true) }
            ?: recipes.firstOrNull()
        val lunch = recipes.find { it.title.contains("Bowl", ignoreCase = true) || it.title.contains("Shakshuka", ignoreCase = true) }
            ?: recipes.getOrNull(1) ?: recipes.firstOrNull()
        val dinner = recipes.find { it.title.contains("Skillet", ignoreCase = true) || it.title.contains("Chicken", ignoreCase = true) }
            ?: recipes.getOrNull(2) ?: recipes.firstOrNull()

        val slots = mutableListOf<MealPlanSlot>()

        if (breakfast != null) {
            val rescued = expiringNames.filter { exp -> breakfast.ingredients.any { it.name.contains(exp, ignoreCase = true) } }
            slots.add(
                MealPlanSlot(
                    mealType = "Breakfast",
                    recipeTitle = breakfast.title,
                    recipeId = breakfast.id,
                    prepTimeMin = breakfast.totalTimeMin,
                    calories = breakfast.calories,
                    proteinG = breakfast.proteinG,
                    whyScheduled = "Autonomously planned: High protein morning kickstart under ${breakfast.totalTimeMin}m",
                    rescuedIngredients = rescued
                )
            )
        }

        if (lunch != null) {
            val rescued = expiringNames.filter { exp -> lunch.ingredients.any { it.name.contains(exp, ignoreCase = true) } }
            slots.add(
                MealPlanSlot(
                    mealType = "Lunch",
                    recipeTitle = lunch.title,
                    recipeId = lunch.id,
                    prepTimeMin = lunch.totalTimeMin,
                    calories = lunch.calories,
                    proteinG = lunch.proteinG,
                    whyScheduled = if (rescued.isNotEmpty()) "Prioritizes expiring ${rescued.joinToString()} to avoid waste" else "Balanced midday nutrient profile matching ${context.preferences.dietaryGoal}",
                    rescuedIngredients = rescued
                )
            )
        }

        if (dinner != null) {
            val rescued = expiringNames.filter { exp -> dinner.ingredients.any { it.name.contains(exp, ignoreCase = true) } }
            slots.add(
                MealPlanSlot(
                    mealType = "Dinner",
                    recipeTitle = dinner.title,
                    recipeId = dinner.id,
                    prepTimeMin = dinner.totalTimeMin,
                    calories = dinner.calories,
                    proteinG = dinner.proteinG,
                    whyScheduled = "Cooks satisfying ${dinner.cuisine} dinner with ${dinner.proteinG}g protein in ${dinner.totalTimeMin}m",
                    rescuedIngredients = rescued
                )
            )
        }

        val totalCal = slots.sumOf { it.calories }
        val totalProtein = slots.sumOf { it.proteinG }
        val totalRescued = slots.flatMap { it.rescuedIngredients }.distinct().size
        val projectedSavings = totalRescued * 3.40 + 2.50

        return AutonomousMealPlan(
            dayLabel = "Today's Autonomous Plan",
            meals = slots,
            totalCalories = totalCal,
            totalProteinG = totalProtein,
            rescuedIngredientsCount = totalRescued,
            projectedSavingsDollars = projectedSavings,
            preferenceAlignmentNote = "Autonomously tailored to your ${context.preferences.dietaryGoal} goal, ${context.preferences.spicePreference} spice tolerance & ≤${context.preferences.maxCookingTimeMin}m limit"
        )
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

    suspend fun askCookingQuestion(question: String, activeRecipe: Recipe, activeStep: RecipeStep): String {
        val context = buildKitchenContext(activeRecipe = activeRecipe, activeStep = activeStep)
        val prompt = "During cooking '${activeRecipe.title}' at Step ${activeStep.stepNumber} ('${activeStep.title}': ${activeStep.instruction}), the cook asked: \"$question\". Give a concise, clear 1-2 sentence culinary answer suitable for voice reading."
        return aiProvider.chatWithChef(prompt, context, emptyList())
    }
}

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

enum class FoodState(val value: String, val label: String) {
    BOUGHT("bought", "Bought"),
    STORED("stored", "Stored"),
    OPENED("opened", "Opened"),
    EXPIRING("expiring", "Expiring Soon"),
    USED("used", "Used"),
    DISCARDED("discarded", "Discarded");

    companion object {
        fun fromString(str: String): FoodState {
            return entries.find { it.value.equals(str, ignoreCase = true) || it.name.equals(str, ignoreCase = true) } ?: STORED
        }
    }
}

enum class IngredientCategory(val label: String, val iconName: String) {
    PRODUCE("Produce", "vegetable"),
    DAIRY_EGGS("Dairy & Eggs", "egg"),
    PROTEINS("Proteins & Meat", "meat"),
    PANTRY_GRAINS("Pantry & Grains", "grain"),
    CONDIMENTS_SPICES("Condiments & Spices", "spice"),
    BAKERY("Bakery", "bread"),
    BEVERAGES("Beverages", "drink"),
    FROZEN("Frozen", "frozen"),
    OTHER("Other", "food")
}

@Entity(tableName = "inventory_items")
@JsonClass(generateAdapter = true)
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ingredient_id: String = "",
    val name: String = "",
    val category: IngredientCategory = IngredientCategory.OTHER,
    val quantity: Double = 1.0,
    val unit: String = "pcs",
    val expiry_date: String = "",
    val state: FoodState = FoodState.STORED,
    val expiryTimestamp: Long = System.currentTimeMillis() + 86400000L * 7, // timestamp in ms
    val addedTimestamp: Long = System.currentTimeMillis(),
    val location: String = "Fridge", // Fridge, Freezer, Pantry
    val notes: String = "",
    val estimatedPriceDollars: Double = 3.50
) {
    fun daysUntilExpiry(): Int {
        val now = System.currentTimeMillis()
        val diff = expiryTimestamp - now
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun isExpiringSoon(): Boolean {
        val days = daysUntilExpiry()
        return days in 0..2 && state != FoodState.USED && state != FoodState.DISCARDED
    }

    fun isExpired(): Boolean {
        val days = daysUntilExpiry()
        return days < 0 && state != FoodState.USED && state != FoodState.DISCARDED
    }
}

@Entity(tableName = "ingredients")
@JsonClass(generateAdapter = true)
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: String = "1",
    val unit: String = "pcs",
    val category: IngredientCategory = IngredientCategory.OTHER,
    val state: FoodState = FoodState.STORED,
    val expiryTimestamp: Long = System.currentTimeMillis() + 86400000L * 7,
    val isCrucial: Boolean = true,
    val substitutionNote: String? = null
)

@JsonClass(generateAdapter = true)
data class RecipeIngredient(
    val name: String,
    val amount: String,
    val unit: String = "",
    val isAvailableInInventory: Boolean = false,
    val substitutionNote: String? = null,
    val isCrucial: Boolean = true
)

@JsonClass(generateAdapter = true)
data class RecipeStep(
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    val durationMinutes: Int = 0, // timer if > 0
    val chefTip: String? = null,
    val scienceExplanation: String? = null,
    val ingredientsNeeded: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SubstitutionAdvice(
    val originalIngredient: String,
    val substituteIngredient: String,
    val reason: String,
    val ratioNote: String
)

@Entity(tableName = "recipes")
@JsonClass(generateAdapter = true)
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val cuisine: String,
    val difficulty: String = "Easy", // Easy, Medium, Hard
    val prepTimeMin: Int,
    val cookTimeMin: Int,
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val fiberG: Int = 4,
    val spiceLevel: Int = 1, // 0 = Mild, 1 = Medium, 2 = Spicy, 3 = Extra Hot
    val matchPercentage: Int = 85,
    val whyRecommended: List<String> = emptyList(),
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<RecipeStep> = emptyList(),
    val substitutions: List<SubstitutionAdvice> = emptyList(),
    val dietaryTags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val wasteReductionScore: Int = 10 // score based on expiring ingredients used
) {
    val totalTimeMin: Int get() = prepTimeMin + cookTimeMin
}

@Entity(tableName = "user_preferences")
@JsonClass(generateAdapter = true)
data class UserPreferences(
    @PrimaryKey val id: Int = 1,
    val favoriteCuisines: List<String> = listOf("Italian", "Asian / Stir-Fry", "Mexican", "Mediterranean"),
    val spicePreference: String = "Medium", // Mild, Medium, Spicy, Extra Hot
    val dislikedIngredients: List<String> = listOf("Cilantro"),
    val allergies: List<String> = emptyList(),
    val dietaryGoal: String = "High Protein & Balanced", // High Protein, Low Carb, Keto, Balanced, Plant-Forward
    val maxCookingTimeMin: Int = 35,
    val cookingSkill: String = "Intermediate", // Beginner, Intermediate, Seasoned
    val mealHabit: String = "Quick Weekday Meals",
    val handsFreeTtsEnabled: Boolean = true,
    val autoVoiceOnStepChange: Boolean = true
)

@Entity(tableName = "cooking_sessions")
@JsonClass(generateAdapter = true)
data class CookingSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val recipeTitle: String,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long = System.currentTimeMillis(),
    val rating: Int = 5,
    val feedbackNote: String = "Delicious and easy!",
    val substitutionsUsed: List<String> = emptyList(),
    val wasteSavedDollars: Double = 4.20
)

@Entity(tableName = "kitchen_memory")
@JsonClass(generateAdapter = true)
data class KitchenMemoryLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String, // TASTE, HABIT, SUBSTITUTION, ALLERGY
    val insight: String,
    val actionTaken: String
)

@JsonClass(generateAdapter = true)
data class DetectedFoodItem(
    val name: String,
    val category: IngredientCategory,
    val suggestedQuantity: Double = 1.0,
    val unit: String = "pcs",
    val estimatedShelfLifeDays: Int = 5,
    val confidence: Float = 0.92f,
    val isSelected: Boolean = true
)

@JsonClass(generateAdapter = true)
data class MealPlanSlot(
    val id: String = java.util.UUID.randomUUID().toString(),
    val mealType: String, // "Breakfast", "Lunch", "Dinner"
    val recipeTitle: String,
    val recipeId: Long,
    val prepTimeMin: Int,
    val calories: Int,
    val proteinG: Int,
    val whyScheduled: String,
    val rescuedIngredients: List<String> = emptyList(),
    val isCompleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class AutonomousMealPlan(
    val dayLabel: String = "Today's Autonomous Plan",
    val generatedAtTimestamp: Long = System.currentTimeMillis(),
    val meals: List<MealPlanSlot> = emptyList(),
    val totalCalories: Int = 0,
    val totalProteinG: Int = 0,
    val rescuedIngredientsCount: Int = 0,
    val projectedSavingsDollars: Double = 8.50,
    val preferenceAlignmentNote: String = "Tailored to your High-Protein goals and Mild spice tolerance"
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedActions: List<String> = emptyList()
)

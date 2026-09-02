package com.example.data.ai.recipe

import com.example.BuildConfig
import com.example.data.ai.GeminiService
import com.example.data.ai.KitchenContext
import com.example.data.ai.LocalRuleBasedAIProvider
import com.example.data.model.*
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

@JsonClass(generateAdapter = true)
data class GeminiGeneratedRecipeDto(
    val title: String = "",
    val description: String = "",
    val cuisine: String = "Global",
    val difficulty: String = "Easy",
    val prepTimeMin: Int = 10,
    val cookTimeMin: Int = 15,
    val calories: Int = 400,
    val proteinG: Int = 30,
    val carbsG: Int = 35,
    val fatG: Int = 12,
    val spiceLevel: Int = 1,
    val matchPercentage: Int = 90,
    val whyRecommended: List<String> = emptyList(),
    val ingredients: List<GeminiGeneratedIngredientDto> = emptyList(),
    val steps: List<GeminiGeneratedStepDto> = emptyList(),
    val substitutions: List<GeminiGeneratedSubDto> = emptyList(),
    val dietaryTags: List<String> = emptyList(),
    val wasteReductionScore: Int = 12
)

@JsonClass(generateAdapter = true)
data class GeminiGeneratedIngredientDto(
    val name: String = "",
    val amount: String = "1",
    val unit: String = "portion",
    val isAvailableInInventory: Boolean = true,
    val substitutionNote: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGeneratedStepDto(
    val stepNumber: Int = 1,
    val title: String = "",
    val instruction: String = "",
    val durationMinutes: Int = 0,
    val chefTip: String? = null,
    val scienceExplanation: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGeneratedSubDto(
    val originalIngredient: String = "",
    val substituteIngredient: String = "",
    val reason: String = "",
    val ratioNote: String = "1:1 ratio"
)

/**
 * Recipe Suggestion Engine powered by the Gemini API.
 * Cross-references the user's live kitchen inventory, expiring ingredients,
 * and dietary preferences to synthesize personalized recipes.
 */
class RecipeSuggestionEngine(
    private val geminiService: GeminiService = GeminiService(),
    private val fallbackProvider: LocalRuleBasedAIProvider = LocalRuleBasedAIProvider()
) {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, GeminiGeneratedRecipeDto::class.java)
    private val recipeListAdapter = moshi.adapter<List<GeminiGeneratedRecipeDto>>(listType)

    suspend fun generateRecipeSuggestions(
        context: KitchenContext,
        customQuery: String? = null
    ): List<Recipe> {
        if (!geminiService.isApiKeyConfigured()) {
            return generateLocalCrossReferencedRecipes(context, customQuery)
        }

        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(context, customQuery)
                val responseText = geminiService.generateContent(prompt)
                val parsed = parseGeminiRecipes(responseText, context)
                if (parsed.isNotEmpty()) {
                    parsed
                } else {
                    generateLocalCrossReferencedRecipes(context, customQuery)
                }
            } catch (e: Exception) {
                generateLocalCrossReferencedRecipes(context, customQuery)
            }
        }
    }

    private fun buildPrompt(context: KitchenContext, customQuery: String?): String {
        val inventoryList = context.inventory.joinToString(separator = "\n") { item ->
            "- ${item.name} (${item.quantity} ${item.unit}, State: ${item.state.label}, Expires in: ${item.daysUntilExpiry()} days, Location: ${item.location})"
        }
        val expiringItems = context.expiringItems.joinToString { "${it.name} (${it.daysUntilExpiry()}d left)" }

        val dietaryRules = buildList {
            if (context.preferences.isVegan) add("STRICTLY VEGAN: 100% plant-based, no meat, poultry, fish, dairy, eggs, honey.")
            if (context.preferences.isVegetarian && !context.preferences.isVegan) add("VEGETARIAN: No meat, poultry, or seafood; dairy and eggs are allowed.")
            if (context.preferences.isGlutenFree) add("GLUTEN-FREE: Strict avoidance of wheat, barley, rye, or gluten-containing grains.")
            if (context.preferences.isDairyFree) add("DAIRY-FREE: No milk, butter, cheese, or cream.")
            if (context.preferences.isKeto) add("KETO / ULTRA LOW CARB: Under 15g net carbs per serving.")
            add("Spice Preference: ${context.preferences.spicePreference} (${if (context.preferences.isSpicy) "spicy heat welcome" else "keep mild"})")
            add("Dietary Goal: ${context.preferences.dietaryGoal}")
            add("Max Cooking Time: ${context.preferences.maxCookingTimeMin} minutes")
            if (context.preferences.dislikedIngredients.isNotEmpty()) {
                add("Disliked Ingredients to Exclude/Substitute: ${context.preferences.dislikedIngredients.joinToString()}")
            }
            if (context.preferences.allergies.isNotEmpty()) {
                add("ALLERGIES (NEVER USE): ${context.preferences.allergies.joinToString()}")
            }
        }.joinToString("\n")

        val memoriesText = if (context.memories.isNotEmpty()) {
            context.memories.take(3).joinToString("\n") { "- ${it.insight}" }
        } else {
            "None yet"
        }

        return """
            You are Mealio AI, the intelligent autonomous culinary engine.
            Your task is to generate 2 to 3 personalized, delicious recipes by CROSS-REFERENCING the user's CURRENT INVENTORY with their DIETARY PREFERENCES and EXPIRING ITEMS to eliminate food waste.

            === USER'S LIVE INVENTORY ===
            $inventoryList

            === EXPIRING SOON (PRIORITIZE RESCUING THESE) ===
            ${if (expiringItems.isNotBlank()) expiringItems else "None expiring immediately"}

            === DIETARY PREFERENCES & CONSTRAINTS ===
            $dietaryRules

            === TASTE MEMORY & USER GOALS ===
            $memoriesText

            === USER QUERY / INTENT ===
            ${customQuery ?: "Create a delicious, zero-waste recipe using what I have"}

            === SCORING & INSTRUCTION RULES ===
            1. Cross-reference available ingredients: Set "isAvailableInInventory" to true if the item is present in the user's inventory.
            2. If an ingredient is missing or disliked, provide an intelligent culinary substitution with scientific reasoning.
            3. Prioritize expiring ingredients to minimize food waste.
            4. Calculate realistic macros (calories, proteinG, carbsG, fatG).
            5. Provide clear step-by-step instructions with step timers in minutes where applicable, plus culinary chemistry chef tips.
            6. Provide whyRecommended explainability points detailing how the recipe cross-references inventory, dietary goals, and saved ingredients.

            Return ONLY a valid JSON array of objects conforming to the schema below. Do not wrap in markdown or backticks if possible, or return as standard JSON array:
            [
              {
                "title": "Garlic Seared Chicken with Wilted Greens",
                "description": "Succulent chicken breast pan-seared with garlic and wilted baby greens.",
                "cuisine": "Mediterranean",
                "difficulty": "Easy",
                "prepTimeMin": 5,
                "cookTimeMin": 15,
                "calories": 420,
                "proteinG": 38,
                "carbsG": 14,
                "fatG": 12,
                "spiceLevel": 1,
                "matchPercentage": 94,
                "whyRecommended": [
                  "Uses expiring Baby Spinach (2d left)",
                  "5 of 6 ingredients currently in stock",
                  "Matches High-Protein and Gluten-Free preferences",
                  "Ready in 20 mins (under your 35m limit)"
                ],
                "ingredients": [
                  {
                    "name": "Chicken Breast",
                    "amount": "300",
                    "unit": "g",
                    "isAvailableInInventory": true,
                    "substitutionNote": null
                  },
                  {
                    "name": "Baby Spinach",
                    "amount": "150",
                    "unit": "g",
                    "isAvailableInInventory": true,
                    "substitutionNote": null
                  }
                ],
                "steps": [
                  {
                    "stepNumber": 1,
                    "title": "Prep Aromatics & Pan",
                    "instruction": "Mince garlic and heat olive oil in a skillet over medium-high heat.",
                    "durationMinutes": 2,
                    "chefTip": "Pre-heating oil allows an even Maillard reaction without sticking.",
                    "scienceExplanation": "Aromatics release fat-soluble flavor terpenes when heated gently in oil."
                  }
                ],
                "substitutions": [
                  {
                    "originalIngredient": "Butter",
                    "substituteIngredient": "Extra Virgin Olive Oil",
                    "reason": "Higher smoke point and aligns with dairy-free / heart-healthy goals.",
                    "ratioNote": "1:1 ratio"
                  }
                ],
                "dietaryTags": ["High Protein", "Gluten-Free", "Low Carb"],
                "wasteReductionScore": 15
              }
            ]
        """.trimIndent()
    }

    private fun parseGeminiRecipes(jsonText: String, context: KitchenContext): List<Recipe> {
        val clean = extractJsonArray(jsonText)
        if (clean.isBlank()) return emptyList()

        return try {
            val dtos = recipeListAdapter.fromJson(clean) ?: emptyList()
            dtos.map { dto ->
                dtoToDomain(dto, context)
            }
        } catch (e: Exception) {
            // Manual fallback parsing via org.json
            parseWithOrgJson(clean, context)
        }
    }

    private fun parseWithOrgJson(cleanJson: String, context: KitchenContext): List<Recipe> {
        return try {
            val array = JSONArray(cleanJson)
            val recipes = mutableListOf<Recipe>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val title = obj.optString("title", "AI Adaptive Meal")
                val desc = obj.optString("description", "")
                val cuisine = obj.optString("cuisine", "Global")
                val difficulty = obj.optString("difficulty", "Easy")
                val prepTime = obj.optInt("prepTimeMin", 10)
                val cookTime = obj.optInt("cookTimeMin", 15)
                val calories = obj.optInt("calories", 400)
                val protein = obj.optInt("proteinG", 25)
                val carbs = obj.optInt("carbsG", 30)
                val fat = obj.optInt("fatG", 12)
                val spice = obj.optInt("spiceLevel", 1)
                val match = obj.optInt("matchPercentage", 88)
                val waste = obj.optInt("wasteReductionScore", 10)

                val whyList = mutableListOf<String>()
                val whyArr = obj.optJSONArray("whyRecommended")
                if (whyArr != null) {
                    for (j in 0 until whyArr.length()) {
                        whyList.add(whyArr.optString(j))
                    }
                }

                val dietaryTags = mutableListOf<String>()
                val tagArr = obj.optJSONArray("dietaryTags")
                if (tagArr != null) {
                    for (j in 0 until tagArr.length()) {
                        dietaryTags.add(tagArr.optString(j))
                    }
                }

                val ingredients = mutableListOf<RecipeIngredient>()
                val ingArr = obj.optJSONArray("ingredients")
                if (ingArr != null) {
                    for (j in 0 until ingArr.length()) {
                        val ingObj = ingArr.getJSONObject(j)
                        val name = ingObj.optString("name", "")
                        val amount = ingObj.optString("amount", "1")
                        val unit = ingObj.optString("unit", "portion")
                        val isAvail = ingObj.optBoolean("isAvailableInInventory", isItemInInventory(name, context))
                        val subNote = if (ingObj.has("substitutionNote") && !ingObj.isNull("substitutionNote")) ingObj.getString("substitutionNote") else null
                        ingredients.add(RecipeIngredient(name, amount, unit, isAvail, subNote))
                    }
                }

                val steps = mutableListOf<RecipeStep>()
                val stepArr = obj.optJSONArray("steps")
                if (stepArr != null) {
                    for (j in 0 until stepArr.length()) {
                        val stObj = stepArr.getJSONObject(j)
                        val num = stObj.optInt("stepNumber", j + 1)
                        val stTitle = stObj.optString("title", "Step $num")
                        val inst = stObj.optString("instruction", "")
                        val duration = stObj.optInt("durationMinutes", 0)
                        val tip = if (stObj.has("chefTip") && !stObj.isNull("chefTip")) stObj.getString("chefTip") else null
                        val sci = if (stObj.has("scienceExplanation") && !stObj.isNull("scienceExplanation")) stObj.getString("scienceExplanation") else null
                        steps.add(RecipeStep(num, stTitle, inst, duration, tip, sci))
                    }
                }

                val substitutions = mutableListOf<SubstitutionAdvice>()
                val subArr = obj.optJSONArray("substitutions")
                if (subArr != null) {
                    for (j in 0 until subArr.length()) {
                        val subObj = subArr.getJSONObject(j)
                        val orig = subObj.optString("originalIngredient", "")
                        val sub = subObj.optString("substituteIngredient", "")
                        val reason = subObj.optString("reason", "")
                        val ratio = subObj.optString("ratioNote", "1:1")
                        substitutions.add(SubstitutionAdvice(orig, sub, reason, ratio))
                    }
                }

                recipes.add(
                    Recipe(
                        title = title,
                        description = desc,
                        cuisine = cuisine,
                        difficulty = difficulty,
                        prepTimeMin = prepTime,
                        cookTimeMin = cookTime,
                        calories = calories,
                        proteinG = protein,
                        carbsG = carbs,
                        fatG = fat,
                        spiceLevel = spice,
                        matchPercentage = match,
                        whyRecommended = whyList,
                        ingredients = ingredients,
                        steps = steps,
                        substitutions = substitutions,
                        dietaryTags = dietaryTags,
                        wasteReductionScore = waste
                    )
                )
            }
            recipes
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun dtoToDomain(dto: GeminiGeneratedRecipeDto, context: KitchenContext): Recipe {
        val ingredients = dto.ingredients.map { ingDto ->
            val isAvail = ingDto.isAvailableInInventory || isItemInInventory(ingDto.name, context)
            RecipeIngredient(
                name = ingDto.name,
                amount = ingDto.amount,
                unit = ingDto.unit,
                isAvailableInInventory = isAvail,
                substitutionNote = ingDto.substitutionNote
            )
        }

        val steps = dto.steps.map { stDto ->
            RecipeStep(
                stepNumber = stDto.stepNumber,
                title = stDto.title,
                instruction = stDto.instruction,
                durationMinutes = stDto.durationMinutes,
                chefTip = stDto.chefTip,
                scienceExplanation = stDto.scienceExplanation
            )
        }

        val subs = dto.substitutions.map { subDto ->
            SubstitutionAdvice(
                originalIngredient = subDto.originalIngredient,
                substituteIngredient = subDto.substituteIngredient,
                reason = subDto.reason,
                ratioNote = subDto.ratioNote
            )
        }

        return Recipe(
            title = dto.title.ifBlank { "Adaptive Recipe" },
            description = dto.description,
            cuisine = dto.cuisine.ifBlank { "Mediterranean" },
            difficulty = dto.difficulty.ifBlank { "Easy" },
            prepTimeMin = dto.prepTimeMin.coerceAtLeast(3),
            cookTimeMin = dto.cookTimeMin.coerceAtLeast(5),
            calories = dto.calories.coerceAtLeast(150),
            proteinG = dto.proteinG.coerceAtLeast(5),
            carbsG = dto.carbsG.coerceAtLeast(0),
            fatG = dto.fatG.coerceAtLeast(2),
            spiceLevel = dto.spiceLevel.coerceIn(0, 3),
            matchPercentage = dto.matchPercentage.coerceIn(60, 99),
            whyRecommended = if (dto.whyRecommended.isNotEmpty()) dto.whyRecommended else listOf("Cross-referenced with your current inventory and dietary profile"),
            ingredients = ingredients,
            steps = steps,
            substitutions = subs,
            dietaryTags = dto.dietaryTags,
            wasteReductionScore = dto.wasteReductionScore
        )
    }

    private fun isItemInInventory(name: String, context: KitchenContext): Boolean {
        val lower = name.lowercase(Locale.ROOT)
        return context.inventory.any { item ->
            val itemLower = item.name.lowercase(Locale.ROOT)
            lower.contains(itemLower) || itemLower.contains(lower)
        }
    }

    private fun extractJsonArray(text: String): String {
        val start = text.indexOf('[')
        val end = text.lastIndexOf(']')
        return if (start != -1 && end != -1 && end > start) {
            text.substring(start, end + 1).trim()
        } else {
            ""
        }
    }

    private suspend fun generateLocalCrossReferencedRecipes(
        context: KitchenContext,
        customQuery: String?
    ): List<Recipe> {
        return fallbackProvider.generateRecipeRecommendations(context, customQuery)
    }
}

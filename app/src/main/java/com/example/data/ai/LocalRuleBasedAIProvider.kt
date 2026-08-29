package com.example.data.ai

import com.example.data.model.*
import java.util.Locale

class LocalRuleBasedAIProvider : AIProvider {

    override suspend fun analyzeFridgeImage(imageDescriptionOrLabels: String): List<DetectedFoodItem> {
        val lower = imageDescriptionOrLabels.lowercase(Locale.ROOT)
        val detected = mutableListOf<DetectedFoodItem>()

        // Smart preset recognition based on vision scans
        if (lower.contains("drawer") || lower.contains("vegetable") || lower.contains("produce")) {
            detected.add(DetectedFoodItem("Bell Peppers", IngredientCategory.PRODUCE, 3.0, "pcs", 6, 0.96f))
            detected.add(DetectedFoodItem("Zucchini", IngredientCategory.PRODUCE, 2.0, "pcs", 5, 0.91f))
            detected.add(DetectedFoodItem("Fresh Baby Spinach", IngredientCategory.PRODUCE, 200.0, "g", 2, 0.95f))
            detected.add(DetectedFoodItem("Roma Tomatoes", IngredientCategory.PRODUCE, 4.0, "pcs", 4, 0.94f))
            detected.add(DetectedFoodItem("Avocado", IngredientCategory.PRODUCE, 2.0, "pcs", 3, 0.88f))
        } else if (lower.contains("pantry") || lower.contains("shelf") || lower.contains("grain")) {
            detected.add(DetectedFoodItem("Jasmine Rice", IngredientCategory.PANTRY_GRAINS, 1.0, "kg", 180, 0.98f))
            detected.add(DetectedFoodItem("Garlic Cloves", IngredientCategory.CONDIMENTS_SPICES, 1.0, "head", 20, 0.92f))
            detected.add(DetectedFoodItem("Extra Virgin Olive Oil", IngredientCategory.CONDIMENTS_SPICES, 500.0, "ml", 120, 0.97f))
            detected.add(DetectedFoodItem("Pasta (Penne)", IngredientCategory.PANTRY_GRAINS, 500.0, "g", 150, 0.93f))
        } else {
            // Default full fridge scan detection
            detected.add(DetectedFoodItem("Chicken Breast", IngredientCategory.PROTEINS, 500.0, "g", 2, 0.97f))
            detected.add(DetectedFoodItem("Farm Fresh Eggs", IngredientCategory.DAIRY_EGGS, 6.0, "pcs", 10, 0.98f))
            detected.add(DetectedFoodItem("Fresh Baby Spinach", IngredientCategory.PRODUCE, 200.0, "g", 1, 0.95f))
            detected.add(DetectedFoodItem("Roma Tomatoes", IngredientCategory.PRODUCE, 4.0, "pcs", 3, 0.93f))
            detected.add(DetectedFoodItem("Greek Yogurt", IngredientCategory.DAIRY_EGGS, 400.0, "g", 4, 0.92f))
            detected.add(DetectedFoodItem("Cheddar Cheese", IngredientCategory.DAIRY_EGGS, 200.0, "g", 14, 0.89f))
        }

        return detected
    }

    override suspend fun getRecipeRecommendations(ingredients: List<String>): List<Recipe> {
        val dummyContext = KitchenContext(
            inventory = ingredients.map { InventoryItem(name = it, category = IngredientCategory.OTHER, quantity = 1.0, unit = "unit", expiryTimestamp = System.currentTimeMillis() + 86400000L * 5) },
            expiringItems = emptyList(),
            preferences = UserPreferences(),
            memories = emptyList()
        )
        return generateRecipeRecommendations(dummyContext, null)
    }

    override suspend fun generateRecipeRecommendations(
        context: KitchenContext,
        customQuery: String?
    ): List<Recipe> {
        val inventoryNames = context.inventory.map { it.name.lowercase(Locale.ROOT) }
        val expiringNames = context.expiringItems.map { it.name.lowercase(Locale.ROOT) }

        // Ranking algorithm weights specified in user prompt:
        // Ingredient match: 35%
        // Preference match: 25%
        // Cooking time: 20%
        // Waste reduction: 15%
        // Variety: 5%

        return listOf(
            createRankedRecipe(
                title = "Garlic Seared Chicken & Wilted Spinach Bowl",
                desc = "Pan-seared tender chicken breast served over fluffy jasmine rice with garlic-wilted baby spinach and fresh tomato garnish.",
                cuisine = "Mediterranean",
                prepMin = 10,
                cookMin = 15,
                cal = 480,
                protein = 42,
                carbs = 38,
                fat = 14,
                spice = 1,
                dietary = listOf("High Protein", "Gluten-Free", "Clean Eating"),
                requiredIngredients = listOf("chicken breast", "fresh baby spinach", "jasmine rice", "garlic cloves", "olive oil", "roma tomatoes"),
                inventoryNames = inventoryNames,
                expiringNames = expiringNames,
                context = context
            ),
            createRankedRecipe(
                title = "Mediterranean Shakshuka with Poached Eggs",
                desc = "Gently poached farm eggs in a rich, spiced garlic tomato sauce with wilted greens, served warm.",
                cuisine = "Mediterranean",
                prepMin = 5,
                cookMin = 15,
                cal = 340,
                protein = 22,
                carbs = 18,
                fat = 20,
                spice = 2,
                dietary = listOf("Vegetarian", "High Protein", "Low Carb"),
                requiredIngredients = listOf("farm fresh eggs", "roma tomatoes", "fresh baby spinach", "garlic cloves", "olive oil"),
                inventoryNames = inventoryNames,
                expiringNames = expiringNames,
                context = context
            ),
            createRankedRecipe(
                title = "Savory Golden Egg Fried Rice",
                desc = "Aromatic wok-style fried rice infused with garlic, scrambled farm eggs, wilted greens, and caramelized savory aromatics.",
                cuisine = "Asian / Stir-Fry",
                prepMin = 5,
                cookMin = 10,
                cal = 410,
                protein = 18,
                carbs = 52,
                fat = 14,
                spice = 1,
                dietary = listOf("Vegetarian", "Quick Meal", "Comfort Food"),
                requiredIngredients = listOf("jasmine rice", "farm fresh eggs", "fresh baby spinach", "garlic cloves", "olive oil"),
                inventoryNames = inventoryNames,
                expiringNames = expiringNames,
                context = context
            ),
            createRankedRecipe(
                title = "Creamy Garlic Greek Yogurt Chicken Skillet",
                desc = "Juicy seared chicken simmered in a light, high-protein garlic herb yogurt sauce with blistered tomatoes.",
                cuisine = "Mediterranean",
                prepMin = 10,
                cookMin = 15,
                cal = 460,
                protein = 48,
                carbs = 12,
                fat = 16,
                spice = 1,
                dietary = listOf("High Protein", "Keto Friendly", "Low Carb"),
                requiredIngredients = listOf("chicken breast", "greek yogurt", "roma tomatoes", "garlic cloves", "olive oil", "fresh baby spinach"),
                inventoryNames = inventoryNames,
                expiringNames = expiringNames,
                context = context
            ),
            createRankedRecipe(
                title = "20-Min Protein Scramble with Sautéed Greens & Tomatoes",
                desc = "Fluffy scrambled eggs with garlic-sautéed spinach and sweet blistered tomatoes, high in micronutrients and quick to prepare.",
                cuisine = "American",
                prepMin = 5,
                cookMin = 10,
                cal = 310,
                protein = 24,
                carbs = 8,
                fat = 20,
                spice = 1,
                dietary = listOf("Keto", "High Protein", "Vegetarian", "Under 15 Mins"),
                requiredIngredients = listOf("farm fresh eggs", "fresh baby spinach", "roma tomatoes", "olive oil", "garlic cloves"),
                inventoryNames = inventoryNames,
                expiringNames = expiringNames,
                context = context
            )
        ).sortedByDescending { it.matchPercentage }
    }

    private fun createRankedRecipe(
        title: String,
        desc: String,
        cuisine: String,
        prepMin: Int,
        cookMin: Int,
        cal: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        spice: Int,
        dietary: List<String>,
        requiredIngredients: List<String>,
        inventoryNames: List<String>,
        expiringNames: List<String>,
        context: KitchenContext
    ): Recipe {
        val totalTime = prepMin + cookMin

        // 1. Ingredient match (35%)
        val matchedCount = requiredIngredients.count { req ->
            inventoryNames.any { it.contains(req) || req.contains(it) }
        }
        val ingredientMatchRatio = matchedCount.toFloat() / requiredIngredients.size.coerceAtLeast(1)
        val ingredientScore = ingredientMatchRatio * 35f

        // 2. Preference match (25%)
        var prefScore = 15f
        if (context.preferences.favoriteCuisines.any { it.contains(cuisine, ignoreCase = true) }) {
            prefScore += 10f
        }
        if (context.preferences.dietaryGoal.contains("Protein", ignoreCase = true) && protein >= 25) {
            prefScore = (prefScore + 5f).coerceAtMost(25f)
        }

        // 3. Cooking time (20%)
        val maxTime = context.preferences.maxCookingTimeMin
        val timeScore = if (totalTime <= maxTime) {
            20f
        } else {
            (20f - ((totalTime - maxTime) * 1.5f)).coerceAtLeast(5f)
        }

        // 4. Waste reduction (15%) - checks expiring items
        val usesExpiring = requiredIngredients.count { req ->
            expiringNames.any { it.contains(req) || req.contains(it) }
        }
        val wasteScore = if (usesExpiring > 0) (usesExpiring * 7.5f).coerceAtMost(15f) else 5f

        // 5. Variety (5%)
        val varietyScore = 4.5f

        val finalScore = (ingredientScore + prefScore + timeScore + wasteScore + varietyScore).toInt().coerceIn(60, 99)

        val whyList = mutableListOf<String>()
        if (usesExpiring > 0) {
            whyList.add("Rescues expiring ingredients (${context.expiringItems.take(2).joinToString { it.name }})")
        }
        whyList.add("High ingredient match: $matchedCount of ${requiredIngredients.size} items in stock")
        if (totalTime <= maxTime) {
            whyList.add("Ready in $totalTime mins (under your $maxTime min target)")
        }
        if (protein >= 30) {
            whyList.add("Aligns with ${context.preferences.dietaryGoal} ($protein g protein)")
        }

        val ingredientsList = requiredIngredients.map { name ->
            val hasIt = inventoryNames.any { it.contains(name) || name.contains(it) }
            RecipeIngredient(
                name = name.replaceFirstChar { it.uppercase() },
                amount = "1",
                unit = "portion",
                isAvailableInInventory = hasIt,
                substitutionNote = if (!hasIt) "Can substitute with pantry alternatives" else null
            )
        }

        return Recipe(
            title = title,
            description = desc,
            cuisine = cuisine,
            difficulty = if (totalTime <= 20) "Easy" else "Medium",
            prepTimeMin = prepMin,
            cookTimeMin = cookMin,
            calories = cal,
            proteinG = protein,
            carbsG = carbs,
            fatG = fat,
            spiceLevel = spice,
            matchPercentage = finalScore,
            whyRecommended = whyList,
            ingredients = ingredientsList,
            steps = listOf(
                RecipeStep(1, "Preparation", "Prep and measure all ingredients cleanly on your cutting board.", 3, "Mise en place keeps cooking stress-free.", "Pre-measuring prevents burning aromatics."),
                RecipeStep(2, "Cook Aromatics", "Sauté garlic and aromatics in heated olive oil until fragrant.", 2, "Golden garlic gives sweet nutty undertones.", "Releases allicin and aroma terpenes."),
                RecipeStep(3, "Main Cooking & Assembly", "Combine proteins/eggs and simmer with vegetables until tender and cooked through.", 10, "Check internal temperature for juicy finish.", "Proteins set cleanly at 74°C (165°F)."),
                RecipeStep(4, "Plating & Flavor Finish", "Plate over grains or fresh greens, garnish with herbs and serve.", 2, "Season with cracked pepper at the very end.", "Heat destroys delicate pepper aromatics if added too early.")
            ),
            substitutions = listOf(
                SubstitutionAdvice("Butter", "Ghee or Olive Oil", "Provides high smoke point with nutty profile", "1:1 ratio")
            ),
            dietaryTags = dietary,
            wasteReductionScore = (wasteScore * 2).toInt()
        )
    }

    override suspend fun getSubstitutionAdvice(
        missingIngredient: String,
        context: KitchenContext
    ): SubstitutionAdvice {
        val lower = missingIngredient.lowercase(Locale.ROOT)
        return when {
            lower.contains("butter") -> SubstitutionAdvice(
                originalIngredient = "Butter",
                substituteIngredient = "Ghee or Olive Oil",
                reason = "Ghee offers identical rich milk fat and higher smoke point without burning; olive oil provides heart-healthy monounsaturated fat.",
                ratioNote = "Use 1:1 replacement ratio."
            )
            lower.contains("cream") || lower.contains("heavy cream") -> SubstitutionAdvice(
                originalIngredient = "Heavy Cream",
                substituteIngredient = "Greek Yogurt + splash of water",
                reason = "Maintains silky creamy mouthfeel while adding probiotic acidity and 15g extra protein.",
                ratioNote = "Whisk 3 tbsp Greek yogurt with 1 tbsp warm water on low heat."
            )
            lower.contains("egg") -> SubstitutionAdvice(
                originalIngredient = "Eggs",
                substituteIngredient = "Ground Flaxseed + Water or Silken Tofu",
                reason = "Flax gel binds baked goods; crumbled tofu replicates scrambled egg texture and protein.",
                ratioNote = "1 tbsp ground flax + 3 tbsp water = 1 egg (let stand 5 mins)."
            )
            lower.contains("sugar") -> SubstitutionAdvice(
                originalIngredient = "Sugar",
                substituteIngredient = "Honey or Pure Maple Syrup",
                reason = "Natural unrefined sweeteners add complex caramel undertones and retain moisture.",
                ratioNote = "Use 3/4 cup honey for every 1 cup white sugar."
            )
            lower.contains("soy sauce") -> SubstitutionAdvice(
                originalIngredient = "Soy Sauce",
                substituteIngredient = "Coconut Aminos or Tamari",
                reason = "Tamari is gluten-free with deep umami; coconut aminos provides lower sodium with mild sweetness.",
                ratioNote = "Use 1:1 replacement ratio."
            )
            lower.contains("lemon") || lower.contains("lemon juice") -> SubstitutionAdvice(
                originalIngredient = "Lemon Juice",
                substituteIngredient = "Apple Cider Vinegar or Rice Vinegar",
                reason = "Delivers the crisp bright acid needed to cut through rich sauces and balance savory fats.",
                ratioNote = "Use 1/2 tsp vinegar for every 1 tsp lemon juice."
            )
            else -> SubstitutionAdvice(
                originalIngredient = missingIngredient,
                substituteIngredient = "Pantry Neutral Seasoning / Olive Oil",
                reason = "Balances moisture and savory depth using available pantry staples.",
                ratioNote = "Adjust to taste."
            )
        }
    }

    override suspend fun chatWithChef(
        userMessage: String,
        context: KitchenContext,
        conversationHistory: List<ChatMessage>
    ): String {
        val lower = userMessage.lowercase(Locale.ROOT)
        val invSummary = context.inventory.joinToString(", ") { "${it.name} (${it.state.label})" }
        val expiring = context.expiringItems.joinToString(", ") { it.name }

        return when {
            lower.contains("what can i cook") || lower.contains("what should i make") || lower.contains("dinner") || lower.contains("lunch") -> {
                val best = if (context.expiringItems.isNotEmpty()) {
                    "Since your **$expiring** is expiring soon, I strongly recommend making the **Garlic Seared Chicken & Wilted Spinach Bowl** or **Mediterranean Shakshuka**! Both take under 25 minutes, pack over 22-42g of protein, and eliminate kitchen waste."
                } else {
                    "Based on your current stock of **$invSummary**, you can prepare a delicious **Savory Golden Egg Fried Rice** or **Creamy Garlic Yogurt Chicken Skillet** in 15-20 minutes."
                }
                "$best\n\nWould you like me to walk you through the recipe step-by-step with voice and timers in Cooking Mode?"
            }
            lower.contains("substitute") || lower.contains("replace") || lower.contains("instead of") -> {
                "In Mealy's culinary chemistry engine, substitutions are adapted to preserve texture and flavor:\n\n" +
                "• **Butter → Ghee or Olive Oil**: 1:1 ratio. Higher smoke point, rich nutty aroma.\n" +
                "• **Heavy Cream → Greek Yogurt**: 3 parts yogurt to 1 part warm water on low heat. Adds 15g protein!\n" +
                "• **Soy Sauce → Coconut Aminos / Tamari**: Gluten-free with balanced savory umami.\n\n" +
                "Which specific ingredient in your kitchen would you like to swap?"
            }
            lower.contains("waste") || lower.contains("expire") || lower.contains("expiring") -> {
                if (context.expiringItems.isNotEmpty()) {
                    "⚠️ **Food Waste Alert**: You have **${context.expiringItems.size} items** expiring soon: **$expiring**.\n\nBy cooking with these tonight, you save an estimated **$11.40** and prevent 1.2 kg of CO2 equivalent emissions! Check the top suggested recipes on your Home feed to use them immediately."
                } else {
                    "Great news! All your stored ingredients are currently fresh. Your oldest items have several days of shelf life remaining."
                }
            }
            lower.contains("timer") || lower.contains("how long") -> {
                "When cooking proteins like chicken cutlets, sear for **4-5 minutes per side** over medium-high heat until the internal temperature reaches **74°C (165°F)**. In Cooking Mode, I can run hands-free audio timers for each step!"
            }
            lower.contains("protein") || lower.contains("diet") || lower.contains("macro") -> {
                "Your profile is tuned to **${context.preferences.dietaryGoal}**. Our recipes average **35g+ clean protein** per serving with balanced low-glycemic carbohydrates and healthy fats."
            }
            else -> {
                "Hello Chef! I'm Mealy, your AI kitchen companion. I have full real-time awareness of your inventory (**${context.inventory.size} items tracked**), your spice preference (**${context.preferences.spicePreference}**), and your goal (**${context.preferences.dietaryGoal}**).\n\nYou can ask me:\n• *'What can I cook with my chicken and spinach?'*\n• *'How do I substitute butter in sautéing?'*\n• *'Give me a 15-minute high-protein dinner'*."
            }
        }
    }
}

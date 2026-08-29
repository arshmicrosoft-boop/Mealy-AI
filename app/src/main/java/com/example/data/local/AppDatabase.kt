package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        InventoryItem::class,
        Ingredient::class,
        Recipe::class,
        UserPreferences::class,
        CookingSession::class,
        KitchenMemoryLog::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun preferencesDao(): PreferencesDao
    abstract fun cookingSessionDao(): CookingSessionDao
    abstract fun kitchenMemoryDao(): KitchenMemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mealy_ai_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            // 1. Initial Inventory with realistic shelf lives (some expiring soon to showcase Food Waste Intelligence!)
            val initialInventory = listOf(
                InventoryItem(
                    name = "Fresh Baby Spinach",
                    category = IngredientCategory.PRODUCE,
                    quantity = 200.0,
                    unit = "g",
                    state = FoodState.OPENED,
                    expiryTimestamp = now + (1 * dayMs), // Expiring tomorrow!
                    location = "Fridge",
                    notes = "Opened 2 days ago, use quickly for salads or sautéing",
                    estimatedPriceDollars = 3.99
                ),
                InventoryItem(
                    name = "Chicken Breast",
                    category = IngredientCategory.PROTEINS,
                    quantity = 500.0,
                    unit = "g",
                    state = FoodState.STORED,
                    expiryTimestamp = now + (2 * dayMs), // Expiring in 2 days
                    location = "Fridge",
                    notes = "Fresh boneless chicken fillets",
                    estimatedPriceDollars = 7.50
                ),
                InventoryItem(
                    name = "Farm Fresh Eggs",
                    category = IngredientCategory.DAIRY_EGGS,
                    quantity = 6.0,
                    unit = "pcs",
                    state = FoodState.STORED,
                    expiryTimestamp = now + (10 * dayMs),
                    location = "Fridge",
                    notes = "Grade A large eggs",
                    estimatedPriceDollars = 4.20
                ),
                InventoryItem(
                    name = "Roma Tomatoes",
                    category = IngredientCategory.PRODUCE,
                    quantity = 4.0,
                    unit = "pcs",
                    state = FoodState.STORED,
                    expiryTimestamp = now + (3 * dayMs),
                    location = "Fridge",
                    notes = "Ripe and firm",
                    estimatedPriceDollars = 2.80
                ),
                InventoryItem(
                    name = "Jasmine Rice",
                    category = IngredientCategory.PANTRY_GRAINS,
                    quantity = 1.0,
                    unit = "kg",
                    state = FoodState.STORED,
                    expiryTimestamp = now + (180 * dayMs),
                    location = "Pantry",
                    notes = "Aromatic long-grain rice",
                    estimatedPriceDollars = 3.20
                ),
                InventoryItem(
                    name = "Garlic Cloves",
                    category = IngredientCategory.CONDIMENTS_SPICES,
                    quantity = 1.0,
                    unit = "head",
                    state = FoodState.STORED,
                    expiryTimestamp = now + (20 * dayMs),
                    location = "Pantry",
                    notes = "Fresh purple garlic",
                    estimatedPriceDollars = 1.00
                ),
                InventoryItem(
                    name = "Extra Virgin Olive Oil",
                    category = IngredientCategory.CONDIMENTS_SPICES,
                    quantity = 500.0,
                    unit = "ml",
                    state = FoodState.OPENED,
                    expiryTimestamp = now + (120 * dayMs),
                    location = "Pantry",
                    notes = "Cold pressed",
                    estimatedPriceDollars = 8.50
                ),
                InventoryItem(
                    name = "Greek Yogurt",
                    category = IngredientCategory.DAIRY_EGGS,
                    quantity = 400.0,
                    unit = "g",
                    state = FoodState.OPENED,
                    expiryTimestamp = now + (4 * dayMs),
                    location = "Fridge",
                    notes = "Plain whole milk Greek yogurt",
                    estimatedPriceDollars = 4.00
                )
            )
            db.inventoryDao().insertAll(initialInventory)

            // 2. Default User Preferences
            val defaultPrefs = UserPreferences(
                id = 1,
                favoriteCuisines = listOf("Italian", "Asian Stir-Fry", "Mediterranean", "Mexican"),
                spicePreference = "Medium",
                dislikedIngredients = listOf("Cilantro"),
                allergies = emptyList(),
                dietaryGoal = "High Protein & Balanced",
                maxCookingTimeMin = 30,
                cookingSkill = "Intermediate",
                mealHabit = "Quick Weekday Meals",
                handsFreeTtsEnabled = true,
                autoVoiceOnStepChange = true
            )
            db.preferencesDao().savePreferences(defaultPrefs)

            // 3. Initial Kitchen Memory Logs
            val initialMemories = listOf(
                KitchenMemoryLog(
                    category = "TASTE_PREFERENCE",
                    insight = "Enjoys garlic and warm aromatic herbs in chicken sautés",
                    actionTaken = "AI adjusts seasoning recommendations to elevate garlic & thyme"
                ),
                KitchenMemoryLog(
                    category = "SUBSTITUTION_LEARNING",
                    insight = "Successfully substituted Ghee for Butter with excellent aroma",
                    actionTaken = "AI automatically suggests ghee for high-smoke-point searing"
                ),
                KitchenMemoryLog(
                    category = "WASTE_HABIT",
                    insight = "Frequently rescues leafy greens when prompted before expiry",
                    actionTaken = "AI prioritizes spinach and herbs in top 3 recommended recipes"
                )
            )
            initialMemories.forEach { db.kitchenMemoryDao().insertMemory(it) }

            // 4. Initial Base Recipe Catalog
            val initialRecipes = getBaseRecipes()
            db.recipeDao().insertAll(initialRecipes)
        }

        private fun getBaseRecipes(): List<Recipe> {
            return listOf(
                Recipe(
                    title = "Garlic Seared Chicken & Wilted Spinach Bowl",
                    description = "Pan-seared tender chicken breast served over fluffy jasmine rice with garlic-wilted baby spinach and fresh tomato garnish.",
                    cuisine = "Mediterranean",
                    difficulty = "Easy",
                    prepTimeMin = 10,
                    cookTimeMin = 15,
                    calories = 480,
                    proteinG = 42,
                    carbsG = 38,
                    fatG = 14,
                    fiberG = 4,
                    spiceLevel = 1,
                    matchPercentage = 98,
                    whyRecommended = listOf(
                        "Uses your Chicken Breast (expiring in 2 days)",
                        "Uses your Fresh Baby Spinach (expiring tomorrow!)",
                        "Matches your 30-min cooking goal (takes 25 mins)",
                        "High Protein: 42g per serving"
                    ),
                    ingredients = listOf(
                        RecipeIngredient("Chicken Breast", "300", "g", isAvailableInInventory = true),
                        RecipeIngredient("Fresh Baby Spinach", "150", "g", isAvailableInInventory = true),
                        RecipeIngredient("Jasmine Rice", "1", "cup", isAvailableInInventory = true),
                        RecipeIngredient("Garlic Cloves", "3", "cloves", isAvailableInInventory = true),
                        RecipeIngredient("Roma Tomatoes", "1", "diced", isAvailableInInventory = true),
                        RecipeIngredient("Olive Oil", "1.5", "tbsp", isAvailableInInventory = true),
                        RecipeIngredient("Butter", "1", "tbsp", isAvailableInInventory = false, substitutionNote = "Substituted with Ghee or Olive Oil")
                    ),
                    steps = listOf(
                        RecipeStep(
                            stepNumber = 1,
                            title = "Cook the Jasmine Rice",
                            instruction = "Rinse 1 cup of jasmine rice until water runs clear. Combine with 1.5 cups water and a pinch of salt in a small pot. Bring to a boil, cover, reduce heat to low, and simmer for 12 minutes.",
                            durationMinutes = 12,
                            chefTip = "Do not lift the lid during the simmer to lock in steam.",
                            scienceExplanation = "Steam gelatinizes rice starches evenly without creating mushy grains.",
                            ingredientsNeeded = listOf("Jasmine Rice", "Water", "Salt")
                        ),
                        RecipeStep(
                            stepNumber = 2,
                            title = "Prep and Season the Chicken",
                            instruction = "Pat the chicken breast dry with a paper towel. Slice horizontally into even cutlets. Season both sides generously with salt, cracked black pepper, and minced garlic.",
                            durationMinutes = 3,
                            chefTip = "Drying meat thoroughly ensures a crisp, golden crust when searing.",
                            scienceExplanation = "Moisture prevents the pan surface from exceeding 100°C, causing steaming instead of the Maillard browning reaction.",
                            ingredientsNeeded = listOf("Chicken Breast", "Garlic", "Salt", "Pepper")
                        ),
                        RecipeStep(
                            stepNumber = 3,
                            title = "Sear Chicken to Golden Perfection",
                            instruction = "Heat 1 tbsp olive oil in a skillet over medium-high heat. Add chicken cutlets and cook undisturbed for 4-5 minutes per side until deeply golden and internal temp reaches 74°C (165°F). Transfer to a plate to rest.",
                            durationMinutes = 9,
                            chefTip = "Let resting chicken sit for 3 minutes so juices redistribute evenly.",
                            scienceExplanation = "Resting allows muscle fibers to relax and retain savory meat juices.",
                            ingredientsNeeded = listOf("Chicken Breast", "Olive Oil")
                        ),
                        RecipeStep(
                            stepNumber = 4,
                            title = "Wilt Spinach & Warm Tomatoes",
                            instruction = "In the same skillet with remaining juices, add minced garlic and sauté for 30 seconds. Toss in the fresh baby spinach and diced tomatoes. Cook for 90 seconds until spinach wilts gently.",
                            durationMinutes = 2,
                            chefTip = "Remove from heat immediately as soon as spinach collapses to keep its bright emerald color.",
                            scienceExplanation = "Brief flash-cooking preserves heat-sensitive vitamin C and vibrant chlorophyll pigments.",
                            ingredientsNeeded = listOf("Baby Spinach", "Roma Tomatoes", "Garlic")
                        ),
                        RecipeStep(
                            stepNumber = 5,
                            title = "Assemble and Serve",
                            instruction = "Fluff the jasmine rice with a fork. Layer rice into bowls, top with sliced chicken breast, garlic spinach, and diced tomatoes. Drizzle pan juices on top.",
                            durationMinutes = 2,
                            chefTip = "Add a squeeze of fresh lemon juice or Greek yogurt dollop if desired.",
                            scienceExplanation = "Acidity balances the rich savory proteins and elevates aroma.",
                            ingredientsNeeded = listOf("Jasmine Rice", "Chicken Breast", "Spinach", "Tomatoes")
                        )
                    ),
                    substitutions = listOf(
                        SubstitutionAdvice(
                            originalIngredient = "Butter",
                            substituteIngredient = "Olive Oil or Ghee",
                            reason = "Provides high smoke point and healthy monounsaturated fats while maintaining pan richness.",
                            ratioNote = "Use 1:1 ratio."
                        )
                    ),
                    dietaryTags = listOf("High Protein", "Gluten-Free", "Clean Eating"),
                    wasteReductionScore = 15
                ),
                Recipe(
                    title = "Mediterranean Shakshuka with Poached Eggs",
                    description = "Gently poached farm eggs in a rich, spiced garlic tomato sauce with wilted greens, served warm.",
                    cuisine = "Mediterranean",
                    difficulty = "Easy",
                    prepTimeMin = 5,
                    cookTimeMin = 15,
                    calories = 340,
                    proteinG = 22,
                    carbsG = 18,
                    fatG = 20,
                    fiberG = 5,
                    spiceLevel = 2,
                    matchPercentage = 94,
                    whyRecommended = listOf(
                        "Uses your Farm Fresh Eggs",
                        "Uses your Roma Tomatoes & Spinach",
                        "Matches your Medium spice preference",
                        "Ready in only 20 minutes"
                    ),
                    ingredients = listOf(
                        RecipeIngredient("Farm Fresh Eggs", "3", "large", isAvailableInInventory = true),
                        RecipeIngredient("Roma Tomatoes", "3", "diced", isAvailableInInventory = true),
                        RecipeIngredient("Fresh Baby Spinach", "80", "g", isAvailableInInventory = true),
                        RecipeIngredient("Garlic Cloves", "2", "minced", isAvailableInInventory = true),
                        RecipeIngredient("Olive Oil", "1", "tbsp", isAvailableInInventory = true),
                        RecipeIngredient("Paprika & Cumin", "1", "tsp", isAvailableInInventory = true),
                        RecipeIngredient("Feta Cheese", "30", "g", isAvailableInInventory = false, substitutionNote = "Substituted with Greek Yogurt dollop")
                    ),
                    steps = listOf(
                        RecipeStep(
                            stepNumber = 1,
                            title = "Build the Spiced Aromatic Base",
                            instruction = "Heat olive oil in a skillet over medium heat. Sauté minced garlic, paprika, and cumin for 1 minute until fragrant.",
                            durationMinutes = 2,
                            chefTip = "Blooming spices in warm oil releases fat-soluble aroma compounds.",
                            scienceExplanation = "Spices contain fat-soluble terpenes that unlock flavor when heated in oil.",
                            ingredientsNeeded = listOf("Olive Oil", "Garlic", "Paprika", "Cumin")
                        ),
                        RecipeStep(
                            stepNumber = 2,
                            title = "Simmer the Tomato & Spinach Sauce",
                            instruction = "Add diced tomatoes and a splash of water. Simmer for 6 minutes until tomatoes break down into a thick sauce. Stir in spinach until wilted.",
                            durationMinutes = 6,
                            chefTip = "Crush tomatoes lightly with the back of your spoon.",
                            scienceExplanation = "Heat breaks down tomato cell walls, concentrating natural glutamates.",
                            ingredientsNeeded = listOf("Roma Tomatoes", "Baby Spinach")
                        ),
                        RecipeStep(
                            stepNumber = 3,
                            title = "Poach Eggs in Sauce Wells",
                            instruction = "Create 3 small wells in the sauce. Crack an egg directly into each well. Cover skillet with lid and cook on low heat for 5-6 minutes until egg whites are set and yolks remain runny.",
                            durationMinutes = 6,
                            chefTip = "Keep the lid on tightly to let steam cook the top of the egg whites.",
                            scienceExplanation = "Egg white proteins (albumen) coagulate at 62-65°C while yolks stay silky at lower temps.",
                            ingredientsNeeded = listOf("Farm Fresh Eggs")
                        ),
                        RecipeStep(
                            stepNumber = 4,
                            title = "Garnish and Serve",
                            instruction = "Remove from heat, season with fresh cracked pepper and add a spoonful of Greek yogurt or herbs. Serve straight from the pan.",
                            durationMinutes = 1,
                            chefTip = "Dip warm crusty bread or spoon over cooked rice.",
                            scienceExplanation = "Creamy yogurt contrasts against acidic tomato sauce.",
                            ingredientsNeeded = listOf("Greek Yogurt", "Black Pepper")
                        )
                    ),
                    substitutions = listOf(
                        SubstitutionAdvice(
                            originalIngredient = "Feta Cheese",
                            substituteIngredient = "Greek Yogurt",
                            reason = "Adds creamy tanginess and extra protein while keeping dairy fresh.",
                            ratioNote = "Dollop 2 tbsp on top before serving."
                        )
                    ),
                    dietaryTags = listOf("Vegetarian", "High Protein", "Low Carb"),
                    wasteReductionScore = 12
                ),
                Recipe(
                    title = "Savory Golden Egg Fried Rice",
                    description = "Aromatic wok-style fried rice infused with garlic, scrambled farm eggs, wilted greens, and caramelized savory aromatics.",
                    cuisine = "Asian / Stir-Fry",
                    difficulty = "Easy",
                    prepTimeMin = 5,
                    cookTimeMin = 10,
                    calories = 410,
                    proteinG = 18,
                    carbsG = 52,
                    fatG = 14,
                    fiberG = 3,
                    spiceLevel = 1,
                    matchPercentage = 91,
                    whyRecommended = listOf(
                        "100% ingredients in your kitchen",
                        "Ultra fast 15-minute meal",
                        "Great way to use cooked Jasmine Rice and Spinach"
                    ),
                    ingredients = listOf(
                        RecipeIngredient("Jasmine Rice (Cooked)", "2", "cups", isAvailableInInventory = true),
                        RecipeIngredient("Farm Fresh Eggs", "3", "beaten", isAvailableInInventory = true),
                        RecipeIngredient("Fresh Baby Spinach", "60", "g", isAvailableInInventory = true),
                        RecipeIngredient("Garlic Cloves", "3", "minced", isAvailableInInventory = true),
                        RecipeIngredient("Olive Oil", "1.5", "tbsp", isAvailableInInventory = true),
                        RecipeIngredient("Soy Sauce", "1.5", "tbsp", isAvailableInInventory = true)
                    ),
                    steps = listOf(
                        RecipeStep(
                            stepNumber = 1,
                            title = "Scramble Eggs",
                            instruction = "Heat 1/2 tbsp oil in a large skillet over high heat. Pour in beaten eggs. Swirl and scramble for 45 seconds until soft curd forms. Transfer to a bowl.",
                            durationMinutes = 1,
                            chefTip = "Do not overcook eggs here; they finish cooking when tossed back into the rice.",
                            scienceExplanation = "Gentle coagulation keeps curds fluffy.",
                            ingredientsNeeded = listOf("Farm Fresh Eggs", "Olive Oil")
                        ),
                        RecipeStep(
                            stepNumber = 2,
                            title = "Sauté Garlic & Fry Rice",
                            instruction = "Add remaining oil to the pan on high heat. Add garlic for 20 seconds, then dump in the cooked rice. Press flat to let grains sear, tossing occasionally for 4 minutes.",
                            durationMinutes = 4,
                            chefTip = "Day-old chilled rice fries best without clumping.",
                            scienceExplanation = "Retrograded starches in cold rice stay firm and absorb savory pan oils.",
                            ingredientsNeeded = listOf("Jasmine Rice", "Garlic Cloves", "Olive Oil")
                        ),
                        RecipeStep(
                            stepNumber = 3,
                            title = "Toss with Greens & Sauce",
                            instruction = "Drizzle soy sauce around the outer edge of the skillet. Toss in spinach and cooked eggs. Stir-fry vigorously for 90 seconds until spinach is tender and grains are golden.",
                            durationMinutes = 2,
                            chefTip = "Pouring sauce along the hot pan wall caramelizes sugars instantly.",
                            scienceExplanation = "Caramelization against the hot metal wall creates authentic wok aroma.",
                            ingredientsNeeded = listOf("Baby Spinach", "Soy Sauce", "Scrambled Eggs")
                        )
                    ),
                    substitutions = listOf(
                        SubstitutionAdvice(
                            originalIngredient = "Butter",
                            substituteIngredient = "Olive Oil or Ghee",
                            reason = "Provides cleaner wok-sear without burning milk solids.",
                            ratioNote = "1:1 ratio"
                        )
                    ),
                    dietaryTags = listOf("Vegetarian", "Quick Meal", "Comfort Food"),
                    wasteReductionScore = 10
                ),
                Recipe(
                    title = "Creamy Garlic Greek Yogurt Chicken Skillet",
                    description = "Juicy seared chicken simmered in a light, high-protein garlic herb yogurt sauce with blistered tomatoes.",
                    cuisine = "Mediterranean",
                    difficulty = "Medium",
                    prepTimeMin = 10,
                    cookTimeMin = 15,
                    calories = 460,
                    proteinG = 48,
                    carbsG = 12,
                    fatG = 16,
                    fiberG = 2,
                    spiceLevel = 1,
                    matchPercentage = 95,
                    whyRecommended = listOf(
                        "Uses Chicken Breast & Greek Yogurt",
                        "High Protein: 48g per portion",
                        "Uses Roma Tomatoes & Garlic"
                    ),
                    ingredients = listOf(
                        RecipeIngredient("Chicken Breast", "350", "g", isAvailableInInventory = true),
                        RecipeIngredient("Greek Yogurt", "150", "g", isAvailableInInventory = true),
                        RecipeIngredient("Roma Tomatoes", "2", "sliced", isAvailableInInventory = true),
                        RecipeIngredient("Garlic Cloves", "4", "crushed", isAvailableInInventory = true),
                        RecipeIngredient("Olive Oil", "1", "tbsp", isAvailableInInventory = true),
                        RecipeIngredient("Fresh Baby Spinach", "50", "g", isAvailableInInventory = true)
                    ),
                    steps = listOf(
                        RecipeStep(
                            stepNumber = 1,
                            title = "Sear Chicken",
                            instruction = "Season chicken with salt, pepper, and garlic powder. Sear in 1 tbsp olive oil over medium-high heat for 5 minutes per side until cooked through. Set aside.",
                            durationMinutes = 10,
                            chefTip = "Ensure skillet is thoroughly heated before adding chicken.",
                            scienceExplanation = "High contact heat kickstarts the Maillard browning process.",
                            ingredientsNeeded = listOf("Chicken Breast", "Olive Oil")
                        ),
                        RecipeStep(
                            stepNumber = 2,
                            title = "Blister Tomatoes & Garlic",
                            instruction = "In same skillet, sauté crushed garlic and tomato slices for 3 minutes until tomatoes soften and release juices.",
                            durationMinutes = 3,
                            chefTip = "Scrape up the golden chicken bits stuck to the bottom of the pan.",
                            scienceExplanation = "The browned bits (fond) contain concentrated savoriness.",
                            ingredientsNeeded = listOf("Roma Tomatoes", "Garlic Cloves")
                        ),
                        RecipeStep(
                            stepNumber = 3,
                            title = "Simmer Yogurt Sauce",
                            instruction = "Reduce heat to lowest setting. Whisk in Greek yogurt and 2 tbsp warm water. Stir constantly to create a silky sauce without curdling. Return chicken to skillet and warm through.",
                            durationMinutes = 3,
                            chefTip = "Keep heat low when adding Greek yogurt so it stays smooth.",
                            scienceExplanation = "Yogurt proteins denature and separate if boiled rapidly; low heat preserves emulsion.",
                            ingredientsNeeded = listOf("Greek Yogurt", "Chicken Breast", "Baby Spinach")
                        )
                    ),
                    substitutions = listOf(
                        SubstitutionAdvice(
                            originalIngredient = "Heavy Cream",
                            substituteIngredient = "Greek Yogurt",
                            reason = "Higher protein, lower calorie, adds pleasant tangy probiotic brightness.",
                            ratioNote = "Whisk with 1-2 tbsp warm water on low heat."
                        )
                    ),
                    dietaryTags = listOf("High Protein", "Keto Friendly", "Low Carb"),
                    wasteReductionScore = 14
                )
            )
        }
    }
}

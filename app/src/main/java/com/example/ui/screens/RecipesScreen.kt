package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Recipe
import com.example.data.model.SubstitutionAdvice
import com.example.ui.components.LoadingScreen
import com.example.ui.components.MatchPercentBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.KitchenViewModel

@Composable
fun RecipesScreen(
    viewModel: KitchenViewModel,
    modifier: Modifier = Modifier
) {
    val recipes by viewModel.allRecipes.collectAsState()
    val selectedRecipe by viewModel.selectedRecipe.collectAsState()
    val cuisineFilter by viewModel.recipeCuisineFilter.collectAsState()
    val isGeneratingRecipes by viewModel.isGeneratingRecipes.collectAsState()

    val cuisineOptions = listOf("All", "Mediterranean", "Asian / Stir-Fry", "Quick (<20m)", "High Protein")

    val filteredRecipes = recipes.filter { recipe ->
        when (cuisineFilter) {
            "Mediterranean" -> recipe.cuisine.contains("Mediterranean", ignoreCase = true)
            "Asian / Stir-Fry" -> recipe.cuisine.contains("Asian", ignoreCase = true)
            "Quick (<20m)" -> recipe.totalTimeMin <= 20
            "High Protein" -> recipe.proteinG >= 25
            else -> true
        }
    }

    if (isGeneratingRecipes) {
        LoadingScreen(
            modifier = modifier,
            title = "Chef AI is Cooking Up Recipes...",
            subtitle = "Crafting zero-waste, personalized recipes with your ingredients",
            stepMessages = listOf(
                "Analyzing pantry and fresh produce...",
                "Prioritizing expiring items to minimize food waste...",
                "Matching dietary habits and flavor profiles...",
                "Assembling precise ingredient ratios & steps..."
            )
        )
    } else if (selectedRecipe != null) {
        RecipeDetailView(
            recipe = selectedRecipe!!,
            onBack = { viewModel.clearSelectedRecipe() },
            onStartCooking = {
                val r = selectedRecipe!!
                viewModel.clearSelectedRecipe()
                viewModel.startCookingSession(r)
            }
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("recipes_screen"),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Adaptive Recipes",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Matched dynamically against your ingredients & preferences",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    FilledTonalButton(
                        onClick = { viewModel.generateAiRecipes() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = SageContainer,
                            contentColor = SageGreenPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("generate_recipes_ai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Generate",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Generate", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // 2. Filter Pills
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cuisineOptions) { opt ->
                        val isSelected = cuisineFilter == opt
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setRecipeCuisineFilter(opt) },
                            label = { Text(opt) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageContainer,
                                selectedLabelColor = OnSageContainer
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 3. Recipes List
            if (filteredRecipes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recipes match this filter. Try 'All'.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(filteredRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { viewModel.viewRecipeDetails(recipe) },
                        onStartCooking = { viewModel.startCookingSession(recipe) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onStartCooking: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("recipe_card_${recipe.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${recipe.cuisine} • ${recipe.totalTimeMin} mins • ${recipe.difficulty}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                MatchPercentBadge(percent = recipe.matchPercentage)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 2
            )

            // Macros Row
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacroTag(label = "Calories", value = "${recipe.calories} kcal")
                MacroTag(label = "Protein", value = "${recipe.proteinG}g", highlight = true)
                MacroTag(label = "Carbs", value = "${recipe.carbsG}g")
                MacroTag(label = "Fat", value = "${recipe.fatG}g")
            }

            // Why recommended pills
            if (recipe.whyRecommended.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    recipe.whyRecommended.take(2).forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = FreshGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${recipe.ingredients.size} ingredients • ${recipe.steps.size} steps",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Button(
                    onClick = onStartCooking,
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.OutdoorGrill, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cook", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailView(
    recipe: Recipe,
    onBack: () -> Unit,
    onStartCooking: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.title, maxLines = 1, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    MatchPercentBadge(percent = recipe.matchPercentage, modifier = Modifier.padding(end = 12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Button(
                        onClick = onStartCooking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_cooking_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.OutdoorGrill, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start AI Guided Cooking Session",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Overview & Description
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailStatChip(title = "Prep", value = "${recipe.prepTimeMin}m")
                        DetailStatChip(title = "Cook", value = "${recipe.cookTimeMin}m")
                        DetailStatChip(title = "Total", value = "${recipe.totalTimeMin}m")
                        DetailStatChip(title = "Difficulty", value = recipe.difficulty)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    // Nutrition Breakdown Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SageContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Calories", style = MaterialTheme.typography.labelSmall)
                                Text("${recipe.calories} kcal", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Protein", style = MaterialTheme.typography.labelSmall)
                                Text("${recipe.proteinG}g", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SageGreenPrimary))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Carbs", style = MaterialTheme.typography.labelSmall)
                                Text("${recipe.carbsG}g", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Fat", style = MaterialTheme.typography.labelSmall)
                                Text("${recipe.fatG}g", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // 2. Ingredients & Culinary Chemistry Substitutions
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Ingredients & Inventory Status",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Mealy checks your inventory in real-time",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(recipe.ingredients) { ing ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (ing.isAvailableInInventory) MaterialTheme.colorScheme.surface else AmberContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (ing.isAvailableInInventory) Icons.Default.CheckCircle else Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = if (ing.isAvailableInInventory) FreshGreen else ToastedAmberSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = ing.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                if (ing.substitutionNote != null) {
                                    Text(
                                        text = ing.substitutionNote,
                                        style = MaterialTheme.typography.labelSmall.copy(color = ToastedAmberSecondary)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${ing.amount} ${ing.unit}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // 3. Substitution Intelligence Breakdown
            if (recipe.substitutions.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Culinary Chemistry & Substitutions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
                items(recipe.substitutions) { sub ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AmberContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = ToastedAmberSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${sub.originalIngredient} ➔ ${sub.substituteIngredient}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OnAmberContainer
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sub.reason,
                                style = MaterialTheme.typography.bodySmall.copy(color = OnAmberContainer)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ratio: ${sub.ratioNote}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ToastedAmberSecondary
                                )
                            )
                        }
                    }
                }
            }

            // 4. Step-by-Step Cooking Walkthrough Preview
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Cooking Steps (${recipe.steps.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Voice narration and hands-free timers ready in Cooking Mode",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(recipe.steps) { step ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Step ${step.stepNumber}: ${step.title}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            if (step.durationMinutes > 0) {
                                Surface(
                                    color = SageContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = SageGreenPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "${step.durationMinutes}m timer",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = SageGreenPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = step.instruction,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (!step.chefTip.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "💡 Chef Tip: ${step.chefTip}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SageGreenPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroTag(label: String, value: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (highlight) SageGreenPrimary else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun DetailStatChip(title: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

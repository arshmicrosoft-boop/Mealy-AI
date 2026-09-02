package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.InventoryItem
import com.example.data.model.Recipe
import com.example.data.model.UserPreferences
import com.example.ui.theme.*

@Composable
fun RecipeSuggestionDialog(
    inventory: List<InventoryItem>,
    preferences: UserPreferences,
    isGenerating: Boolean,
    generatedRecipes: List<Recipe>,
    onDismiss: () -> Unit,
    onGenerate: (query: String?) -> Unit,
    onSelectRecipe: (Recipe) -> Unit,
    onStartCooking: (Recipe) -> Unit,
    onToggleFavorite: (Recipe) -> Unit = {}
) {
    var customQueryText by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf("⚡ Quick Weeknight (<20m)") }

    val presets = listOf(
        "⚡ Quick Weeknight (<20m)",
        "♻️ Zero Waste (Expiring Items)",
        "💪 High Protein Focus",
        "🥑 Low Carb / Keto",
        "🌱 Plant-Forward / Vegan",
        "🌶️ Spicy & Warm Comfort"
    )

    val expiringCount = inventory.count { it.isExpiringSoon() }
    val latestGeneratedRecipe = generatedRecipes.firstOrNull()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp)
                .testTag("recipe_suggestion_engine_dialog"),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SageContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = SageGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Recipe Suggestion Engine",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Powered by Gemini AI • Cross-referencing Inventory & Diet",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_suggestion_dialog_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 2. Active Cross-Reference Context Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SageContainer.copy(alpha = 0.45f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SageGreenPrimary.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FactCheck,
                                        contentDescription = null,
                                        tint = SageGreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "LIVE KITCHEN CROSS-REFERENCE CONTEXT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SageGreenPrimary,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ContextChip(
                                        icon = Icons.Default.Inventory2,
                                        label = "${inventory.size} items in stock"
                                    )
                                    if (expiringCount > 0) {
                                        ContextChip(
                                            icon = Icons.Default.WarningAmber,
                                            label = "$expiringCount expiring soon",
                                            isAlert = true
                                        )
                                    }
                                    ContextChip(
                                        icon = Icons.Default.Tune,
                                        label = preferences.dietaryGoal
                                    )
                                }
                            }
                        }
                    }

                    // 3. Inspiration Presets & Prompt Input
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "What are you craving or aiming to cook?",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(presets) { preset ->
                                    val isSelected = selectedPreset == preset
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedPreset = preset
                                            customQueryText = preset
                                        },
                                        label = { Text(preset, style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SageContainer,
                                            selectedLabelColor = OnSageContainer
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customQueryText,
                                onValueChange = { customQueryText = it },
                                placeholder = { Text("e.g. Warm 20-min dinner with my chicken, spinach and rice...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("recipe_suggestion_prompt_input"),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    if (customQueryText.isNotEmpty()) {
                                        IconButton(onClick = { customQueryText = "" }) {
                                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                maxLines = 2
                            )

                            Button(
                                onClick = {
                                    val query = customQueryText.ifBlank { selectedPreset }
                                    onGenerate(query)
                                },
                                enabled = !isGenerating,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("generate_recipe_suggestion_submit_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                            ) {
                                if (isGenerating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Synthesizing Recipe with Gemini AI...")
                                } else {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generate Recipe with Gemini AI", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 4. Generated Recipe Card View
                    if (latestGeneratedRecipe != null) {
                        item {
                            Text(
                                text = "Generated Recipe Card",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        item {
                            GeneratedRecipeCard(
                                recipe = latestGeneratedRecipe,
                                onSelect = { onSelectRecipe(latestGeneratedRecipe) },
                                onStartCooking = { onStartCooking(latestGeneratedRecipe) },
                                onToggleFavorite = { onToggleFavorite(latestGeneratedRecipe) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeneratedRecipeCard(
    recipe: Recipe,
    onSelect: () -> Unit,
    onStartCooking: () -> Unit,
    onToggleFavorite: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("generated_recipe_card_${recipe.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, SageGreenPrimary.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Badge Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SageGreenPrimary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Text(
                            text = "GEMINI AI GENERATED",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.5.sp)
                        )
                    }
                }

                MatchPercentBadge(percent = recipe.matchPercentage)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Meta
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${recipe.cuisine} • ${recipe.totalTimeMin} mins • ${recipe.difficulty}",
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                if (recipe.spiceLevel > 0) {
                    val spiceStr = "🌶️".repeat(recipe.spiceLevel)
                    Text(text = spiceStr, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            // Macros Banner
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MacroItem(label = "Calories", value = "${recipe.calories} kcal")
                    MacroItem(label = "Protein", value = "${recipe.proteinG}g", highlight = true)
                    MacroItem(label = "Carbs", value = "${recipe.carbsG}g")
                    MacroItem(label = "Fat", value = "${recipe.fatG}g")
                }
            }

            // Why Gemini AI Suggested This (Cross-referencing explainability)
            if (recipe.whyRecommended.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = AmberContainer.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = ToastedAmberSecondary, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Cross-Reference Intelligence:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = OnAmberContainer)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        recipe.whyRecommended.forEach { reason ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(vertical = 1.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ToastedAmberSecondary, modifier = Modifier.size(12.dp))
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnAmberContainer, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
            }

            // Cross-referenced Ingredients Summary
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Ingredients Cross-Reference (${recipe.ingredients.count { it.isAvailableInInventory }} of ${recipe.ingredients.size} in pantry):",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                recipe.ingredients.take(5).forEach { ing ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (ing.isAvailableInInventory) Icons.Default.CheckCircle else Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = if (ing.isAvailableInInventory) FreshGreen else ToastedAmberSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = ing.name,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                            if (ing.substitutionNote != null) {
                                Text(
                                    text = "(${ing.substitutionNote})",
                                    style = MaterialTheme.typography.labelSmall.copy(color = ToastedAmberSecondary, fontSize = 10.sp)
                                )
                            }
                        }
                        Text(
                            text = "${ing.amount} ${ing.unit}",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSelect,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("View Full Steps", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = onStartCooking,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("start_cooking_generated_recipe_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.OutdoorGrill, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cook Now", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun MacroItem(label: String, value: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
fun ContextChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isAlert: Boolean = false
) {
    Surface(
        color = if (isAlert) ExpiringRedContainer else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isAlert) ExpiringRed else SageGreenPrimary,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isAlert) ExpiringRed else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

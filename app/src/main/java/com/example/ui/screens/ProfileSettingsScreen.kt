package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserPreferences
import com.example.ui.theme.*
import com.example.ui.viewmodel.KitchenViewModel
import com.example.ui.viewmodel.MainTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    viewModel: KitchenViewModel,
    onNavigateBack: () -> Unit = { viewModel.selectTab(MainTab.PROFILE) },
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.userPreferences.collectAsState()
    var newDislikedIngredient by remember { mutableStateOf("") }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    val spiceOptions = listOf("Mild", "Medium", "Spicy", "Extra Hot")
    val goalOptions = listOf(
        "High Protein & Balanced",
        "Plant-Forward & Clean",
        "Low Carb / Keto",
        "Budget-Friendly Zero-Waste",
        "Quick & Easy Weeknights"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dietary & AI Settings",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Configure Gemini AI Recipe Generation",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            showSavedSnackbar = true
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_settings_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", style = MaterialTheme.typography.labelMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            if (showSavedSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showSavedSnackbar = false }) {
                            Text("OK", color = Color.White)
                        }
                    },
                    containerColor = SageGreenPrimary,
                    contentColor = Color.White
                ) {
                    Text("Dietary preferences updated! Gemini AI will adapt recipes.")
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_settings_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Dietary Preference Toggles Card (Primary Requirement)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dietary_toggles_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SageContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = SageGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Dietary Preferences",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Toggles actively constrain and guide Gemini AI recipes",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 14.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Toggle 1: Vegan
                        DietaryToggleRow(
                            title = "Vegan",
                            description = "100% plant-based. Excludes all meat, poultry, seafood, dairy, eggs, and animal byproducts.",
                            icon = Icons.Default.Spa,
                            isChecked = preferences.isVegan,
                            testTag = "toggle_vegan",
                            onCheckedChange = { checked ->
                                viewModel.updatePreferences(
                                    preferences.copy(
                                        isVegan = checked,
                                        // If vegan is selected, vegetarian is inherently active
                                        isVegetarian = if (checked) true else preferences.isVegetarian
                                    )
                                )
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // Toggle 2: Gluten-Free
                        DietaryToggleRow(
                            title = "Gluten-Free",
                            description = "Excludes wheat, barley, rye, and all gluten-containing grains or sauces.",
                            icon = Icons.Default.Grass,
                            isChecked = preferences.isGlutenFree,
                            testTag = "toggle_gluten_free",
                            onCheckedChange = { checked ->
                                viewModel.updatePreferences(preferences.copy(isGlutenFree = checked))
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // Toggle 3: Spicy Preference
                        DietaryToggleRow(
                            title = "Spicy & Bold Heat",
                            description = "Instructs Gemini AI to incorporate hot peppers, chili oils, and warming aromatic spices.",
                            icon = Icons.Default.LocalFireDepartment,
                            isChecked = preferences.isSpicy,
                            testTag = "toggle_spicy",
                            onCheckedChange = { checked ->
                                viewModel.updatePreferences(
                                    preferences.copy(
                                        isSpicy = checked,
                                        spicePreference = if (checked && preferences.spicePreference == "Mild") "Spicy" else preferences.spicePreference
                                    )
                                )
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // Toggle 4: Dairy-Free
                        DietaryToggleRow(
                            title = "Dairy-Free",
                            description = "Excludes cow's milk, butter, cheese, heavy cream, and lactose.",
                            icon = Icons.Default.LocalDrink,
                            isChecked = preferences.isDairyFree,
                            testTag = "toggle_dairy_free",
                            onCheckedChange = { checked ->
                                viewModel.updatePreferences(preferences.copy(isDairyFree = checked))
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // Toggle 5: Keto / Low-Carb
                        DietaryToggleRow(
                            title = "Keto / Low-Carb",
                            description = "Focus on healthy fats, high quality proteins, and very low carbohydrates (<15g net carbs).",
                            icon = Icons.Default.FitnessCenter,
                            isChecked = preferences.isKeto,
                            testTag = "toggle_keto",
                            onCheckedChange = { checked ->
                                viewModel.updatePreferences(preferences.copy(isKeto = checked))
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // Toggle 6: Vegetarian
                        DietaryToggleRow(
                            title = "Vegetarian",
                            description = "Plant-forward meals without meat or fish; allows eggs and dairy unless toggled off.",
                            icon = Icons.Default.Park,
                            isChecked = preferences.isVegetarian,
                            testTag = "toggle_vegetarian",
                            onCheckedChange = { checked ->
                                viewModel.updatePreferences(preferences.copy(isVegetarian = checked))
                            }
                        )
                    }
                }
            }

            // 2. Spice Level Customizer (When Spicy is toggled)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AmberContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = null,
                                    tint = OnAmberContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Spice Intensity Level",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Current: ${preferences.spicePreference}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SageGreenPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            spiceOptions.forEach { opt ->
                                val isSelected = preferences.spicePreference.equals(opt, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.updatePreferences(
                                            preferences.copy(
                                                spicePreference = opt,
                                                isSpicy = opt != "Mild"
                                            )
                                        )
                                    },
                                    label = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (opt == "Extra Hot" || opt == "Spicy") {
                                                Icon(
                                                    imageVector = Icons.Default.LocalFireDepartment,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = if (isSelected) OnSageContainer else ExpiringRed
                                                )
                                            }
                                            Text(opt)
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SageContainer,
                                        selectedLabelColor = OnSageContainer
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Nutrition & Dietary Goals
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ToastedAmberSecondary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = ToastedAmberSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Primary Dietary Goal",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Used to balance macros and calorie distribution",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            goalOptions.forEach { goal ->
                                val isSelected = preferences.dietaryGoal == goal
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) SageContainer.copy(alpha = 0.4f) else Color.Transparent)
                                        .clickable {
                                            viewModel.updatePreferences(preferences.copy(dietaryGoal = goal))
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.updatePreferences(preferences.copy(dietaryGoal = goal)) },
                                        colors = RadioButtonDefaults.colors(selectedColor = SageGreenPrimary)
                                    )
                                    Text(
                                        text = goal,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Disliked Ingredients & Allergens
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ExpiringRedContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = ExpiringRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Excluded Ingredients & Allergens",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "AI will avoid these items and suggest smart substitutes",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Chip list of excluded ingredients
                        if (preferences.dislikedIngredients.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                preferences.dislikedIngredients.forEach { item ->
                                    InputChip(
                                        selected = true,
                                        onClick = {
                                            viewModel.updatePreferences(
                                                preferences.copy(
                                                    dislikedIngredients = preferences.dislikedIngredients.filter { it != item }
                                                )
                                            )
                                        },
                                        label = { Text(item) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = InputChipDefaults.inputChipColors(
                                            selectedContainerColor = ExpiringRedContainer,
                                            selectedLabelColor = ExpiringRed
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Add new excluded ingredient input field
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newDislikedIngredient,
                                onValueChange = { newDislikedIngredient = it },
                                placeholder = { Text("e.g. Mushrooms, Peanuts, Cilantro") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (newDislikedIngredient.isNotBlank() && !preferences.dislikedIngredients.contains(newDislikedIngredient.trim())) {
                                        viewModel.updatePreferences(
                                            preferences.copy(
                                                dislikedIngredients = preferences.dislikedIngredients + newDislikedIngredient.trim()
                                            )
                                        )
                                        newDislikedIngredient = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SageGreenPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Disliked",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 5. Gemini AI Prompt Transparency Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SageContainer)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Gemini AI Synthesis Preview",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnSageContainer
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Every recipe generation query is conditioned on these active parameters:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OnSageContainer.copy(alpha = 0.85f)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "• Vegan: ${if (preferences.isVegan) "YES (Strict 100% plant-based)" else "No"}\n" +
                                           "• Gluten-Free: ${if (preferences.isGlutenFree) "YES (Strict zero-gluten)" else "No"}\n" +
                                           "• Spicy: ${if (preferences.isSpicy) "YES (${preferences.spicePreference})" else "Mild"}\n" +
                                           "• Dairy-Free: ${if (preferences.isDairyFree) "YES" else "No"}\n" +
                                           "• Goal: ${preferences.dietaryGoal}\n" +
                                           "• Excluded: ${if (preferences.dislikedIngredients.isEmpty()) "None" else preferences.dislikedIngredients.joinToString()}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = OnWarmSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 6. Action Button to Save & Generate
            item {
                Button(
                    onClick = {
                        showSavedSnackbar = true
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("apply_preferences_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apply & Update AI Recommendations",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DietaryToggleRow(
    title: String,
    description: String,
    icon: ImageVector,
    isChecked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isChecked) SageGreenPrimary else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(22.dp)
                    .padding(top = 2.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isChecked) SageGreenPrimary else MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SageGreenPrimary,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

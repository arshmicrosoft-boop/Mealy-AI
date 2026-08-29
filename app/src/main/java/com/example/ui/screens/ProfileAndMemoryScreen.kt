package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.KitchenMemoryLog
import com.example.data.model.UserPreferences
import com.example.ui.theme.*
import com.example.ui.viewmodel.KitchenViewModel

@Composable
fun ProfileAndMemoryScreen(
    viewModel: KitchenViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val preferences by viewModel.userPreferences.collectAsState()
    val memories by viewModel.kitchenMemories.collectAsState()
    val cookingSessions by viewModel.cookingSessions.collectAsState()
    var showAddMemoryDialog by remember { mutableStateOf(false) }

    val spiceOptions = listOf("Mild", "Medium", "Spicy", "Extra Hot")
    val goalOptions = listOf("High Protein & Balanced", "Low Carb", "Keto", "Plant-Forward", "Quick Weekday Meals")
    val cuisineOptions = listOf("Italian", "Asian Stir-Fry", "Mediterranean", "Mexican", "American", "Middle Eastern")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("profile_memory_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Header & User Account Card
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "Kitchen Intelligence & Profile",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Your personal chef identity and taste memory",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // User Account Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SageGreenPrimary,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (!userProfile.photoUrl.isNullOrBlank()) {
                                    coil.compose.AsyncImage(
                                        model = userProfile.photoUrl,
                                        contentDescription = "User Avatar",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = userProfile.displayName.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile.displayName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (!userProfile.isAnonymous) {
                                    Surface(
                                        color = SageContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "GOOGLE",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = SageGreenPrimary
                                            )
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (userProfile.email.isNotBlank()) userProfile.email else "Guest Kitchen Mode",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.signOut() },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sign Out", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // 2. Kitchen Memory Engine Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SageContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = SageGreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Learned Memory Logs (${memories.size})",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OnSageContainer
                                    )
                                )
                                Text(
                                    text = "Dynamic vector-style kitchen preferences",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = OnSageContainer.copy(alpha = 0.8f)
                                    )
                                )
                            }
                        }
                        IconButton(onClick = { showAddMemoryDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Add Memory",
                                tint = SageGreenPrimary
                            )
                        }
                    }
                }
            }
        }

        items(memories) { mem ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = AmberContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = mem.category,
                                color = OnAmberContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = mem.insight,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "➔ ${mem.actionTaken}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SageGreenPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // 3. Preferences Configuration Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Taste & Cooking Preferences",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Fine-tune Mealy's recipe generation parameters",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Spice Level Selector
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Spice Level Preference",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        spiceOptions.forEach { opt ->
                            val isSelected = preferences.spicePreference == opt
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updatePreferences(preferences.copy(spicePreference = opt)) },
                                label = { Text(opt) },
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

        // Dietary Goal Selector
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dietary & Nutrition Goal",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        goalOptions.forEach { goal ->
                            val isSelected = preferences.dietaryGoal == goal
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updatePreferences(preferences.copy(dietaryGoal = goal)) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

        // Cooking Time Slider
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Target Max Cooking Time",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${preferences.maxCookingTimeMin} mins",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SageGreenPrimary
                            )
                        )
                    }
                    Slider(
                        value = preferences.maxCookingTimeMin.toFloat(),
                        onValueChange = { viewModel.updatePreferences(preferences.copy(maxCookingTimeMin = it.toInt())) },
                        valueRange = 15f..60f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = SageGreenPrimary,
                            activeTrackColor = SageGreenPrimary
                        )
                    )
                }
            }
        }

        // Hands-Free TTS Voice Audio Settings
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hands-Free Voice Narration (TTS)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-read cooking steps aloud")
                        Switch(
                            checked = preferences.autoVoiceOnStepChange,
                            onCheckedChange = { viewModel.updatePreferences(preferences.copy(autoVoiceOnStepChange = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = SageGreenPrimary)
                        )
                    }
                }
            }
        }
    }

    if (showAddMemoryDialog) {
        AddMemoryDialog(
            onDismiss = { showAddMemoryDialog = false },
            onAdd = { insight, category, action ->
                viewModel.addKitchenMemory(insight, category, action)
                showAddMemoryDialog = false
            }
        )
    }
}

@Composable
fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onAdd: (insight: String, category: String, action: String) -> Unit
) {
    var insight by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("TASTE_PREFERENCE") }
    var action by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Kitchen Memory", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = insight,
                    onValueChange = { insight = it },
                    label = { Text("What did Mealio learn?") },
                    placeholder = { Text("e.g. Loves crispy golden chicken skin") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = action,
                    onValueChange = { action = it },
                    label = { Text("How AI should adapt recipes") },
                    placeholder = { Text("e.g. Prioritize high-heat pan searing") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (insight.isNotBlank()) {
                        onAdd(insight, category, if (action.isBlank()) "Adapted in future meal plans" else action)
                    }
                },
                enabled = insight.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
            ) {
                Text("Save Memory")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

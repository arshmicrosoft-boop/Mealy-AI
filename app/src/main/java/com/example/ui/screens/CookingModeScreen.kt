package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Recipe
import com.example.ui.theme.*
import com.example.ui.viewmodel.KitchenViewModel
import com.example.ui.viewmodel.TimerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeScreen(
    viewModel: KitchenViewModel,
    modifier: Modifier = Modifier
) {
    val recipe by viewModel.activeCookingRecipe.collectAsState()
    val stepIndex by viewModel.currentCookingStepIndex.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    var showCompleteDialog by remember { mutableStateOf(false) }

    if (recipe == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.OutdoorGrill,
                    contentDescription = null,
                    tint = SageGreenPrimary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Active Cooking Session",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select any recipe and tap 'Start AI Guided Cooking' to begin.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val activeRecipe = recipe!!
    val currentStep = activeRecipe.steps.getOrNull(stepIndex) ?: return
    val totalSteps = activeRecipe.steps.size
    val isLastStep = stepIndex == totalSteps - 1

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("cooking_mode_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = activeRecipe.title,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Step ${stepIndex + 1} of $totalSteps",
                            style = MaterialTheme.typography.labelSmall.copy(color = SageGreenPrimary)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.exitCookingSession() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.speakCurrentStep() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Read Step Aloud",
                            tint = SageGreenPrimary
                        )
                    }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (stepIndex > 0) {
                        OutlinedButton(
                            onClick = { viewModel.previousCookingStep() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Previous", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Button(
                        onClick = {
                            if (isLastStep) {
                                showCompleteDialog = true
                            } else {
                                viewModel.nextCookingStep()
                            }
                        },
                        modifier = Modifier
                            .weight(if (stepIndex > 0) 1.5f else 1f)
                            .height(52.dp)
                            .testTag("cooking_next_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLastStep) ToastedAmberSecondary else SageGreenPrimary
                        )
                    ) {
                        Text(
                            text = if (isLastStep) "Finish Cooking" else "Next Step",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (isLastStep) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Step Progress Indicator
            LinearProgressIndicator(
                progress = { (stepIndex + 1).toFloat() / totalSteps },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SageGreenPrimary,
                trackColor = SageContainer
            )

            // 2. Large High-Contrast Step Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = SageContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "STEP ${currentStep.stepNumber} OF $totalSteps",
                                color = SageGreenPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.speakCurrentStep() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Voice Guide",
                                tint = SageGreenPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = currentStep.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentStep.instruction,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 17.sp,
                            lineHeight = 26.sp
                        )
                    )

                    if (currentStep.ingredientsNeeded.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ingredients for this step:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            currentStep.ingredientsNeeded.forEach { ing ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "• $ing",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Hands-Free Interactive Kitchen Timer (if duration > 0)
            if (currentStep.durationMinutes > 0 || timerState.totalSeconds > 0) {
                CookingTimerCard(
                    timerState = timerState,
                    onToggle = { viewModel.toggleTimer() },
                    onReset = { viewModel.resetTimer() }
                )
            }

            // 4. Chef Chemistry & Food Science Insight Card
            if (!currentStep.chefTip.isNullOrBlank() || !currentStep.scienceExplanation.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = ToastedAmberSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Chef Science & Chemistry Tip",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnAmberContainer
                                )
                            )
                        }

                        if (!currentStep.chefTip.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "💡 Tip: ${currentStep.chefTip}",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnAmberContainer)
                            )
                        }

                        if (!currentStep.scienceExplanation.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🔬 Food Science: ${currentStep.scienceExplanation}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OnAmberContainer,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCompleteDialog) {
        CompleteCookingDialog(
            recipeTitle = activeRecipe.title,
            onDismiss = { showCompleteDialog = false },
            onFinish = { rating, notes ->
                showCompleteDialog = false
                viewModel.finishCookingSession(rating, notes)
            }
        )
    }
}

@Composable
fun CookingTimerCard(
    timerState: TimerState,
    onToggle: () -> Unit,
    onReset: () -> Unit
) {
    val minutes = timerState.remainingSeconds / 60
    val seconds = timerState.remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cooking_timer_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (timerState.isRunning) SageGreenPrimary else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (timerState.isRunning) Icons.Default.HourglassTop else Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (timerState.isRunning) Color.White else SageGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                    )
                    Text(
                        text = if (timerState.isFinished) "Time's up!" else if (timerState.isRunning) "Timer running..." else "Step timer",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (timerState.isFinished) ExpiringRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onReset) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = "Reset Timer")
                }

                Button(
                    onClick = onToggle,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (timerState.isRunning) ToastedAmberSecondary else SageGreenPrimary
                    )
                ) {
                    Text(if (timerState.isRunning) "Pause" else "Start")
                }
            }
        }
    }
}

@Composable
fun CompleteCookingDialog(
    recipeTitle: String,
    onDismiss: () -> Unit,
    onFinish: (rating: Int, notes: String) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var notes by remember { mutableStateOf("Turned out amazing! Perfect flavor balance.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Cooking Session Completed!", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "How did '$recipeTitle' turn out? Mealy will log this to your personal taste memory.",
                    style = MaterialTheme.typography.bodyMedium
                )

                // 5 Star Rating Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$i Stars",
                                tint = ToastedAmberSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Tasting Note / Feedback") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onFinish(rating, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
            ) {
                Text("Save to Memory & Finish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

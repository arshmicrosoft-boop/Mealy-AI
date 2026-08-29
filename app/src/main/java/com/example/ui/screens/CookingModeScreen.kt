package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Recipe
import com.example.data.model.RecipeStep
import com.example.ui.theme.*
import com.example.ui.viewmodel.KitchenViewModel
import com.example.ui.viewmodel.TimerState
import com.example.util.rememberAudioPermissionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeScreen(
    viewModel: KitchenViewModel,
    modifier: Modifier = Modifier
) {
    val recipe by viewModel.activeCookingRecipe.collectAsState()
    val stepIndex by viewModel.currentCookingStepIndex.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val speechState by viewModel.speechState.collectAsState()
    val userPrefs by viewModel.userPreferences.collectAsState()
    val voiceFeedback by viewModel.voiceFeedbackMessage.collectAsState()
    val isVoiceAiProcessing by viewModel.isVoiceAiProcessing.collectAsState()
    val aiVoiceResponse by viewModel.aiVoiceResponse.collectAsState()
    val completionRequested by viewModel.showCompletionDialogRequested.collectAsState()

    var showCompleteDialog by remember { mutableStateOf(false) }
    var showManualAskDialog by remember { mutableStateOf(false) }

    LaunchedEffect(completionRequested) {
        if (completionRequested) {
            showCompleteDialog = true
        }
    }

    val audioPermissionState = rememberAudioPermissionState { isGranted ->
        if (isGranted) {
            viewModel.startVoiceListening()
        }
    }

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
                    text = "Select any recipe and tap 'Start AI Guided Cooking' to begin with hands-free interactive voice.",
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Step ${stepIndex + 1} of $totalSteps",
                                style = MaterialTheme.typography.labelSmall.copy(color = SageGreenPrimary, fontWeight = FontWeight.Bold)
                            )
                            if (userPrefs.autoVoiceOnStepChange) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = SageContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "AUTO-VOICE ON",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SageGreenPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.exitCookingSession() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Cooking Mode")
                    }
                },
                actions = {
                    // Auto-voice toggle
                    IconButton(
                        onClick = {
                            viewModel.updatePreferences(userPrefs.copy(autoVoiceOnStepChange = !userPrefs.autoVoiceOnStepChange))
                        }
                    ) {
                        Icon(
                            imageVector = if (userPrefs.autoVoiceOnStepChange) Icons.Default.Hearing else Icons.Default.HearingDisabled,
                            contentDescription = "Toggle Hands-Free Auto Voice",
                            tint = if (userPrefs.autoVoiceOnStepChange) SageGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Read Step Aloud
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Navigation Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (stepIndex > 0) {
                            OutlinedButton(
                                onClick = { viewModel.previousCookingStep() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
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
                                .height(48.dp)
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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

            // 2. Interactive Hands-Free Voice Assistant Controller
            InteractiveVoiceAssistantCard(
                isListening = speechState.isListening,
                rmsLevel = speechState.rmsLevel,
                partialText = speechState.partialText,
                feedbackMessage = voiceFeedback,
                isAiProcessing = isVoiceAiProcessing,
                onMicClick = {
                    if (!audioPermissionState.hasPermission) {
                        audioPermissionState.requestPermission()
                    } else {
                        viewModel.toggleVoiceListening()
                    }
                },
                onQuickCommand = { command ->
                    viewModel.processVoiceCommand(command)
                },
                onAskCustomQuestion = {
                    showManualAskDialog = true
                }
            )

            // 3. AI Voice Response Card (if present)
            if (!aiVoiceResponse.isNullOrBlank()) {
                AiChefVoiceResponseCard(
                    response = aiVoiceResponse!!,
                    onReplay = { viewModel.ttsHelper.speak(aiVoiceResponse!!) },
                    onDismiss = { viewModel.clearAiVoiceResponse() }
                )
            }

            // 4. Large High-Contrast Step Instruction Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentStep.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentStep.instruction,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 17.sp,
                            lineHeight = 26.sp
                        )
                    )

                    if (currentStep.ingredientsNeeded.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Ingredients for this step:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            currentStep.ingredientsNeeded.forEach { ing ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
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

            // 5. Interactive Kitchen Timer
            if (currentStep.durationMinutes > 0 || timerState.totalSeconds > 0) {
                CookingTimerCard(
                    timerState = timerState,
                    onToggle = { viewModel.toggleTimer() },
                    onReset = { viewModel.resetTimer() }
                )
            }

            // 6. Chef Chemistry & Food Science Insight Card
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
            onDismiss = {
                showCompleteDialog = false
                viewModel.dismissCompletionDialog()
            },
            onFinish = { rating, notes ->
                showCompleteDialog = false
                viewModel.dismissCompletionDialog()
                viewModel.finishCookingSession(rating, notes)
            }
        )
    }

    if (showManualAskDialog) {
        AskChefVoiceDialog(
            onDismiss = { showManualAskDialog = false },
            onSubmit = { query ->
                showManualAskDialog = false
                viewModel.processVoiceCommand(query)
            }
        )
    }
}

@Composable
fun InteractiveVoiceAssistantCard(
    isListening: Boolean,
    rmsLevel: Float,
    partialText: String,
    feedbackMessage: String?,
    isAiProcessing: Boolean,
    onMicClick: () -> Unit,
    onQuickCommand: (String) -> Unit,
    onAskCustomQuestion: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("interactive_voice_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isListening) SageContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isListening) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(SageGreenPrimary.copy(alpha = 0.25f))
                            )
                        }

                        IconButton(
                            onClick = onMicClick,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isListening) ExpiringRed else SageGreenPrimary)
                                .testTag("cooking_mic_button")
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                                contentDescription = if (isListening) "Stop Listening" else "Start Voice Commands",
                                tint = Color.White
                            )
                        }
                    }

                    Column {
                        Text(
                            text = if (isListening) "🎙️ Listening to hands-free voice..." else "Hands-Free Voice Assistant",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isListening) {
                                if (partialText.isNotBlank()) "\"$partialText...\"" else "Say 'Next', 'Timer', or ask any question"
                            } else {
                                "Tap mic & speak without touching screen"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isListening) SageGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                if (isAiProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = SageGreenPrimary
                    )
                }
            }

            if (feedbackMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = feedbackMessage,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, color = SageGreenPrimary)
                    )
                }
            }

            // Quick Voice Command Action Chips
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoiceCommandChip(label = "🗣️ \"Next Step\"", onClick = { onQuickCommand("next step") })
                VoiceCommandChip(label = "🗣️ \"Repeat Step\"", onClick = { onQuickCommand("repeat step") })
                VoiceCommandChip(label = "🗣️ \"Start Timer\"", onClick = { onQuickCommand("start timer") })
                VoiceCommandChip(label = "🗣️ \"Ingredients\"", onClick = { onQuickCommand("what ingredients") })
                VoiceCommandChip(label = "🗣️ \"Chef Tip\"", onClick = { onQuickCommand("chef tip") })
                VoiceCommandChip(label = "🗣️ \"Pause Timer\"", onClick = { onQuickCommand("pause timer") })
                VoiceCommandChip(label = "💬 Ask Chef Question", onClick = onAskCustomQuestion)
            }
        }
    }
}

@Composable
fun VoiceCommandChip(label: String, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
fun AiChefVoiceResponseCard(
    response: String,
    onReplay: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_voice_response_card"),
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = SageGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Chef AI Voice Assistant",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnSageContainer
                        )
                    )
                }

                Row {
                    IconButton(onClick = onReplay, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Replay Answer",
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = response,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = OnSageContainer,
                    lineHeight = 22.sp
                )
            )
        }
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
                    text = "How did '$recipeTitle' turn out? Mealio will log this to your personal taste memory.",
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

@Composable
fun AskChefVoiceDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Ask Chef AI During Cooking", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ask any substitution, cooking temperature, or culinary technique question:",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("e.g. Can I use butter instead of olive oil?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onSubmit(text)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
            ) {
                Text("Ask Chef")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

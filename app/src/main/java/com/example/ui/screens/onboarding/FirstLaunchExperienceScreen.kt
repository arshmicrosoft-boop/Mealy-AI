package com.example.ui.screens.onboarding

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserPreferences
import com.example.ui.theme.*
import com.example.ui.viewmodel.KitchenViewModel
import kotlinx.coroutines.delay

enum class OnboardingStage {
    CINEMATIC_OPENING, // Screen 0: 0-3s
    SIGN_IN_CHOICE,    // Screen 1: 3-8s
    KITCHEN_STYLE,     // Screen 2 Step 1: 8-13s
    PRIORITIES,        // Screen 2 Step 2: 13-18s
    COOKING_HABITS,    // Screen 2 Step 3: 18-23s
    PREFERENCES,       // Screen 2 Step 4: 23-27s
    AI_SYNTHESIS       // Screen 3: 27-30s
}

@Composable
fun FirstLaunchExperienceScreen(
    viewModel: KitchenViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isAuthenticating by viewModel.isAuthenticating.collectAsState()
    val authMessage by viewModel.authStateMessage.collectAsState()

    var stage by remember { mutableStateOf(OnboardingStage.CINEMATIC_OPENING) }

    // User Selection States
    var selectedKitchenLook by remember { mutableStateOf("💪 Fitness focused") }
    var dynamicAiResponse by remember { mutableStateOf("Great. I'll prioritize protein-rich meals and efficient preparation.") }
    val selectedPriorities = remember { mutableStateListOf("♻️ Reduce food waste", "💪 Eat healthier") }
    var selectedCookingSpeed by remember { mutableStateOf("⚡ Under 15 minutes") }
    var selectedDiet by remember { mutableStateOf("No restrictions") }
    var selectedCuisine by remember { mutableStateOf("Mixed") }
    val selectedAllergies = remember { mutableStateListOf<String>() }

    // Auto-advance or user-advance cinematic opening after 3 seconds
    LaunchedEffect(Unit) {
        delay(3200)
        if (stage == OnboardingStage.CINEMATIC_OPENING) {
            stage = OnboardingStage.SIGN_IN_CHOICE
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1412))
            .windowInsetsPadding(WindowInsets.systemBars)
            .testTag("first_launch_experience_screen")
    ) {
        // Futuristic ambient background glow
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-40).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SageGreenPrimary.copy(alpha = 0.28f),
                            Color(0xFF1E3A2F).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        AnimatedContent(
            targetState = stage,
            transitionSpec = {
                (fadeIn(animationSpec = tween(400)) + slideInHorizontally { width -> width / 3 })
                    .togetherWith(fadeOut(animationSpec = tween(300)) + slideOutHorizontally { width -> -width / 3 })
            },
            label = "onboarding_stage_transition"
        ) { currentStage ->
            when (currentStage) {
                OnboardingStage.CINEMATIC_OPENING -> {
                    CinematicOpeningScreen(
                        onContinue = { stage = OnboardingStage.SIGN_IN_CHOICE }
                    )
                }
                OnboardingStage.SIGN_IN_CHOICE -> {
                    SignInChoiceScreen(
                        isAuthenticating = isAuthenticating,
                        authMessage = authMessage,
                        onGoogleSignIn = {
                            if (activity != null) {
                                viewModel.signInWithGoogle(activity)
                            } else {
                                viewModel.continueAsGuest("Google User")
                            }
                            stage = OnboardingStage.KITCHEN_STYLE
                        },
                        onDemoKitchen = {
                            viewModel.continueAsGuest("Arsh (Chef Demo)")
                            stage = OnboardingStage.KITCHEN_STYLE
                        }
                    )
                }
                OnboardingStage.KITCHEN_STYLE -> {
                    KitchenStyleStepScreen(
                        selectedOption = selectedKitchenLook,
                        aiResponseText = dynamicAiResponse,
                        onSelect = { option, response ->
                            selectedKitchenLook = option
                            dynamicAiResponse = response
                        },
                        onNext = { stage = OnboardingStage.PRIORITIES },
                        onSkip = { stage = OnboardingStage.PRIORITIES }
                    )
                }
                OnboardingStage.PRIORITIES -> {
                    PrioritiesStepScreen(
                        selectedPriorities = selectedPriorities,
                        onToggle = { priority ->
                            if (selectedPriorities.contains(priority)) {
                                selectedPriorities.remove(priority)
                            } else {
                                selectedPriorities.add(priority)
                            }
                        },
                        onNext = { stage = OnboardingStage.COOKING_HABITS },
                        onSkip = { stage = OnboardingStage.COOKING_HABITS }
                    )
                }
                OnboardingStage.COOKING_HABITS -> {
                    CookingHabitsStepScreen(
                        selectedHabit = selectedCookingSpeed,
                        onSelect = { selectedCookingSpeed = it },
                        onNext = { stage = OnboardingStage.PREFERENCES },
                        onSkip = { stage = OnboardingStage.PREFERENCES }
                    )
                }
                OnboardingStage.PREFERENCES -> {
                    QuickPreferencesStepScreen(
                        selectedDiet = selectedDiet,
                        onDietSelect = { selectedDiet = it },
                        selectedCuisine = selectedCuisine,
                        onCuisineSelect = { selectedCuisine = it },
                        selectedAllergies = selectedAllergies,
                        onAllergyToggle = { allergy ->
                            if (selectedAllergies.contains(allergy)) {
                                selectedAllergies.remove(allergy)
                            } else {
                                selectedAllergies.add(allergy)
                            }
                        },
                        onNext = { stage = OnboardingStage.AI_SYNTHESIS },
                        onSkip = { stage = OnboardingStage.AI_SYNTHESIS }
                    )
                }
                OnboardingStage.AI_SYNTHESIS -> {
                    AiSynthesisScreen(
                        kitchenLook = selectedKitchenLook,
                        priorities = selectedPriorities,
                        cookingSpeed = selectedCookingSpeed,
                        diet = selectedDiet,
                        onEnterKitchen = {
                            // Save configured profile into preferences
                            val maxMins = when (selectedCookingSpeed) {
                                "⚡ Under 15 minutes" -> 15
                                "🍳 15-30 minutes" -> 30
                                "📅 Meal prep" -> 45
                                else -> 40
                            }
                            val updatedPrefs = UserPreferences(
                                favoriteCuisines = listOf(selectedCuisine, "Mediterranean"),
                                isVegan = selectedDiet.contains("Vegan", ignoreCase = true),
                                isVegetarian = selectedDiet.contains("Vegetarian", ignoreCase = true),
                                isKeto = selectedDiet.contains("Keto", ignoreCase = true),
                                dietaryGoal = when {
                                    selectedKitchenLook.contains("Fitness") -> "High Protein & Performance"
                                    selectedPriorities.contains("♻️ Reduce food waste") -> "Zero Waste & Budget Friendly"
                                    else -> "Balanced & Quick"
                                },
                                maxCookingTimeMin = maxMins,
                                allergies = selectedAllergies.toList()
                            )
                            viewModel.updatePreferences(updatedPrefs)
                            viewModel.addKitchenMemory(
                                insight = "Prefers $selectedKitchenLook with focus on ${selectedPriorities.joinToString()}",
                                category = "TASTE",
                                actionTaken = "Tailoring recipe suggestions and zero-waste priorities"
                            )
                        }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN 0: CINEMATIC WELCOME (0-3s)
// -------------------------------------------------------------
@Composable
private fun CinematicOpeningScreen(
    onContinue: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    var phaseTextIndex by remember { mutableStateOf(0) }
    val phases = listOf("Ingredient", "Recipe", "Meal", "AI Intelligence")

    LaunchedEffect(Unit) {
        for (i in phases.indices) {
            phaseTextIndex = i
            delay(700)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Center Glowing Orb Transformation
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
            ) {
                // Outer glow aura
                Surface(
                    shape = CircleShape,
                    color = SageGreenPrimary.copy(alpha = 0.25f),
                    modifier = Modifier.size(150.dp)
                ) {}
                Surface(
                    shape = CircleShape,
                    color = SageGreenPrimary.copy(alpha = 0.55f),
                    modifier = Modifier.size(110.dp)
                ) {}
                Surface(
                    shape = CircleShape,
                    color = SageGreenPrimary,
                    modifier = Modifier.size(80.dp),
                    shadowElevation = 16.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
            }

            // Morphing Particle Label
            AnimatedContent(
                targetState = phases[phaseTextIndex],
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) + slideInVertically { it / 2 } togetherWith
                            fadeOut(animationSpec = tween(200)) + slideOutVertically { -it / 2 }
                },
                label = "particle_phase_text"
            ) { phase ->
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SageGreenPrimary.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "✨ $phase",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Meet your AI kitchen companion.",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Meal planning, cooking, and food intelligence in one place.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.75f),
                    lineHeight = 22.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("cinematic_welcome_continue_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

// -------------------------------------------------------------
// SCREEN 1: SIGN IN (3-8s)
// -------------------------------------------------------------
@Composable
private fun SignInChoiceScreen(
    isAuthenticating: Boolean,
    authMessage: String?,
    onGoogleSignIn: () -> Unit,
    onDemoKitchen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            // Glowing mini AI orb
            Surface(
                shape = CircleShape,
                color = SageGreenPrimary,
                modifier = Modifier.size(68.dp),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Let's personalize your kitchen.",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign in to sync your inventory & continuous taste memory, or explore the live demo instantly.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (authMessage != null) {
                Surface(
                    color = SageGreenPrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = authMessage,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 🟦 Continue with Google
            Button(
                onClick = onGoogleSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("onboarding_google_sign_in_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                enabled = !isAuthenticating
            ) {
                if (isAuthenticating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = SageGreenPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connecting...", color = Color.Black, fontWeight = FontWeight.Bold)
                } else {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF1F3F4),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        color = Color(0xFF1F1F1F),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // 👤 Try Demo Kitchen
            OutlinedButton(
                onClick = onDemoKitchen,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_try_demo_button"),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SageGreenPrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(imageVector = Icons.Default.PersonOutline, contentDescription = null, tint = SageGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "👤 Try Demo Kitchen",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Text(
                text = "Instant 1-tap activation • No forms or credit card required.",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.5f)),
                textAlign = TextAlign.Center
            )
        }
    }
}

// -------------------------------------------------------------
// SCREEN 2 STEP 1: KITCHEN LOOK (8-13s)
// -------------------------------------------------------------
@Composable
private fun KitchenStyleStepScreen(
    selectedOption: String,
    aiResponseText: String,
    onSelect: (String, String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val options = listOf(
        "🏠 Cooking for myself" to "Perfect. I'll calibrate single-portion recipes with zero leftover waste.",
        "👨‍👩‍👧 Family meals" to "Great! I'll focus on balanced, crowd-pleasing batch recipes.",
        "💪 Fitness focused" to "Great. I'll prioritize protein-rich meals and efficient preparation.",
        "⚡ Quick meals" to "Got it. I'll prioritize rapid 15-minute one-pan meals.",
        "🍳 Learning cooking" to "Exciting! I'll provide detailed culinary science tips and step timers."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP 1 OF 4",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SageGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                TextButton(onClick = onSkip) {
                    Text("Skip for now", color = Color.White.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "What does your kitchen look like?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                options.forEach { (title, response) ->
                    val isSelected = selectedOption == title
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(title, response) }
                            .testTag("kitchen_look_option_${title.filter { it.isLetter() }}"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) SageContainer.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) SageGreenPrimary else Color.White.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f)
                                )
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SageGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Dynamic Mealio Live Response
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(
                visible = aiResponseText.isNotBlank(),
                enter = fadeIn() + expandVertically()
            ) {
                Surface(
                    color = Color(0xFF1B2E24),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SageGreenPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "\"$aiResponseText\"",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.95f),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
        ) {
            Text("Next", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

// -------------------------------------------------------------
// SCREEN 2 STEP 2: PRIORITIES (13-18s)
// -------------------------------------------------------------
@Composable
private fun PrioritiesStepScreen(
    selectedPriorities: List<String>,
    onToggle: (String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val priorities = listOf(
        "♻️ Reduce food waste",
        "💪 Eat healthier",
        "⏱ Cook faster",
        "💰 Save money",
        "👨‍🍳 Improve cooking skills"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP 2 OF 4",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SageGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                TextButton(onClick = onSkip) {
                    Text("Skip for now", color = Color.White.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "What matters most to you?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "Select all that apply to tailor zero-waste suggestions.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.65f))
            )

            Spacer(modifier = Modifier.height(18.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                priorities.forEach { priority ->
                    val isSelected = selectedPriorities.contains(priority)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(priority) }
                            .testTag("priority_toggle_${priority.filter { it.isLetter() }}"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) SageContainer.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) SageGreenPrimary else Color.White.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = priority,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f)
                                )
                            )
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onToggle(priority) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = SageGreenPrimary,
                                    uncheckedColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }

            // Real-time Intelligence Profile Tag
            if (selectedPriorities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Kitchen Intelligence Profile:",
                            style = MaterialTheme.typography.labelSmall.copy(color = SageGreenPrimary, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedPriorities.joinToString(" • ") { it.substringAfter(" ") },
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
        ) {
            Text("Next", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

// -------------------------------------------------------------
// SCREEN 2 STEP 3: COOKING HABITS (18-23s)
// -------------------------------------------------------------
@Composable
private fun CookingHabitsStepScreen(
    selectedHabit: String,
    onSelect: (String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val habits = listOf(
        "⚡ Under 15 minutes",
        "🍳 15-30 minutes",
        "👨‍🍳 I enjoy cooking",
        "📅 Meal prep"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP 3 OF 4",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SageGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                TextButton(onClick = onSkip) {
                    Text("Skip for now", color = Color.White.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "How do you usually cook?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "This immediately calibrates step difficulty and prep times.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.65f))
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                habits.forEach { habit ->
                    val isSelected = selectedHabit == habit
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(habit) }
                            .testTag("habit_option_${habit.filter { it.isLetter() }}"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) SageContainer.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) SageGreenPrimary else Color.White.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = habit,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f)
                                )
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SageGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
        ) {
            Text("Next", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

// -------------------------------------------------------------
// SCREEN 2 STEP 4: PREFERENCES & CUISINE (23-27s)
// -------------------------------------------------------------
@Composable
private fun QuickPreferencesStepScreen(
    selectedDiet: String,
    onDietSelect: (String) -> Unit,
    selectedCuisine: String,
    onCuisineSelect: (String) -> Unit,
    selectedAllergies: List<String>,
    onAllergyToggle: (String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val diets = listOf("No restrictions", "Vegetarian", "Vegan", "Keto", "High Protein")
    val cuisines = listOf("Mixed", "Indian", "Asian", "Italian", "Mexican")
    val allergyOptions = listOf("Nuts", "Dairy", "Gluten", "Shellfish")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP 4 OF 4",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SageGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                TextButton(onClick = onSkip) {
                    Text("Skip for now", color = Color.White.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Any preferences?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Diet Selection
            Text(
                text = "Diet:",
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(diets) { diet ->
                    val isSelected = selectedDiet == diet
                    FilterChip(
                        selected = isSelected,
                        onClick = { onDietSelect(diet) },
                        label = { Text(diet) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreenPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.08f),
                            labelColor = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Cuisine Selection
            Text(
                text = "Favorite Cuisine:",
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cuisines) { cuisine ->
                    val isSelected = selectedCuisine == cuisine
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCuisineSelect(cuisine) },
                        label = { Text(cuisine) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreenPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.08f),
                            labelColor = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Allergies Selection
            Text(
                text = "Allergies to Avoid (Optional):",
                style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allergyOptions.forEach { allergy ->
                    val isSelected = selectedAllergies.contains(allergy)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onAllergyToggle(allergy) },
                        label = { Text(allergy) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ExpiringRed,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.08f),
                            labelColor = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
        ) {
            Text("Complete Setup", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
        }
    }
}

// -------------------------------------------------------------
// SCREEN 3: AI CREATES KITCHEN PROFILE (27-30s)
// -------------------------------------------------------------
@Composable
private fun AiSynthesisScreen(
    kitchenLook: String,
    priorities: List<String>,
    cookingSpeed: String,
    diet: String,
    onEnterKitchen: () -> Unit
) {
    var check1 by remember { mutableStateOf(false) }
    var check2 by remember { mutableStateOf(false) }
    var check3 by remember { mutableStateOf(false) }
    var check4 by remember { mutableStateOf(false) }
    var isReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(350)
        check1 = true
        delay(350)
        check2 = true
        delay(350)
        check3 = true
        delay(350)
        check4 = true
        delay(400)
        isReady = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Glowing pulsing orb
            Surface(
                shape = CircleShape,
                color = SageGreenPrimary,
                modifier = Modifier.size(76.dp),
                shadowElevation = 12.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Text(
                text = "Understanding your kitchen...",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Checklist
            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AiSynthesisCheckItem(label = "Cooking style", isDone = check1)
                AiSynthesisCheckItem(label = "Nutrition goals", isDone = check2)
                AiSynthesisCheckItem(label = "Time preference", isDone = check3)
                AiSynthesisCheckItem(label = "Taste profile", isDone = check4)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Personalized Ready Statement
            AnimatedVisibility(
                visible = isReady,
                enter = fadeIn(animationSpec = tween(500)) + expandVertically()
            ) {
                Surface(
                    color = Color(0xFF1E3A2F),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SageGreenPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 Your Mealio is ready.",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "I'll help you create quick high-protein meals while reducing food waste.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 20.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Button(
            onClick = onEnterKitchen,
            enabled = isReady,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("enter_my_kitchen_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
        ) {
            Text(
                text = "Enter My Kitchen",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun AiSynthesisCheckItem(
    label: String,
    isDone: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = if (isDone) SageGreenPrimary else Color.White.copy(alpha = 0.15f),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                color = if (isDone) Color.White else Color.White.copy(alpha = 0.5f)
            )
        )
    }
}

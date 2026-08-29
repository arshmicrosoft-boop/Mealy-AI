package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Reusable LoadingScreen component that provides a clear user interface feedback loop
 * during AI queries, recipe generation, and vision processing API calls.
 */
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    title: String = "Chef AI is Working...",
    subtitle: String = "Generating intelligent recipe recommendations based on your ingredients",
    stepMessages: List<String> = listOf(
        "Scanning available inventory items...",
        "Identifying expiring ingredients to minimize food waste...",
        "Applying your flavor and dietary preferences...",
        "Crafting step-by-step culinary instructions..."
    ),
    onCancel: (() -> Unit)? = null
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }

    // Step message cycle timer
    LaunchedEffect(stepMessages) {
        if (stepMessages.isNotEmpty()) {
            while (true) {
                delay(2400)
                currentStepIndex = (currentStepIndex + 1) % stepMessages.size
            }
        }
    }

    // Gentle pulse animation for the central icon halo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("loading_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pulsing AI Icon Hub
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                // Outer glowing halo
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    SageGreenPrimary.copy(alpha = haloAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Central circle container
                Surface(
                    shape = CircleShape,
                    color = SageContainer,
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(70.dp),
                            color = SageGreenPrimary,
                            strokeWidth = 3.dp,
                            trackColor = SageGreenPrimary.copy(alpha = 0.15f)
                        )
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Processing",
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Primary Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("loading_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Description
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Dynamic Step Feedback Loop Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loading_feedback_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = SageGreenPrimary,
                            strokeWidth = 2.dp
                        )
                        AnimatedContent(
                            targetState = stepMessages.getOrElse(currentStepIndex) { "Processing your request..." },
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith
                                        fadeOut(animationSpec = tween(200))
                            },
                            label = "step_text_transition"
                        ) { message ->
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }

            // Optional Cancel Button
            if (onCancel != null) {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("loading_cancel_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/**
 * Reusable dialog/overlay version of the Loading feedback loop.
 */
@Composable
fun LoadingOverlay(
    title: String = "Processing...",
    subtitle: String = "Communicating with AI services",
    onDismissRequest: (() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxSize()
            .testTag("loading_overlay")
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight()
            ) {
                LoadingScreen(
                    title = title,
                    subtitle = subtitle,
                    onCancel = onDismissRequest,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}

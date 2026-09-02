package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ChefBadge
import com.example.data.model.DailyQuest
import com.example.data.model.KitchenPsychologyState
import com.example.data.model.MysteryChefDrop
import com.example.ui.theme.*

/**
 * 1. THE STREAK & LOSS AVERSION EFFECT (Kahneman & Tversky / Hooked Model)
 * Keeps users returning daily because losing a streak hurts 2x more than building one.
 */
@Composable
fun StreakProtectionCard(
    state: KitchenPsychologyState,
    onProtectStreak: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flamePulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("streak_protection_card"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ToastedAmberSecondary.copy(alpha = 0.35f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AmberContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔥",
                            fontSize = 24.sp,
                            modifier = Modifier.scale(flameScale)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "${state.streakDays}-Day Kitchen Streak",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Surface(
                                color = AmberContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Active 🔥",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OnAmberContainer
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Cook or log 1 food item today to keep streak alive!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Loss Aversion Shield Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Streak Shield",
                        tint = if (state.streakShieldsRemaining > 0) SageGreenPrimary else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (state.streakShieldsRemaining > 0) "${state.streakShieldsRemaining} Freeze Shield Active" else "No Shields Left",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                if (state.streakShieldsRemaining > 0) {
                    FilledTonalButton(
                        onClick = onProtectStreak,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("protect_streak_button")
                    ) {
                        Text("Equip Shield", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

/**
 * 2. VARIABLE REWARD SCHEDULE (B.F. Skinner / Hooked Model - Nir Eyal)
 * Daily Chef Mystery Drop creates unpredictable dopamine surges and daily retention.
 */
@Composable
fun MysteryChefDropBanner(
    drop: MysteryChefDrop,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mysteryGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenClick)
            .testTag("mystery_drop_banner"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1B2820),
        border = BorderStroke(1.5.dp, SageGreenLight.copy(alpha = glowAlpha)),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(ToastedAmberLight, PaprikaTertiary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (drop.isOpened) "✨" else "🎁",
                    fontSize = 26.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (drop.isOpened) "Mystery Unlocked!" else "Daily Chef Mystery Drop",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Surface(
                        color = PaprikaTertiary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "+${drop.bonusXp} XP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = if (drop.isOpened) drop.title else "Tap to unbox today's secret flavor hack & rare recipe!",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.85f)
                    ),
                    maxLines = 1
                )
            }

            Button(
                onClick = onOpenClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (drop.isOpened) SageGreenPrimary else ToastedAmberSecondary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (drop.isOpened) "View" else "Open",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

/**
 * Mystery Drop Reveal Dialog
 */
@Composable
fun MysteryChefDropDialog(
    drop: MysteryChefDrop,
    onClaim: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, ToastedAmberSecondary),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎁",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Today's Culinary Secret",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = drop.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SageGreenPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = ToastedAmberSecondary, modifier = Modifier.size(18.dp))
                            Text("Flavor Science Hack", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = drop.flavorHack,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = SageContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("✨", fontSize = 20.sp)
                        Column {
                            Text("Surprise Pairing Dish", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = OnSageContainer))
                            Text(drop.surpriseDish, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = SageGreenDark))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        onClaim()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("claim_mystery_reward_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
                ) {
                    Text(
                        text = if (drop.isOpened) "Awesome!" else "Claim +${drop.bonusXp} XP Reward",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }
            }
        }
    }
}

/**
 * 3. ENDOWED PROGRESS & ZEIGARNIK EFFECT (Nunes & Drèze)
 * Starting users endowed at 84/100 Kitchen IQ + open quest loops drives high completion tension.
 */
@Composable
fun KitchenIqAndQuestsCard(
    state: KitchenPsychologyState,
    onQuestClick: (DailyQuest) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("kitchen_iq_quests_card"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineBorder),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Kitchen IQ Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Kitchen IQ Score",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            color = FreshGreenContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Top 5% Eco Chef 🏆",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = FreshGreen),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Endowed progress: Complete today's quests to reach 90+!",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                // Circular IQ Badge
                Surface(
                    color = SageGreenPrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${state.kitchenIqScore}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { state.kitchenIqScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = SageGreenPrimary,
                trackColor = SageContainer.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "DAILY KITCHEN QUESTS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quest List
            state.dailyQuests.forEach { quest ->
                QuestItemRow(quest = quest, onClick = { onQuestClick(quest) })
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun QuestItemRow(
    quest: DailyQuest,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (quest.isCompleted) SageContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, if (quest.isCompleted) SageGreenLight.copy(alpha = 0.4f) else OutlineBorder.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = quest.iconEmoji, fontSize = 20.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (quest.isCompleted) SageGreenDark else MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = quest.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            if (quest.isCompleted) {
                Surface(
                    color = SageGreenPrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(16.dp)
                    )
                }
            } else {
                Surface(
                    color = AmberContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "+${quest.xpReward} XP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OnAmberContainer
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * 4. TANGIBLE FINANCIAL & ECO REWARD LOOP (Immediate Gratification)
 * Shows real dollars saved and carbon rescued to satisfy the brain's reward center.
 */
@Composable
fun FinancialSavingsTrackerCard(
    state: KitchenPsychologyState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("financial_savings_tracker_card"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineBorder),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Your Kitchen Impact",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "This Month",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Money Saved Metric
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = FreshGreenContainer.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, FreshGreen.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "💰 Money Saved", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SageGreenDark))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("$%.2f", state.totalMoneySavedDollars),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = FreshGreen
                            )
                        )
                        Text(text = "vs. grocery waste", style = MaterialTheme.typography.labelSmall.copy(color = SageGreenDark.copy(alpha = 0.7f), fontSize = 10.sp))
                    }
                }

                // Food Rescued Metric
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = AmberContainer.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, ToastedAmberSecondary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "🌍 Food Rescued", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = OnAmberContainer))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format("%.1f", state.totalFoodRescuedKg)} kg",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = ToastedAmberSecondary
                            )
                        )
                        Text(text = "${state.mealsCookedTotal} meals cooked", style = MaterialTheme.typography.labelSmall.copy(color = OnAmberContainer.copy(alpha = 0.7f), fontSize = 10.sp))
                    }
                }
            }
        }
    }
}

/**
 * 5. GAMIFIED IDENTITY & MASTER HIERARCHY (Self-Determination Theory)
 * Chef Mastery Tier & Badges showcase.
 */
@Composable
fun ChefMasteryLevelCard(
    state: KitchenPsychologyState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chef_mastery_level_card"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineBorder),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = state.chefTierName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "${state.currentXp} / ${state.xpForNextLevel} XP to Level ${state.chefTierLevel + 1}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Surface(
                    color = ToastedAmberSecondary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Lvl ${state.chefTierLevel}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // XP Bar
            LinearProgressIndicator(
                progress = { (state.currentXp.toFloat() / state.xpForNextLevel).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = ToastedAmberSecondary,
                trackColor = AmberContainer
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "UNLOCKED CHEF BADGES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.badges.take(4).forEach { badge ->
                    BadgeChip(badge = badge, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(badge: ChefBadge, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (badge.isUnlocked) WarmSurfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, if (badge.isUnlocked) ToastedAmberSecondary.copy(alpha = 0.4f) else OutlineBorder)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = badge.iconEmoji,
                fontSize = 22.sp,
                modifier = Modifier.alpha(if (badge.isUnlocked) 1f else 0.4f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = badge.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ),
                maxLines = 1
            )
        }
    }
}

/**
 * 6. THE IKEA EFFECT & SUNK COST INVESTMENT (Norton, Mochon, Ariely)
 * Highlights user's custom-built Taste DNA profile making the app irreplaceable.
 */
@Composable
fun TasteDnaInvestmentCard(
    state: KitchenPsychologyState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("taste_dna_investment_card"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, OutlineBorder),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(SageContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("🧬", fontSize = 22.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Your Taste DNA: ${state.tasteDnaUniquePercentage}% Unique",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
                Text(
                    text = "Mealio has learned 14 flavor memories & custom pantry rules. Your palate is calibrated.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
    }
}

/**
 * 7. SOCIAL PROOF & SCARCITY URGENCY
 * City rank + Expiring urgency triggers quick action.
 */
@Composable
fun SocialProofUrgencyCard(
    state: KitchenPsychologyState,
    onRescueExpiringClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("social_proof_urgency_card"),
        shape = RoundedCornerShape(14.dp),
        color = ExpiringRedContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, ExpiringRed.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("⏳", fontSize = 20.sp)
                Column {
                    Text(
                        text = "1 item expiring in 18 hours ($3.99 value)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = ExpiringRed
                        )
                    )
                    Text(
                        text = "Top 5% Eco Kitchens rescue items within 24 hours.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Button(
                onClick = onRescueExpiringClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ExpiringRed),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Rescue", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
            }
        }
    }
}

/**
 * POPPING DOPAMINE CELEBRATION BANNER
 */
@Composable
fun DopamineCelebrationBanner(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (message != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable(onClick = onDismiss)
                    .testTag("dopamine_celebration_banner"),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E3A2F),
                border = BorderStroke(1.5.dp, SageGreenLight),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

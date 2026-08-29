package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodState
import com.example.data.model.IngredientCategory
import com.example.ui.theme.*

@Composable
fun FoodStateBadge(state: FoodState, modifier: Modifier = Modifier) {
    val (bg, textColor, icon) = when (state) {
        FoodState.BOUGHT -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), Icons.Default.ShoppingCart)
        FoodState.STORED -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), Icons.Default.Inventory2)
        FoodState.OPENED -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Icons.Default.LockOpen)
        FoodState.EXPIRING -> Triple(ExpiringRedContainer, ExpiringRed, Icons.Default.WarningAmber)
        FoodState.USED -> Triple(Color(0xFFF3F4F6), Color(0xFF4B5563), Icons.Default.CheckCircle)
        FoodState.DISCARDED -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), Icons.Default.DeleteOutline)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = state.label,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
            )
        }
    }
}

@Composable
fun ExpiryUrgencyChip(daysLeft: Int, modifier: Modifier = Modifier) {
    val (bg, text, textColor) = when {
        daysLeft < 0 -> Triple(Color(0xFFFEE2E2), "Expired ${-daysLeft}d ago", ExpiringRed)
        daysLeft == 0 -> Triple(ExpiringRedContainer, "Expires Today!", ExpiringRed)
        daysLeft == 1 -> Triple(ExpiringRedContainer, "Expires Tomorrow!", ExpiringRed)
        daysLeft <= 3 -> Triple(AmberContainer, "$daysLeft days left", ToastedAmberSecondary)
        else -> Triple(Color(0xFFF0FDF4), "$daysLeft days fresh", FreshGreen)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = if (daysLeft <= 2) Icons.Default.Timer else Icons.Default.Check,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
            )
        }
    }
}

@Composable
fun MatchPercentBadge(percent: Int, modifier: Modifier = Modifier) {
    val color = when {
        percent >= 90 -> SageGreenPrimary
        percent >= 80 -> ToastedAmberSecondary
        else -> Color(0xFF6B7280)
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "$percent% Match",
                color = color,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun CategoryIcon(category: IngredientCategory, modifier: Modifier = Modifier, tint: Color = SageGreenPrimary) {
    val icon = when (category) {
        IngredientCategory.PRODUCE -> Icons.Default.Eco
        IngredientCategory.DAIRY_EGGS -> Icons.Default.Egg
        IngredientCategory.PROTEINS -> Icons.Default.Restaurant
        IngredientCategory.PANTRY_GRAINS -> Icons.Default.Grain
        IngredientCategory.CONDIMENTS_SPICES -> Icons.Default.LocalFlorist
        IngredientCategory.BAKERY -> Icons.Default.BreakfastDining
        IngredientCategory.BEVERAGES -> Icons.Default.LocalCafe
        IngredientCategory.FROZEN -> Icons.Default.AcUnit
        IngredientCategory.OTHER -> Icons.Default.Kitchen
    }
    Icon(
        imageVector = icon,
        contentDescription = category.label,
        tint = tint,
        modifier = modifier
    )
}

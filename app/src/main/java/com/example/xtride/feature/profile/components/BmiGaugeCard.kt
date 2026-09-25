package com.example.xtride.feature.profile.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xtride.domain.model.BmiCategory
import java.util.Locale

@Composable
fun BmiGaugeCard(
    bmiScore: Float,
    bmiCategory: BmiCategory,
    heightCm: Float,
    healthyWeightMinKg: Float,
    healthyWeightMaxKg: Float,
    modifier: Modifier = Modifier
) {
    val categoryColor = when (bmiCategory) {
        BmiCategory.UNDERWEIGHT -> Color(0xFF38BDF8)
        BmiCategory.NORMAL -> Color(0xFF10B981)
        BmiCategory.OVERWEIGHT -> Color(0xFFF59E0B)
        BmiCategory.OBESE -> Color(0xFFE11D48)
    }

    val animatedColor by animateColorAsState(
        targetValue = categoryColor,
        label = "bmiCategoryColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0A0F1D))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row: Title & Category Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Body Mass Index (BMI)",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(animatedColor.copy(alpha = 0.15f))
                        .border(1.dp, animatedColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = bmiCategory.label,
                        color = animatedColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Big BMI Score Row
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%.1f", bmiScore),
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "kg/m²",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Segmented BMI Range Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(18.5f)
                        .fillMaxHeight()
                        .background(if (bmiCategory == BmiCategory.UNDERWEIGHT) Color(0xFF38BDF8) else Color(0xFF1E293B))
                )
                Box(
                    modifier = Modifier
                        .weight(6.5f)
                        .fillMaxHeight()
                        .background(if (bmiCategory == BmiCategory.NORMAL) Color(0xFF10B981) else Color(0xFF1E293B))
                )
                Box(
                    modifier = Modifier
                        .weight(5f)
                        .fillMaxHeight()
                        .background(if (bmiCategory == BmiCategory.OVERWEIGHT) Color(0xFFF59E0B) else Color(0xFF1E293B))
                )
                Box(
                    modifier = Modifier
                        .weight(10f)
                        .fillMaxHeight()
                        .background(if (bmiCategory == BmiCategory.OBESE) Color(0xFFE11D48) else Color(0xFF1E293B))
                )
            }

            // Healthy Range Callout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Healthy weight for ${heightCm.toInt()} cm:",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp
                )
                Text(
                    text = "${healthyWeightMinKg} – ${healthyWeightMaxKg} kg",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

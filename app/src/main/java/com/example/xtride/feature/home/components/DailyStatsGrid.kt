package com.example.xtride.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DailyStatsGrid(
    distanceKm: Float,
    caloriesBurned: Int,
    activeMinutes: Int,
    stepsRemaining: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            label = "DISTANCE",
            value = String.format(Locale.US, "%.2f", distanceKm),
            unit = "km",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "CALORIES",
            value = caloriesBurned.toString(),
            unit = "kcal",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "ACTIVE",
            value = activeMinutes.toString(),
            unit = "mins",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "REMAINING",
            value = NumberFormat.getNumberInstance(Locale.US).format(stepsRemaining.coerceAtLeast(0)),
            unit = "steps",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A)) // Dark slate-900 surface
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = unit,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE11D48)
            )
        }
    }
}
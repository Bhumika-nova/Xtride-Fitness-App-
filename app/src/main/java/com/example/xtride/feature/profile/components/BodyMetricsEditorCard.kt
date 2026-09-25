package com.example.xtride.feature.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun BodyMetricsEditorCard(
    heightCm: Float,
    weightKg: Float,
    age: Int,
    gender: String,
    dailyStepGoal: Int,
    isSaving: Boolean,
    onHeightChange: (Float) -> Unit,
    onWeightChange: (Float) -> Unit,
    onAgeChange: (Int) -> Unit,
    onGenderChange: (String) -> Unit,
    onStepGoalChange: (Int) -> Unit,
    onSaveProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0A0F1D))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Body & Target Metrics",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        // Height Stepper Row
        MetricStepperRow(
            label = "Height",
            valueText = "${heightCm.toInt()} cm",
            onDecrement = { onHeightChange((heightCm - 1f).coerceAtLeast(100f)) },
            onIncrement = { onHeightChange((heightCm + 1f).coerceAtMost(250f)) },
            quickDeltas = listOf(-5 to "-5", 5 to "+5"),
            onQuickDelta = { delta -> onHeightChange((heightCm + delta).coerceIn(100f, 250f)) }
        )

        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

        // Weight Stepper Row
        MetricStepperRow(
            label = "Weight",
            valueText = String.format(Locale.US, "%.1f kg", weightKg),
            onDecrement = { onWeightChange((weightKg - 0.5f).coerceAtLeast(30f)) },
            onIncrement = { onWeightChange((weightKg + 0.5f).coerceAtMost(250f)) },
            quickDeltas = listOf(-2 to "-2", 2 to "+2"),
            onQuickDelta = { delta -> onWeightChange((weightKg + delta).coerceIn(30f, 250f)) }
        )

        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

        // Age Stepper Row
        MetricStepperRow(
            label = "Age",
            valueText = "$age yrs",
            onDecrement = { onAgeChange((age - 1).coerceAtLeast(12)) },
            onIncrement = { onAgeChange((age + 1).coerceAtMost(100)) },
            quickDeltas = listOf(-5 to "-5", 5 to "+5"),
            onQuickDelta = { delta -> onAgeChange((age + delta).coerceIn(12, 100)) }
        )

        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

        // Gender Selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Gender",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Female", "Male", "Other").forEach { option ->
                    val isSelected = gender.equals(option, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFFE11D48) else Color(0xFF0F172A))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFFE11D48) else Color(0xFF1E293B),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onGenderChange(option) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

        // Step Goal Quick Presets
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Daily Step Goal",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5000, 6000, 8000, 10000).forEach { goal ->
                    val isSelected = dailyStepGoal == goal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFFE11D48) else Color(0xFF0F172A))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFFE11D48) else Color(0xFF1E293B),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onStepGoalChange(goal) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${goal / 1000}k",
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Save Button
        Button(
            onClick = onSaveProfile,
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE11D48),
                disabledContainerColor = Color(0xFFE11D48).copy(alpha = 0.5f)
            )
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Profile Changes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun MetricStepperRow(
    label: String,
    valueText: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    quickDeltas: List<Pair<Int, String>>,
    onQuickDelta: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, color = Color(0xFF94A3B8), fontSize = 13.sp)
            Text(text = valueText, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickDeltas.forEach { (delta, tag) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                        .clickable { onQuickDelta(delta) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(text = tag, color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .clickable(onClick = onDecrement),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White, modifier = Modifier.size(16.dp))
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE11D48))
                    .clickable(onClick = onIncrement),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

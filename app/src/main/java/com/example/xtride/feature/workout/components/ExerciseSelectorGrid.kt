package com.example.xtride.feature.workout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseSelectorGrid(
    selectedExercise: String,
    customExerciseName: String,
    onExerciseSelected: (String) -> Unit,
    onCustomNameChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        "Bench Press",
        "Squats",
        "Deadlift",
        "Push-ups",
        "Pull-ups",
        "Dumbbell Curls",
        "Shoulder Press",
        "Custom"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "SELECT EXERCISE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Flow layout of chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { exercise ->
                val isSelected = selectedExercise == exercise
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) Color(0xFFE11D48) else Color(0xFF0F172A))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFFFB7185) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onExerciseSelected(exercise) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = exercise,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Custom Exercise Input field if "Custom" is active
        if (selectedExercise == "Custom") {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFFE11D48).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (customExerciseName.isEmpty()) {
                    Text(
                        text = "Enter custom exercise name...",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = customExerciseName,
                    onValueChange = onCustomNameChanged,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(Color(0xFFE11D48)),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
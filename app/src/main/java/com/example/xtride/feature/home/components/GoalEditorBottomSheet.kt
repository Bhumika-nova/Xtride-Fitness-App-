package com.example.xtride.feature.home.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditorBottomSheet(
    currentGoal: Int,
    onGoalUpdated: (Int) -> Unit,
    onResetSteps: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedGoal by remember { mutableIntStateOf(currentGoal) }
    var customGoalText by remember { mutableStateOf(currentGoal.toString()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A), // Dark slate container
        scrimColor = Color.Black.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Adjust Daily Step Target ",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Choose a preset or type a customized target.",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Preset Goal Chips
            val presets = listOf(5000, 6000, 8000, 10000, 12000)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = selectedGoal == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFE11D48) else Color(0xFF1E293B))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFFB7185) else Color(0xFF334155),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedGoal = preset
                                customGoalText = preset.toString()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${preset / 1000}k",
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Custom Input Box
            Text(
                text = "CUSTOM STEP GOAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = customGoalText,
                    onValueChange = {
                        customGoalText = it.filter { ch -> ch.isDigit() }.take(6)
                        it.toIntOrNull()?.let { num -> selectedGoal = num }
                    },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(Color(0xFFE11D48)),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Goal CTA Button
            Button(
                onClick = {
                    val finalGoal = customGoalText.toIntOrNull() ?: selectedGoal
                    if (finalGoal in 1000..50000) {
                        onGoalUpdated(finalGoal)
                    }
                    onDismiss()
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Update Goal",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            if (onResetSteps != null) {
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = {
                        onResetSteps()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Reset Today's Steps to 0 (Recalibrate)",
                        color = Color(0xFFFB7185),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
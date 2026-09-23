package com.example.xtride.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingSetupScreen(
    viewModel: AuthViewModel,
    onComplete: () -> Unit
) {
    var heightText by remember { mutableStateOf("168") }
    var weightText by remember { mutableStateOf("64.5") }
    var ageText by remember { mutableStateOf("25") }
    var selectedGoal by remember { mutableIntStateOf(6000) }
    var selectedGender by remember { mutableStateOf("Female") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.OnboardingComplete) {
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040711)), // Deep OLED obsidian
        contentAlignment = Alignment.TopCenter
    ) {
        // Ambient Crimson Red Glow at the top
        Box(
            modifier = Modifier
                .size(360.dp)
                .blur(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE11D48).copy(alpha = 0.22f),
                            Color(0xFFE11D48).copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Step Indicator Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE11D48).copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "STEP 2 OF 2",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFB7185),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Personalize Profile 🎯",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Calibrate your daily step goals and body metrics.",
                fontSize = 14.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // Height & Weight row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CustomNeonField(
                        label = "Height (cm)",
                        value = heightText,
                        onValueChange = { heightText = it },
                        placeholder = "168",
                        keyboardType = KeyboardType.Number
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    CustomNeonField(
                        label = "Weight (kg)",
                        value = weightText,
                        onValueChange = { weightText = it },
                        placeholder = "64.5",
                        keyboardType = KeyboardType.Decimal
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Age Field
            CustomNeonField(
                label = "Age (Years)",
                value = ageText,
                onValueChange = { ageText = it },
                placeholder = "25",
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Gender Selector
            Text(
                text = "Gender",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

            val genderOptions = listOf("Female", "Male", "Other")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                genderOptions.forEach { gender ->
                    val isSelected = selectedGender == gender
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0xFFE11D48) else Color(0xFF131826))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFFB7185) else Color(0xFF232D42),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedGender = gender },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = gender,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Daily Step Goal Selection
            Text(
                text = "Daily Step Goal",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

            val goalOptions = listOf(5000, 6000, 8000, 10000)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                goalOptions.forEach { goal ->
                    val isSelected = selectedGoal == goal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0xFFE11D48) else Color(0xFF131826))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFFB7185) else Color(0xFF232D42),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedGoal = goal },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${goal / 1000}k",
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Submit Button
            Button(
                onClick = {
                    val height = heightText.toFloatOrNull() ?: 168f
                    val weight = weightText.toFloatOrNull() ?: 64.5f
                    val age = ageText.toIntOrNull() ?: 25
                    viewModel.saveOnboardingProfile(height, weight, age, selectedGender, selectedGoal)
                },
                enabled = uiState !is AuthUiState.Loading,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE11D48)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "Complete Setup & Open xtride →",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CustomNeonField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF131826))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFFE11D48).copy(alpha = 0.4f),
                            Color(0xFF232D42),
                            Color(0xFFE11D48).copy(alpha = 0.4f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color(0xFF64748B),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush = SolidColor(Color(0xFFE11D48)),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
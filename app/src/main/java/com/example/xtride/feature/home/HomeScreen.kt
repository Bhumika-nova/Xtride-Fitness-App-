package com.example.xtride.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xtride.feature.home.components.ActivityHeatmap
import com.example.xtride.feature.home.components.CircularStepMeter
import com.example.xtride.feature.home.components.DailyStatsGrid
import com.example.xtride.feature.home.components.GoalEditorBottomSheet

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showGoalSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040711)) // Obsidian Dark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Header: xtride Brand Logo & Profile Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // xtride Wordmark Logo
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFFE11D48),
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic,
                                fontSize = 34.sp
                            )
                        ) {
                            append("x")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic,
                                fontSize = 34.sp
                            )
                        ) {
                            append("tride")
                        }
                    }
                )

                // Athlete Monogram Avatar (Directs to Profile page when clicked)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE11D48))
                        .clickable { onNavigateToProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.userName.take(1).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Circular Step Progress Meter
            CircularStepMeter(
                steps = uiState.todaySteps,
                goal = uiState.dailyGoal,
                progress = uiState.progressPercentage,
                percentDone = uiState.percentDoneInt,
                onGoalClick = { showGoalSheet = true }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 3. Daily Stats Quad-Grid (Distance, Calories, Active, Remaining)
            DailyStatsGrid(
                distanceKm = uiState.distanceKm,
                caloriesBurned = uiState.caloriesBurned,
                activeMinutes = uiState.activeMinutes,
                stepsRemaining = uiState.dailyGoal - uiState.todaySteps
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. 35-Day Consistency Heatmap Grid
            ActivityHeatmap(
                days = uiState.heatmapDays,
                streak = uiState.streak
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 5. Goal Editor Bottom Sheet
        if (showGoalSheet) {
            GoalEditorBottomSheet(
                currentGoal = uiState.dailyGoal,
                onGoalUpdated = { newGoal ->
                    viewModel.updateDailyGoal(newGoal)
                },
                onResetSteps = {
                    viewModel.resetTodaySteps()
                },
                onDismiss = { showGoalSheet = false }
            )
        }
    }
}
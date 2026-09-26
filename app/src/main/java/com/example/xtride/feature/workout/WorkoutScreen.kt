package com.example.xtride.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xtride.feature.workout.components.ExerciseSelectorGrid
import com.example.xtride.feature.workout.components.ValueStepperCard
import com.example.xtride.feature.workout.components.WorkoutHistoryList
import com.example.xtride.feature.workout.components.WorkoutTabSelector
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040711)) // Obsidian Dark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Header
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Strength & Training",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Workout Tracker",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Segmented Tab Selector [ Log Workout | History ]
            WorkoutTabSelector(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Tab Content
            when (uiState.selectedTab) {
                WorkoutTab.LOG -> {
                    // Log Workout Form
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Exercise Preset Chips
                        ExerciseSelectorGrid(
                            selectedExercise = uiState.selectedExercise,
                            customExerciseName = uiState.customExerciseName,
                            onExerciseSelected = { viewModel.selectExercise(it) },
                            onCustomNameChanged = { viewModel.updateCustomExerciseName(it) }
                        )

                        // Weight Stepper
                        ValueStepperCard(
                            label = "WEIGHT",
                            valueText = uiState.weightKg.toString(),
                            unit = "kg",
                            onDecrement = { viewModel.adjustWeight(-2.5f) },
                            onIncrement = { viewModel.adjustWeight(2.5f) },
                            quickDeltas = listOf("-5", "-2.5", "+2.5", "+5"),
                            onQuickDeltaClick = { viewModel.adjustWeight(it) }
                        )

                        // Reps Stepper
                        ValueStepperCard(
                            label = "REPETITIONS",
                            valueText = uiState.reps.toString(),
                            unit = "reps",
                            onDecrement = { viewModel.adjustReps(-1) },
                            onIncrement = { viewModel.adjustReps(1) },
                            quickDeltas = listOf("-5", "-1", "+1", "+5"),
                            onQuickDeltaClick = { viewModel.adjustReps(it.toInt()) }
                        )

                        // Sets Stepper
                        ValueStepperCard(
                            label = "SETS",
                            valueText = uiState.sets.toString(),
                            unit = "sets",
                            onDecrement = { viewModel.adjustSets(-1) },
                            onIncrement = { viewModel.adjustSets(1) }
                        )

                        // Volume Preview Banner
                        val formattedVolume = NumberFormat.getNumberInstance(Locale.US).format(uiState.totalVolumeKg.toInt())
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F172A))
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Estimated Volume: $formattedVolume kg",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFB7185)
                            )
                        }

                        // Save Workout Primary Crimson Pill Button
                        Button(
                            onClick = { viewModel.saveCurrentWorkout() },
                            enabled = !uiState.isSaving,
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Save Workout",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                WorkoutTab.HISTORY -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        WorkoutHistoryList(
                            historyList = uiState.historyList,
                            onDeleteWorkout = { viewModel.deleteWorkout(it) }
                        )
                    }
                }
            }
        }

        // Floating Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}
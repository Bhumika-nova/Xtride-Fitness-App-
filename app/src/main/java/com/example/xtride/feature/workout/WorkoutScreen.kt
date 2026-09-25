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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF040711) // Obsidian Dark
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF040711))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top Brand Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFFE11D48),
                                    fontWeight = FontWeight.Black,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 32.sp
                                )
                            ) {
                                append("x")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 32.sp
                                )
                            ) {
                                append("tride")
                            }
                        }
                    )

                    Text(
                        text = "WORKOUT TRACKER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE11D48),
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Segmented Tab Selector [ Log Workout | History ]
                WorkoutTabSelector(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Tab Content
                when (uiState.selectedTab) {
                    WorkoutTab.LOG -> {
                        // Log Workout Form
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
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

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    WorkoutTab.HISTORY -> {
                        // History List
                        WorkoutHistoryList(
                            historyList = uiState.historyList,
                            onDeleteWorkout = { viewModel.deleteWorkout(it) }
                        )
                    }
                }
            }
        }
    }
}
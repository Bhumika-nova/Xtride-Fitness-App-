package com.example.xtride.feature.workout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.xtride.feature.workout.WorkoutTab

@Composable
fun WorkoutTabSelector(
    selectedTab: WorkoutTab,
    onTabSelected: (WorkoutTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(Color(0xFF0F172A)) // Slate-900 elevated surface
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // "Log Workout" Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (selectedTab == WorkoutTab.LOG) Color(0xFFE11D48) else Color.Transparent)
                    .clickable { onTabSelected(WorkoutTab.LOG) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Log Workout",
                    color = if (selectedTab == WorkoutTab.LOG) Color.White else Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // "History" Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (selectedTab == WorkoutTab.HISTORY) Color(0xFFE11D48) else Color.Transparent)
                    .clickable { onTabSelected(WorkoutTab.HISTORY) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "History",
                    color = if (selectedTab == WorkoutTab.HISTORY) Color.White else Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

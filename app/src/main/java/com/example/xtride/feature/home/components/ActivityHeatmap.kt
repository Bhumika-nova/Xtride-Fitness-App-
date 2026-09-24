package com.example.xtride.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xtride.domain.model.StreakData
import com.example.xtride.feature.home.HeatmapDay

@Composable
fun ActivityHeatmap(
    days: List<HeatmapDay>,
    streak: StreakData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F172A)) 
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header Row: Title & Streak Flame
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HABIT TRACKER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Activity Matrix",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                // Streak Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE11D48).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFE11D48).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFFB7185),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${streak.currentStreakDays} Day Streak",
                            color = Color(0xFFFB7185),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7 Columns x 5 Rows Grid (5 Weeks of 7 Days)
            val weeks = days.chunked(7)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weeks.forEach { week ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            week.forEach { day ->
                                HeatmapSquare(level = day.level, size = 20)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Less",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(end = 4.dp)
                )
                (0..4).forEach { lvl ->
                    HeatmapSquare(level = lvl, size = 10)
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = "More",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun HeatmapSquare(level: Int, size: Int = 18) {
    val color = when (level) {
        4 -> Color(0xFFE11D48) // Athletic Crimson (Goal Met)
        3 -> Color(0xFFBE123C) // Level 3
        2 -> Color(0xFF881337) // Level 2
        1 -> Color(0xFF4C0519) // Level 1
        else -> Color(0xFF1E293B) // Level 0 (Rested / Inactive)
    }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
    )
}
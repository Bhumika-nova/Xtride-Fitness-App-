package com.example.xtride.feature.analytics.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xtride.feature.analytics.DailyStatItem
import com.example.xtride.feature.analytics.TimeRange
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnalyticsBarChart(
    timeRange: TimeRange,
    dailyStats: List<DailyStatItem>,
    selectedIndex: Int?,
    onBarSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormatter = NumberFormat.getNumberInstance(Locale.US)
    val maxSteps = (dailyStats.maxOfOrNull { it.stepsCount } ?: 6000).coerceAtLeast(6000)

    val activeStat = selectedIndex?.let { dailyStats.getOrNull(it) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0A0F1D))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        // Dynamic Callout Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = activeStat?.date ?: (if (timeRange == TimeRange.WEEK) "Past 7 Days" else "Past 30 Days"),
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val displaySteps = activeStat?.stepsCount ?: dailyStats.sumOf { it.stepsCount }
                    val label = if (activeStat != null) "steps" else "total steps"
                    Text(
                        text = "${numberFormatter.format(displaySteps)} $label",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (activeStat != null && activeStat.isGoalMet) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Goal Met",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Target baseline pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Goal: ${numberFormatter.format(dailyStats.firstOrNull()?.targetGoal ?: 6000)}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Chart Area
        if (timeRange == TimeRange.WEEK) {
            // Week View: Fixed 7 equal-spaced bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyStats.forEachIndexed { index, stat ->
                    BarItem(
                        stat = stat,
                        maxSteps = maxSteps,
                        isSelected = selectedIndex == index,
                        onClick = { onBarSelected(index) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // Month View: Scrollable 30 bars, pre-scrolled to today
            val scrollState = rememberScrollState()
            LaunchedEffect(dailyStats.size) {
                if (dailyStats.isNotEmpty()) {
                    scrollState.scrollTo(scrollState.maxValue)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                dailyStats.forEachIndexed { index, stat ->
                    BarItem(
                        stat = stat,
                        maxSteps = maxSteps,
                        isSelected = selectedIndex == index,
                        onClick = { onBarSelected(index) },
                        modifier = Modifier.width(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BarItem(
    stat: DailyStatItem,
    maxSteps: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fillFraction = (stat.stepsCount.toFloat() / maxSteps.toFloat()).coerceIn(0.04f, 1f)
    val animatedFill by animateFloatAsState(
        targetValue = fillFraction,
        animationSpec = tween(durationMillis = 600),
        label = "barFill"
    )

    val barBrush = when {
        isSelected -> Brush.verticalGradient(
            colors = listOf(Color(0xFFFB7185), Color(0xFFE11D48))
        )
        stat.isGoalMet -> Brush.verticalGradient(
            colors = listOf(Color(0xFFE11D48), Color(0xFF9F1239))
        )
        stat.stepsCount > 0 -> Brush.verticalGradient(
            colors = listOf(Color(0xFF475569), Color(0xFF1E293B))
        )
        else -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
        )
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Selection indicator dot
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFB7185))
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // The Bar
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isSelected) 0.85f else 0.7f)
                .fillMaxHeight(animatedFill * 0.8f)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(barBrush)
                .then(
                    if (isSelected) Modifier.border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    else Modifier
                )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Day Label
        Text(
            text = stat.dayLabel,
            color = if (isSelected) Color(0xFFFB7185) else Color(0xFF64748B),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

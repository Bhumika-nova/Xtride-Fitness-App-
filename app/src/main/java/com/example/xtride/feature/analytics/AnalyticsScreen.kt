package com.example.xtride.feature.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xtride.feature.analytics.components.AnalyticsBarChart
import com.example.xtride.feature.analytics.components.AnalyticsMetricCards
import com.example.xtride.feature.analytics.components.AnalyticsTimeRangeSelector
import com.example.xtride.feature.analytics.components.DailyHistoryBreakdown

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040711))
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Screen Header
        Column {
            Text(
                text = "Performance",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Analytics & Reports",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Time Range Pill Selector (Week / Month)
        AnalyticsTimeRangeSelector(
            selectedRange = uiState.selectedRange,
            onRangeSelected = { viewModel.setTimeRange(it) }
        )

        // Interactive Bar Chart
        AnalyticsBarChart(
            timeRange = uiState.selectedRange,
            dailyStats = uiState.dailyStats,
            selectedIndex = uiState.selectedBarIndex,
            onBarSelected = { viewModel.selectBar(it) }
        )

        // Summary Metric Quad Cards
        AnalyticsMetricCards(uiState = uiState)

        // Historical Daily Breakdown
        DailyHistoryBreakdown(dailyStats = uiState.dailyStats)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

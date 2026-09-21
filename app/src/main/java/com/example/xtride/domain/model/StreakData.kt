package com.example.xtride.domain.model

data class StreakData(
    val currentStreakDays: Int,
    val bestStreakDays: Int,
    // 35 integers (5 weeks x 7 days) representing intensity levels (0 to 4) for the Crimson Heatmap
    val heatmapIntensities: List<Int>
)
package com.example.liquidapp.data.dao

import java.time.LocalDate

// Data class representing the total water intake for a specific day.
data class DailyTotal(
    val date: LocalDate,
    val total_oz: Int
) 
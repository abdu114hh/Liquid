package com.example.liquidapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Entity representing a daily water intake goal.
 */
@Entity(tableName = "DailyGoal")
data class DailyGoalEntity(
    @PrimaryKey
    val start_date: LocalDate,
    val goal_oz: Int
) 
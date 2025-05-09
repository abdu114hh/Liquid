package com.example.liquidapp.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

// Entity representing a water intake log entry.
@Entity(
    tableName = "WaterLog",
    indices = [Index(value = ["date"], name = "idx_waterlog_date")]
)
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val amount_oz: Int
) 
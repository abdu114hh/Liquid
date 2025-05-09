package com.example.liquidapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.liquidapp.data.entity.DailyGoalEntity
import com.example.liquidapp.data.entity.WaterLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate


// Data Access Object for all hydration-related database operations.
@Dao
interface HydrationDao {
    
    // Water Log operations
    @Insert
    suspend fun insertLog(log: WaterLogEntity): Long
    
    @Query("SELECT * FROM WaterLog WHERE date = :date ORDER BY id")
    fun getLogsFor(date: LocalDate): Flow<List<WaterLogEntity>>
    
    @Query("SELECT SUM(amount_oz) FROM WaterLog WHERE date = :date")
    fun sumByDate(date: LocalDate): Flow<Int?>
    
    // Daily Goal operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: DailyGoalEntity): Long
    
    @Query("SELECT * FROM DailyGoal WHERE start_date <= :date ORDER BY start_date DESC LIMIT 1")
    fun latestGoal(date: LocalDate): Flow<DailyGoalEntity?>
    
    // Additional queries for the history screen
    @Query("SELECT date, SUM(amount_oz) as total_oz FROM WaterLog WHERE date BETWEEN :startDate AND :endDate GROUP BY date ORDER BY date DESC")
    fun getDailyTotals(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyTotal>>
} 
package com.example.liquidapp.data.repository

import com.example.liquidapp.data.dao.DailyTotal
import com.example.liquidapp.data.dao.HydrationDao
import com.example.liquidapp.data.entity.DailyGoalEntity
import com.example.liquidapp.data.entity.WaterLogEntity
import com.example.liquidapp.util.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

// Repository that handles all hydration-related data operations.
@Singleton
class HydrationRepository @Inject constructor(
    private val hydrationDao: HydrationDao,
    private val preferenceManager: PreferenceManager
) {
    
    // Cup size preferences
    fun getCupSize(): Int = preferenceManager.getCupSize()
    
    fun setCupSize(ounces: Int) = preferenceManager.setCupSize(ounces)
    
    // Water log operations
    suspend fun addWaterLog(date: LocalDate, amountOz: Int) {
        val logEntry = WaterLogEntity(date = date, amount_oz = amountOz)
        hydrationDao.insertLog(logEntry)
    }
    
    fun getWaterLogsForDate(date: LocalDate): Flow<List<WaterLogEntity>> {
        return hydrationDao.getLogsFor(date)
    }
    
    fun getTotalOuncesForDate(date: LocalDate): Flow<Int> {
        return hydrationDao.sumByDate(date).map { it ?: 0 }
    }
    
    // Get total cups (logged ounces divided by cup size)
    fun getTotalCupsForDate(date: LocalDate): Flow<Int> {
        return hydrationDao.sumByDate(date).map { (it ?: 0) / getCupSize() }
    }
    
    // Daily goal operations
    suspend fun setDailyGoal(date: LocalDate, goalOz: Int) {
        val goalEntry = DailyGoalEntity(start_date = date, goal_oz = goalOz)
        hydrationDao.insertGoal(goalEntry)
    }
    
    fun getActiveGoal(date: LocalDate): Flow<Int> {
        return hydrationDao.latestGoal(date).map { it?.goal_oz ?: DEFAULT_GOAL_OZ }
    }
    
    // Combines the current day's intake with the active goal to calculate progress percentage
    fun getDailyProgressPercentage(date: LocalDate): Flow<Int> {
        return combine(
            getTotalOuncesForDate(date),
            getActiveGoal(date)
        ) { ouncesConsumed, goalOunces ->
            if (goalOunces <= 0) 0 else (ouncesConsumed * 100 / goalOunces).coerceIn(0, 100)
        }
    }
    
    // For the history screen
    fun getHistoryForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyTotal>> {
        return hydrationDao.getDailyTotals(startDate, endDate)
    }
    
    companion object {
        const val DEFAULT_GOAL_OZ = 64 // Default daily goal (8 cups * 8 oz)
        const val DEFAULT_CUP_SIZE_OZ = 8 // Default cup size in ounces
    }
} 
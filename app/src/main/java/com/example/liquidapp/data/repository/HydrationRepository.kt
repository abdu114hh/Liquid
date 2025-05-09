package com.example.liquidapp.data.repository

import android.util.Log
import com.example.liquidapp.data.dao.DailyTotal
import com.example.liquidapp.data.dao.HydrationDao
import com.example.liquidapp.data.entity.DailyGoalEntity
import com.example.liquidapp.data.entity.WaterLogEntity
import com.example.liquidapp.util.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

private const val TAG = "HydrationRepository"

/**
 * Repository that handles all hydration-related data operations.
 */
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
        try {
            // Check if adding a negative amount would result in negative total
            if (amountOz < 0) {
                val currentTotal = hydrationDao.sumByDate(date).map { it ?: 0 }.first()
                if (currentTotal + amountOz < 0) {
                    // Just set to zero instead of going negative
                    val logEntry = WaterLogEntity(date = date, amount_oz = -currentTotal)
                    hydrationDao.insertLog(logEntry)
                    return
                }
            }
            
            val logEntry = WaterLogEntity(date = date, amount_oz = amountOz)
            hydrationDao.insertLog(logEntry)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding water log", e)
            throw e
        }
    }
    
    fun getWaterLogsForDate(date: LocalDate): Flow<List<WaterLogEntity>> {
        return hydrationDao.getLogsFor(date)
            .catch { e ->
                Log.e(TAG, "Error getting water logs for date", e)
                emit(emptyList())
            }
    }
    
    fun getTotalOuncesForDate(date: LocalDate): Flow<Int> {
        return hydrationDao.sumByDate(date)
            .map { it ?: 0 }
            .catch { e ->
                Log.e(TAG, "Error getting total ounces", e)
                emit(0)
            }
    }
    
    // Get total cups (logged ounces divided by cup size)
    fun getTotalCupsForDate(date: LocalDate): Flow<Float> {
        return hydrationDao.sumByDate(date)
            .map { 
                val totalOz = it ?: 0
                val cupSize = getCupSize().coerceAtLeast(1)
                totalOz.toFloat() / cupSize
            }
            .catch { e ->
                Log.e(TAG, "Error getting total cups", e)
                emit(0f)
            }
    }
    
    // Daily goal operations
    suspend fun setDailyGoal(date: LocalDate, goalOz: Int) {
        try {
            val goalEntry = DailyGoalEntity(start_date = date, goal_oz = goalOz)
            hydrationDao.insertGoal(goalEntry)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting daily goal", e)
            throw e
        }
    }
    
    fun getActiveGoal(date: LocalDate): Flow<Int> {
        return hydrationDao.latestGoal(date)
            .map { it?.goal_oz ?: DEFAULT_GOAL_OZ }
            .catch { e ->
                Log.e(TAG, "Error getting active goal", e)
                emit(DEFAULT_GOAL_OZ)
            }
    }
    
    // Combines the current day's intake with the active goal to calculate progress percentage
    fun getDailyProgressPercentage(date: LocalDate): Flow<Int> {
        return try {
            combine(
                getTotalOuncesForDate(date),
                getActiveGoal(date)
            ) { ouncesConsumed, goalOunces ->
                if (goalOunces <= 0) 0 else (ouncesConsumed * 100 / goalOunces).coerceIn(0, 100)
            }.catch { e ->
                Log.e(TAG, "Error calculating progress percentage", e)
                emit(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up progress calculation", e)
            flow { emit(0) }
        }
    }
    
    // For the history screen
    fun getHistoryForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyTotal>> {
        return hydrationDao.getDailyTotals(startDate, endDate)
            .catch { e ->
                Log.e(TAG, "Error getting history for date range", e)
                emit(emptyList())
            }
    }
    
    companion object {
        const val DEFAULT_GOAL_OZ = 64 // Default daily goal (8 cups * 8 oz)
        const val DEFAULT_CUP_SIZE_OZ = 8 // Default cup size in ounces
    }
} 
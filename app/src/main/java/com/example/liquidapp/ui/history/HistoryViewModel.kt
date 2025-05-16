package com.example.liquidapp.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.liquidapp.data.dao.DailyTotal
import com.example.liquidapp.data.repository.HydrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

// ViewModel for the History screen.
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HydrationRepository
) : ViewModel() {
    
    // Start date is 30 days ago by default
    private val _startDate = MutableStateFlow(LocalDate.now().minusDays(29))
    
    // End date is today by default
    private val _endDate = MutableStateFlow(LocalDate.now())
    
    // History items based on date range
    val historyItems: LiveData<List<HistoryItem>> = combine(
        _startDate,
        _endDate
    ) { start, end ->
        Pair(start, end)
    }.flatMapLatest { (start, end) ->
        repository.getHistoryForDateRange(start, end)
    }.flatMapLatest { dailyTotals ->
        // Combine with daily goals for each date
        combine(
            dailyTotals.map { dailyTotal ->
                repository.getActiveGoal(dailyTotal.date).combine(
                    repository.getTotalCupsForDate(dailyTotal.date)
                ) { goalOz, cups ->
                    val percentageComplete = if (goalOz > 0) {
                        (dailyTotal.total_oz * 100 / goalOz).coerceIn(0, 100)
                    } else 0
                    
                    HistoryItem(
                        date = dailyTotal.date,
                        totalOz = dailyTotal.total_oz,
                        cups = cups,
                        percentageComplete = percentageComplete,
                        goalOz = goalOz
                    )
                }
            }
        ) { historyItems -> historyItems.sortedByDescending { it.date } }
    }.asLiveData()
    
    // Set the date range for history display.
    fun setDateRange(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _startDate.value = startDate
            _endDate.value = endDate
        }
    }
}

// Represents one day's hydration data for the history screen.
data class HistoryItem(
    val date: LocalDate,
    val totalOz: Int,
    val cups: Float,
    val percentageComplete: Int,
    val goalOz: Int
) 
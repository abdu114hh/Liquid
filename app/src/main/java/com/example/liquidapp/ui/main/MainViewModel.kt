package com.example.liquidapp.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.liquidapp.data.repository.HydrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.text.format
import kotlin.text.map
import androidx.lifecycle.map
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map


private const val TAG = "MainViewModel"

/**
 * ViewModel for the main hydration tracking screen.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: HydrationRepository
) : ViewModel() {

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    // Error handling
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // Track the last increment type (full cup or quarter cup)
    private var lastIncrementType = IncrementType.FULL

    // Format the current date for display using the map extension function
    val formattedDate: LiveData<String> =
        currentDate.asLiveData().map { date ->
            try {
                val formatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy")
                date.format(formatter)
            } catch (e: Exception) {
                Log.e(TAG, "Error formatting date", e)
                "Today"
            }
        }

    // Get the total cups consumed today
    val todayCupCount: LiveData<Float> = repository.getTotalCupsForDate(LocalDate.now())
        .catch { e ->
            Log.e(TAG, "Error getting cup count", e)
            _error.postValue("Failed to load cup count: ${e.message}")
            emit(0f)
        }
        .asLiveData()

    // Get the progress percentage for today
    val progressPercentage: LiveData<Int> = repository.getDailyProgressPercentage(LocalDate.now())
        .catch { e ->
            Log.e(TAG, "Error getting progress", e)
            _error.postValue("Failed to load progress: ${e.message}")
            emit(0)
        }
        .asLiveData()

    // Get the current cup size in ounces
    val cupSizeOz: Int
        get() = try {
            repository.getCupSize()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cup size", e)
            8 // Default to 8oz if there's an error
        }

    // Add a full cup of water.
    fun addFullCup() {
        viewModelScope.launch {
            try {
                lastIncrementType = IncrementType.FULL
                repository.addWaterLog(LocalDate.now(), repository.getCupSize())
            } catch (e: Exception) {
                Log.e(TAG, "Error adding full cup", e)
                _error.postValue("Failed to add water: ${e.message}")
            }
        }
    }

    // Add a quarter cup of water.
    fun addQuarterCup() {
        viewModelScope.launch {
            try {
                lastIncrementType = IncrementType.QUARTER
                val quarterAmount = repository.getCupSize() / 4
                if (quarterAmount > 0) {
                    repository.addWaterLog(LocalDate.now(), quarterAmount)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding quarter cup", e)
                _error.postValue("Failed to add water: ${e.message}")
            }
        }
    }

    // Remove water based on the last increment type
    fun removeLastIncrement() {
        viewModelScope.launch {
            try {
                val currentOunces = repository.getTotalOuncesForDate(LocalDate.now()).first()
                val cupSize = repository.getCupSize()
                
                val amountToRemove = when (lastIncrementType) {
                    IncrementType.FULL -> cupSize
                    IncrementType.QUARTER -> cupSize / 4
                }
                
                // Only remove if we won't go below zero
                if (currentOunces >= amountToRemove) {
                    repository.addWaterLog(LocalDate.now(), -amountToRemove)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing increment", e)
                _error.postValue("Failed to remove water: ${e.message}")
            }
        }
    }
    
    // Enum to track the last increment type
    private enum class IncrementType {
        FULL,
        QUARTER
    }
}
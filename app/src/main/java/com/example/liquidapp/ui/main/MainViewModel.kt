package com.example.liquidapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.liquidapp.data.repository.HydrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.text.format
import kotlin.text.map
import androidx.lifecycle.map

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: HydrationRepository
) : ViewModel() {

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    // Format the current date for display using the map extension function
    val formattedDate: LiveData<String> = currentDate.asLiveData().map { date ->
        val formatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy")
        date.format(formatter)
    }

    // Get the total cups consumed today
    val todayCupCount: LiveData<Int> = repository.getTotalCupsForDate(LocalDate.now()).asLiveData()

    // Get the progress percentage for today
    val progressPercentage: LiveData<Int> = repository.getDailyProgressPercentage(LocalDate.now()).asLiveData()

    // Get the current cup size in ounces
    val cupSizeOz: Int
        get() = repository.getCupSize()

    // Add a full cup of water.
    fun addFullCup() {
        viewModelScope.launch {
            repository.addWaterLog(LocalDate.now(), repository.getCupSize())
        }
    }

    // Add a quarter cup of water.
    fun addQuarterCup() {
        viewModelScope.launch {
            val quarterAmount = repository.getCupSize() / 4
            if (quarterAmount > 0) {
                repository.addWaterLog(LocalDate.now(), quarterAmount)
            }
        }
    }

    // Remove a full cup of water
    fun removeFullCup() {
        viewModelScope.launch {
            // Add a negative amount to decrease the total
            repository.addWaterLog(LocalDate.now(), -repository.getCupSize())
        }
    }
}
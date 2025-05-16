package com.example.liquidapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liquidapp.data.repository.HydrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import javax.inject.Inject

// ViewModel for Settings screen that manages cup size and daily goal preferences.
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: HydrationRepository
) : ViewModel() {
    
    // Get the current cup size in ounces.
    fun getCurrentCupSize(): Int {
        return repository.getCupSize()
    }
    
    // Set a new cup size in ounces.
    fun setCupSize(ounces: Int) {
        repository.setCupSize(ounces)
    }
    
    // Get the current daily goal in ounces.
    fun getCurrentDailyGoal(): Int {
        // This is a simplification - in a real app we would use Flow/LiveData,
        // but for UI purposes we provide a synchronous version
        return runBlocking {
            repository.getActiveGoal(LocalDate.now()).first()
        }
    }
    
    // Set a new daily goal in ounces.
    fun setDailyGoal(ounces: Int) {
        viewModelScope.launch {
            repository.setDailyGoal(LocalDate.now(), ounces)
        }
    }
} 
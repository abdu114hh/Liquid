package com.example.liquidapp.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// Provides access to WorkManager for scheduling background tasks.
@Singleton
class WorkManagerProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val HYDRATION_REMINDER_WORK = "hydration_reminder_work"
    }
    
    // Schedule periodic hydration reminder checks.
    fun scheduleHydrationReminders() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val reminderRequest = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            repeatInterval = 2,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            HYDRATION_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }
} 
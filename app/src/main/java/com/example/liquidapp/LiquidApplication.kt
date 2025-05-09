package com.example.liquidapp

import android.app.Application
import android.util.Log
import com.example.liquidapp.work.WorkManagerProvider
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

private const val TAG = "LiquidApplication"

/**
 * Application class for the LIQUID app.
 */
@HiltAndroidApp
class LiquidApplication : Application() {
    
    @Inject
    lateinit var workManagerProvider: WorkManagerProvider
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Schedule hydration reminders
            workManagerProvider.scheduleHydrationReminders()
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminders", e)
        }
    }
} 
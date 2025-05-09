package com.example.liquidapp

import android.app.Application
import com.example.liquidapp.work.WorkManagerProvider
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// Application class for the LIQUID app.
@HiltAndroidApp
class LiquidApplication : Application() {
    
    @Inject
    lateinit var workManagerProvider: WorkManagerProvider
    
    override fun onCreate() {
        super.onCreate()
        
        // Schedule hydration reminders
        workManagerProvider.scheduleHydrationReminders()
    }
} 
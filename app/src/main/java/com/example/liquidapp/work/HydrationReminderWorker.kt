package com.example.liquidapp.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.liquidapp.MainActivity
import com.example.liquidapp.R
import com.example.liquidapp.data.repository.HydrationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

// Worker that checks if the user is behind on their hydration goal and sends a reminder notification.
@HiltWorker
class HydrationReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: HydrationRepository
) : CoroutineWorker(context, workerParameters) {
    
    companion object {
        private const val CHANNEL_ID = "hydration_reminders"
        private const val NOTIFICATION_ID = 1001
    }
    
    override suspend fun doWork(): Result {
        // Don't remind during sleeping hours (10 PM - 7 AM)
        val now = LocalTime.now()
        if (now.isBefore(LocalTime.of(7, 0)) || now.isAfter(LocalTime.of(22, 0))) {
            return Result.success()
        }
        
        val todayProgress = repository.getDailyProgressPercentage(LocalDate.now()).first()
        val expectedProgress = calculateExpectedProgress()
        
        // If the user is significantly behind expected progress (>20% behind), send a reminder
        if (todayProgress < expectedProgress - 20) {
            sendHydrationReminder()
        }
        
        return Result.success()
    }
    
    // Calculate expected progress based on the time of day.
    // This assumes a linear progression throughout waking hours (7 AM - 10 PM)
    private fun calculateExpectedProgress(): Int {
        val now = LocalTime.now()
        val dayStart = LocalTime.of(7, 0)
        val dayEnd = LocalTime.of(22, 0)
        
        // If outside of waking hours, return appropriate bounds
        if (now.isBefore(dayStart)) return 0
        if (now.isAfter(dayEnd)) return 100
        
        // Calculate how far we are through the day as a percentage
        val totalMinutesInDay = ChronoUnit.MINUTES.between(dayStart, dayEnd)
        val minutesPassed = ChronoUnit.MINUTES.between(dayStart, now)
        
        return ((minutesPassed * 100) / totalMinutesInDay).toInt()
    }
    
    // Send a notification reminding the user to drink water
    private fun sendHydrationReminder() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create the notification channel (required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to drink water throughout the day"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent to open the app when notification is tapped
        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the notification
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_glass)
            .setContentTitle("Hydration Reminder")
            .setContentText("You're a bit behind on your water intake goal today. Time for a drink!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Show the notification
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
} 
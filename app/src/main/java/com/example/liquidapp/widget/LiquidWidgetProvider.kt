package com.example.liquidapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import com.example.liquidapp.MainActivity
import com.example.liquidapp.R
import com.example.liquidapp.data.repository.HydrationRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

private const val TAG = "LiquidWidgetProvider"

/**
 * Implementation of App Widget functionality for the LIQUID app.
 */
class LiquidWidgetProvider : AppWidgetProvider() {
    
    /**
     * Hilt entry point for accessing dependencies from a non-Android class
     */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HydrationRepositoryEntryPoint {
        fun hydrationRepository(): HydrationRepository
    }
    
    /**
     * Get the repository instance using EntryPointAccessors
     */
    private fun getRepository(context: Context): HydrationRepository? {
        return try {
            val hiltEntryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                HydrationRepositoryEntryPoint::class.java
            )
            hiltEntryPoint.hydrationRepository()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting repository", e)
            Toast.makeText(context, "Widget error: Unable to access data", Toast.LENGTH_SHORT).show()
            null
        }
    }
    
    companion object {
        // Intent actions for widget buttons
        const val ACTION_ADD = "com.example.liquidapp.widget.ACTION_ADD"
        const val ACTION_MINUS = "com.example.liquidapp.widget.ACTION_MINUS"
        const val ACTION_QUARTER = "com.example.liquidapp.widget.ACTION_QUARTER"
        
        // Progress bar colors
        private const val COLOR_RED = "#FF5252"
        private const val COLOR_ORANGE = "#FFA726"
        private const val COLOR_GREEN = "#66BB6A"
        
        /**
         * Update all active widgets.
         */
        fun updateAllWidgets(context: Context) {
            try {
                val intent = Intent(context, LiquidWidgetProvider::class.java)
                intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                
                val widgetManager = AppWidgetManager.getInstance(context)
                val widgetIds = widgetManager.getAppWidgetIds(ComponentName(context, LiquidWidgetProvider::class.java))
                
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                context.sendBroadcast(intent)
                
                Log.d(TAG, "Widget update broadcast sent")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widgets", e)
            }
        }
    }
    
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate called for ${appWidgetIds.size} widgets")
        try {
            // Update each widget
            for (widgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onUpdate", e)
            // Show a fallback widget with error state
            for (widgetId in appWidgetIds) {
                try {
                    val views = RemoteViews(context.packageName, R.layout.widget_liquid)
                    views.setTextViewText(R.id.widget_count, "!")
                    views.setProgressBar(R.id.widget_progress, 100, 0, false)
                    views.setInt(R.id.widget_progress, "setProgressTintList", Color.parseColor(COLOR_RED))
                    
                    // Set click to open main app
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        Intent(context, MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_progress, pendingIntent)
                    
                    appWidgetManager.updateAppWidget(widgetId, views)
                } catch (innerEx: Exception) {
                    Log.e(TAG, "Fatal widget error", innerEx)
                }
            }
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: ${intent.action}")
        try {
            super.onReceive(context, intent)
            
            // Handle custom actions
            when (intent.action) {
                ACTION_ADD -> handleAddAction(context)
                ACTION_MINUS -> handleMinusAction(context)
                ACTION_QUARTER -> handleQuarterAction(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onReceive", e)
        }
    }
    
    private fun handleAddAction(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = getRepository(context) ?: return@launch
                repository.addWaterLog(LocalDate.now(), repository.getCupSize())
                updateAllWidgets(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error in handleAddAction", e)
            }
        }
    }
    
    private fun handleMinusAction(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = getRepository(context) ?: return@launch
                val currentOunces = repository.getTotalOuncesForDate(LocalDate.now()).first()
                val cupSize = repository.getCupSize()
                
                // Only remove if we won't go below zero
                if (currentOunces >= cupSize) {
                    repository.addWaterLog(LocalDate.now(), -cupSize)
                    updateAllWidgets(context)
                } else {
                    // Show a toast message if trying to go below 0
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Already at minimum", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in handleMinusAction", e)
            }
        }
    }
    
    private fun handleQuarterAction(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = getRepository(context) ?: return@launch
                val quarterAmount = repository.getCupSize() / 4
                if (quarterAmount > 0) {
                    repository.addWaterLog(LocalDate.now(), quarterAmount)
                    updateAllWidgets(context)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in handleQuarterAction", e)
            }
        }
    }
    
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        Log.d(TAG, "Updating widget ID: $appWidgetId")
        try {
            // Create remote views
            val views = RemoteViews(context.packageName, R.layout.widget_liquid)
            
            // Set up button click intents
            setUpWidgetButtons(context, views)
            
            // Update progress and count synchronously
            updateWidgetDataSync(context, views)
            
            // Launch main activity when the progress bar is clicked
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_progress, pendingIntent)
            
            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d(TAG, "Widget $appWidgetId updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateAppWidget", e)
        }
    }
    
    private fun setUpWidgetButtons(context: Context, views: RemoteViews) {
        try {
            // Add button
            val addIntent = Intent(context, LiquidWidgetProvider::class.java).apply {
                action = ACTION_ADD
            }
            val addPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_increase, addPendingIntent)
            
            // Minus button
            val minusIntent = Intent(context, LiquidWidgetProvider::class.java).apply {
                action = ACTION_MINUS
            }
            val minusPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                minusIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_decrease, minusPendingIntent)
            
            // Quarter button
            val quarterIntent = Intent(context, LiquidWidgetProvider::class.java).apply {
                action = ACTION_QUARTER
            }
            val quarterPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                quarterIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_quarter, quarterPendingIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error in setUpWidgetButtons", e)
        }
    }
    
    private fun updateWidgetDataSync(context: Context, views: RemoteViews) {
        try {
            // Use runBlocking to ensure we have the data before updating the widget
            runBlocking {
                val repository = getRepository(context) ?: return@runBlocking
                
                // Get current date progress
                val progress = repository.getDailyProgressPercentage(LocalDate.now()).first()
                val totalAmount = repository.getTotalOuncesForDate(LocalDate.now()).first()
                val cupSize = repository.getCupSize()
                
                Log.d(TAG, "Widget data: progress=$progress%, total=$totalAmount oz, cup=$cupSize oz")
                
                // Update UI elements with the fetched data
                views.setProgressBar(R.id.widget_progress, 100, progress.coerceIn(0, 100), false)
                
                // Set progress bar color based on progress
                val progressColor = when {
                    progress >= 100 -> COLOR_GREEN
                    progress >= 50 -> COLOR_ORANGE
                    else -> COLOR_RED
                }
                views.setInt(R.id.widget_progress, "setProgressTintList", Color.parseColor(progressColor))
                
                // Calculate and set drink count exactly as it appears in the app
                // Showing the actual number of cups consumed
                val drinkCount = if (cupSize > 0) (totalAmount / cupSize).toInt() else 0
                views.setTextViewText(R.id.widget_count, drinkCount.toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateWidgetDataSync", e)
            // Set default values if there's an error
            views.setProgressBar(R.id.widget_progress, 100, 0, false)
            views.setInt(R.id.widget_progress, "setProgressTintList", Color.parseColor(COLOR_RED))
            views.setTextViewText(R.id.widget_count, "0")
        }
    }
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "Widget provider enabled")
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "Widget provider disabled")
    }
} 
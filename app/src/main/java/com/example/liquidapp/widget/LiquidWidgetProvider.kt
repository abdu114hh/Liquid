package com.example.liquidapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
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
            null
        }
    }
    
    companion object {
        // Intent actions for widget buttons
        const val ACTION_ADD = "com.example.liquidapp.widget.ACTION_ADD"
        const val ACTION_MINUS = "com.example.liquidapp.widget.ACTION_MINUS"
        const val ACTION_QUARTER = "com.example.liquidapp.widget.ACTION_QUARTER"
        
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
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widgets", e)
            }
        }
    }
    
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        try {
            // Update each widget
            for (widgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onUpdate", e)
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
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
                repository.addWaterLog(LocalDate.now(), -repository.getCupSize())
                updateAllWidgets(context)
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
        try {
            // Create remote views
            val views = RemoteViews(context.packageName, R.layout.widget_liquid)
            
            // Set up button click intents
            setUpWidgetButtons(context, views)
            
            // Update progress and count synchronously
            updateWidgetDataSync(context, views)
            
            // Launch main activity when the widget is clicked
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_progress, pendingIntent)
            
            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
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
            runBlocking {
                val repository = getRepository(context) ?: return@runBlocking
                
                // Get current date progress
                val progress = repository.getDailyProgressPercentage(LocalDate.now()).first()
                views.setProgressBar(R.id.widget_progress, 100, progress, false)
                
                // Get drink count
                val totalAmount = repository.getDailyTotalAmount(LocalDate.now()).first()
                val cupSize = repository.getCupSize()
                val drinkCount = if (cupSize > 0) (totalAmount / cupSize).toInt() else 0
                views.setTextViewText(R.id.widget_count, drinkCount.toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateWidgetDataSync", e)
            // Set default values if there's an error
            views.setProgressBar(R.id.widget_progress, 100, 0, false)
            views.setTextViewText(R.id.widget_count, "0")
        }
    }
} 
package com.example.liquidapp.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager as AndroidPreferenceManager
import com.example.liquidapp.data.repository.HydrationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferenceManager"

/**
 * Manages user preferences for the app.
 */
@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        try {
            AndroidPreferenceManager.getDefaultSharedPreferences(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SharedPreferences", e)
            // Fallback to empty preferences
            context.getSharedPreferences("fallback_prefs", Context.MODE_PRIVATE)
        }
    }
    
    companion object {
        private const val KEY_CUP_SIZE = "pref_cup_size_oz"
        private const val DEFAULT_CUP_SIZE = 8 // Default 8 oz cup
    }
    
    /**
     * Gets the user's preferred cup size in ounces.
     */
    fun getCupSize(): Int {
        return try {
            prefs.getInt(KEY_CUP_SIZE, DEFAULT_CUP_SIZE)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cup size preference", e)
            DEFAULT_CUP_SIZE
        }
    }
    
    /**
     * Sets the user's preferred cup size in ounces.
     */
    fun setCupSize(ounces: Int) {
        try {
            prefs.edit().putInt(KEY_CUP_SIZE, ounces).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting cup size preference", e)
        }
    }
} 
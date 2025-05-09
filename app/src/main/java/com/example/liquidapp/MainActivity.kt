package com.example.liquidapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.liquidapp.databinding.ActivityMainBinding
import com.example.liquidapp.ui.history.HistoryActivity
import com.example.liquidapp.ui.main.MainViewModel
import com.example.liquidapp.ui.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Set up view binding
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // Set up UI observers
            setUpObservers()
            
            // Set up click listeners
            setUpClickListeners()
            
            // Update the current date and time
            updateDateTime()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            // Show a toast to make the error visible
            android.widget.Toast.makeText(
                this,
                "Error: ${e.message}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun setUpObservers() {
        try {
            // Observe progress changes
            viewModel.progressPercentage.observe(this, Observer { progress ->
                binding.progressBar.progress = progress
                
                // Color the progress bar based on completion
                val colorResId = when {
                    progress >= 100 -> R.color.colorSuccess
                    progress >= 75 -> R.color.accent
                    progress >= 50 -> R.color.colorWarning
                    else -> R.color.colorDanger
                }
                
                val color = ContextCompat.getColor(this, colorResId)
                binding.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(color)
            })
            
            // Observe cup count changes
            viewModel.todayCupCount.observe(this, Observer { count ->
                // Format to show decimal places only when needed (e.g., 5.0 shows as 5, but 5.25 shows as 5.25)
                val formattedCount = if (count == count.toInt().toFloat()) {
                    count.toInt().toString()
                } else {
                    String.format("%.2f", count).replace(Regex("\\.?0*$"), "")
                }
                binding.glassCount.text = formattedCount
            })
            
            // Observe date changes
            viewModel.formattedDate.observe(this, Observer { formattedDate ->
                binding.dateTime.text = formattedDate
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error in setUpObservers", e)
        }
    }
    
    private fun setUpClickListeners() {
        try {
            // Quarter cup button
            binding.btnQuarter.setOnClickListener {
                viewModel.addQuarterCup()
            }
            
            // Minus button (remove cup)
            binding.btnMinus.setOnClickListener {
                viewModel.removeLastIncrement()
            }
            
            // Water glass button (add full cup)
            binding.circleContainer.setOnClickListener {
                viewModel.addFullCup()
            }
            
            // Add a plus button to the UI since the current UI doesn't have it but we need it
            binding.menuIcon.setOnClickListener {
                showMenu()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in setUpClickListeners", e)
        }
    }
    
    private fun updateDateTime() {
        try {
            val now = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy • h:mm a")
            binding.dateTime.text = now.format(formatter)
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateDateTime", e)
        }
    }
    
    private fun showMenu() {
        try {
            val popupMenu = android.widget.PopupMenu(this, binding.menuIcon)
            popupMenu.menuInflater.inflate(R.menu.main_menu, popupMenu.menu)
            
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_history -> {
                        startActivity(Intent(this, HistoryActivity::class.java))
                        true
                    }
                    R.id.menu_settings -> {
                        startActivity(Intent(this, SettingsActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
            
            popupMenu.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error in showMenu", e)
        }
    }
}
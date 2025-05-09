package com.example.liquidapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.liquidapp.databinding.ActivityMainBinding
import com.example.liquidapp.ui.history.HistoryActivity
import com.example.liquidapp.ui.main.MainViewModel
import com.example.liquidapp.ui.settings.SettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set up view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up UI observers
        setUpObservers()
        
        // Set up click listeners
        setUpClickListeners()
        
        // Update the current date and time
        updateDateTime()
    }
    
    private fun setUpObservers() {
        // Observe progress changes
        viewModel.progressPercentage.observe(this, Observer { progress ->
            binding.progressBar.progress = progress
        })
        
        // Observe cup count changes
        viewModel.todayCupCount.observe(this, Observer { count ->
            binding.glassCount.text = count.toString()
        })
        
        // Observe date changes
        viewModel.formattedDate.observe(this, Observer { formattedDate ->
            binding.dateTime.text = formattedDate
        })
    }
    
    private fun setUpClickListeners() {
        // Quarter cup button
        binding.btnQuarter.setOnClickListener {
            viewModel.addQuarterCup()
        }
        
        // Minus button (remove cup)
        binding.btnMinus.setOnClickListener {
            viewModel.removeFullCup()
        }
        
        // Add a plus button to the UI since the current UI doesn't have it but we need it
        binding.menuIcon.setOnClickListener {
            showMenu()
        }
    }
    
    private fun updateDateTime() {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy • h:mm a")
        binding.dateTime.text = now.format(formatter)
    }
    
    private fun showMenu() {
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
    }
}
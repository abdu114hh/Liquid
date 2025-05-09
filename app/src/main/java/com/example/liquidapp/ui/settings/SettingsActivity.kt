package com.example.liquidapp.ui.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.liquidapp.databinding.ActivitySettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set up view binding
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up toolbar
        setSupportActionBar(binding.settingsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
        
        // Set initial values
        binding.cupSizeSeekBar.progress = viewModel.getCurrentCupSize() - 1 // Convert to 0-based index
        binding.cupSizeValue.text = "${viewModel.getCurrentCupSize()} oz"
        
        binding.goalSeekBar.progress = (viewModel.getCurrentDailyGoal() / 8) - 1 // Convert to cups (8 oz units)
        binding.goalValue.text = "${viewModel.getCurrentDailyGoal()} oz"
        
        // Set up listeners
        setupListeners()
    }
    
    private fun setupListeners() {
        // Cup size seek bar
        binding.cupSizeSeekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val cupSize = progress + 1 // Convert from 0-based to 1-based
                binding.cupSizeValue.text = "$cupSize oz"
            }
            
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                val cupSize = seekBar?.progress?.plus(1) ?: 8
                viewModel.setCupSize(cupSize)
            }
        })
        
        // Goal seek bar
        binding.goalSeekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val goalInOz = (progress + 1) * 8 // Convert from cups to ounces
                binding.goalValue.text = "$goalInOz oz"
            }
            
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                val goalInOz = (seekBar?.progress?.plus(1) ?: 8) * 8
                viewModel.setDailyGoal(goalInOz)
            }
        })
        
        // Save button
        binding.saveButton.setOnClickListener {
            finish() // Just finish since values are saved on change
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 
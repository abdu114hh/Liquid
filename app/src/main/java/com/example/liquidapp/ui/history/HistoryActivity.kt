package com.example.liquidapp.ui.history

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.liquidapp.databinding.ActivityHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class HistoryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set up view binding
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up toolbar
        setSupportActionBar(binding.historyToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "History"
        
        // Set up RecyclerView
        setupRecyclerView()
        
        // Set up date range
        setupDateRange()
        
        // Observe changes to history data
        viewModel.historyItems.observe(this) { items ->
            adapter.submitList(items)
            
            // Update summary text
            updateSummary(items)
        }
    }
    
    private fun setupRecyclerView() {
        adapter = HistoryAdapter()
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = this@HistoryActivity.adapter
        }
    }
    
    private fun setupDateRange() {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(29)
        
        val formatter = DateTimeFormatter.ofPattern("MMM d")
        binding.dateRangeText.text = "${startDate.format(formatter)} - ${endDate.format(formatter)}"
        
        // Set date range in view model
        viewModel.setDateRange(startDate, endDate)
    }
    
    private fun updateSummary(items: List<HistoryItem>) {
        if (items.isEmpty()) {
            binding.summaryText.text = "No data recorded yet"
            return
        }
        
        // Calculate daily average
        val totalOz = items.sumOf { it.totalOz }
        val averageOz = totalOz / items.size
        
        // Calculate days on target
        val daysOnTarget = items.count { it.percentageComplete >= 100 }
        val percentOnTarget = if (items.isNotEmpty()) {
            (daysOnTarget * 100 / items.size)
        } else 0
        
        binding.summaryText.text = "Average: $averageOz oz/day • On target: $percentOnTarget%"
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 
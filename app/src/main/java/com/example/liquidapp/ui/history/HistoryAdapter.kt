package com.example.liquidapp.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.liquidapp.R
import com.example.liquidapp.databinding.ItemHistoryDayBinding
import java.time.format.DateTimeFormatter

// Adapter for the history RecyclerView that displays past water intake.
class HistoryAdapter : ListAdapter<HistoryItem, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {
    
    // Date formatter for displaying day names
    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class HistoryViewHolder(
        private val binding: ItemHistoryDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: HistoryItem) {
            // Format the date
            binding.dayText.text = dateFormatter.format(item.date)
            
            // Set the consumption details
            binding.consumptionText.text = "${item.totalOz} oz (${item.cups} cups)"
            
            // Set the goal completion text
            binding.goalText.text = "${item.percentageComplete}% of ${item.goalOz} oz goal"
            
            // Update progress bar
            binding.progressBar.progress = item.percentageComplete
            
            // Color the progress bar based on completion
            val colorResId = when {
                item.percentageComplete >= 100 -> R.color.colorSuccess
                item.percentageComplete >= 75 -> R.color.accent
                item.percentageComplete >= 50 -> R.color.colorWarning
                else -> R.color.colorDanger
            }
            
            val color = ContextCompat.getColor(binding.root.context, colorResId)
            binding.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(color)
        }
    }
}

// DiffUtil callback for the history items.
class HistoryDiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
    override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        return oldItem.date == newItem.date
    }
    
    override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        return oldItem == newItem
    }
} 
package com.unchain.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unchain.R
import com.unchain.data.model.SugarHistory
import java.text.SimpleDateFormat
import java.util.*

class DailyConsumeAdapter : RecyclerView.Adapter<DailyConsumeAdapter.ViewHolder>() {
    private var items: List<SugarHistory> = listOf()
    private var expandedPosition = -1

    fun setItems(newItems: List<SugarHistory>) {
        // Filter items untuk hari ini saja
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        items = newItems.filter { history ->
            history.createdAt?.let { dateString ->
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                val date = inputFormat.parse(dateString)
                val historyDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
                historyDate == today
            } ?: false
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_consume, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvIcon: TextView = view.findViewById(R.id.tvIcon)
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val tvWeight: TextView = view.findViewById(R.id.tvWeight)
        private val tvDate: TextView = view.findViewById(R.id.tvDate)
        private val detailsView: View = view.findViewById(R.id.detailsView) // Assuming you have a details view

        fun bind(item: SugarHistory, position: Int) {
            tvIcon.text = if (item.isBeverage) "☕" else "🍽️"
            tvName.text = item.title
            tvWeight.text = "${item.weight} gr"

            // Format the date
            item.createdAt?.let { dateString ->
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                    val outputFormat = SimpleDateFormat("dd MMM", Locale.US)
                    val date = inputFormat.parse(dateString)
                    tvDate.text = date?.let { outputFormat.format(it) } ?: dateString
                } catch (e: Exception) {
                    tvDate.text = dateString
                }
            }

            // Toggle visibility of details
            val isExpanded = position == expandedPosition
            detailsView.visibility = if (isExpanded) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                expandedPosition = if (isExpanded) -1 else position
                notifyDataSetChanged()
            }
        }
    }
}

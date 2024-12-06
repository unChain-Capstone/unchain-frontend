package com.unchain.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unchain.R
import com.unchain.data.ml.RecommendationItem

class RecommendationAdapter(
    private var recommendations: List<RecommendationItem> = emptyList(),
    private val onItemClick: (RecommendationItem) -> Unit
) : RecyclerView.Adapter<RecommendationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: LinearLayout = view.findViewById(R.id.recommendationContainer)
        val titleText: TextView = view.findViewById(R.id.titleText)
        val scoreText: TextView = view.findViewById(R.id.scoreText)
        val recommendationText: TextView = view.findViewById(R.id.recommendationText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = recommendations[position]
        
        holder.titleText.text = "Sugar Level: ${item.sugarLevel}"
        holder.scoreText.text = String.format("Confidence: %.1f%%", item.score * 100)
        holder.recommendationText.text = item.recommendation
        
        holder.container.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = recommendations.size

    fun updateRecommendations(newRecommendations: List<RecommendationItem>) {
        recommendations = newRecommendations
        notifyDataSetChanged()
    }
}

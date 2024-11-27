package com.unchain.ui.chat

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.unchain.R
import com.unchain.api.Message
import com.unchain.databinding.ItemMessageBinding

class ChatAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
        private const val VIEW_TYPE_LOADING = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            messages[position].isLoading -> VIEW_TYPE_LOADING
            messages[position].role == "user" -> VIEW_TYPE_USER
            else -> VIEW_TYPE_BOT
        }
    }

    inner class MessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            if (message.isLoading) {
                // Show loading animation
                binding.messageText.text = "Typing..."
                binding.loadingDots.visibility = View.VISIBLE
            } else {
                binding.messageText.text = message.content
                binding.loadingDots.visibility = View.GONE
            }

            val params = binding.messageContainer.layoutParams as ConstraintLayout.LayoutParams
            if (message.role == "user") {
                setupUserMessage(params)
            } else {
                setupBotMessage(params)
            }
            binding.messageContainer.layoutParams = params
        }

        private fun setupUserMessage(params: ConstraintLayout.LayoutParams) {
            params.startToEnd = ConstraintLayout.LayoutParams.UNSET
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            params.startToStart = ConstraintLayout.LayoutParams.UNSET
            params.marginEnd = 0
            params.marginStart = 48.dpToPx(itemView.context)
            binding.messageContainer.setBackgroundResource(R.drawable.message_background)
            binding.messageText.setTextColor(Color.WHITE)
        }

        private fun setupBotMessage(params: ConstraintLayout.LayoutParams) {
            params.startToEnd = ConstraintLayout.LayoutParams.UNSET
            params.endToEnd = ConstraintLayout.LayoutParams.UNSET
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            params.marginEnd = 48.dpToPx(itemView.context)
            params.marginStart = 0
            binding.messageContainer.setBackgroundResource(R.drawable.message_background_bot)
            binding.messageText.setTextColor(Color.BLACK)
        }
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMessageBinding.inflate(inflater, parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size
}


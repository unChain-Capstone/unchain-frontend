package com.unchain.ui.chat

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.unchain.BuildConfig
import com.unchain.R
import com.unchain.api.ChatRequest
import com.unchain.api.Message
import com.unchain.api.OpenRouterClient
import com.unchain.databinding.FragmentChatBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    private val viewModel: ChatViewModel by viewModels()

    private val fragmentScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()

        // Add initial system message
        messages.addAll(createInitialMessages())

        // Add welcome message
        messages.add(Message(
            role = "assistant",
            content = "Hello! I'm SugarSense, your personal assistant for monitoring daily sugar intake. How can I help you today?"
        ))
        chatAdapter.notifyDataSetChanged()
        binding.recyclerView.smoothScrollToPosition(messages.size - 1)
    }

    private fun createInitialMessages(): List<Message> {
        return listOf(
            Message(
                role = "system",
                content = "You are SugarSense, a friendly and knowledgeable personal assistant focused on helping users monitor their daily sugar intake."
            )
        )
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages)
        binding.recyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context)
            addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom) {
                    smoothScrollToPosition(messages.size)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.messageInput.text?.clear()
            }
        }
    }

    private fun sendMessage(content: String) {
        val userMessage = Message("user", content)
        messages.add(userMessage)
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.recyclerView.smoothScrollToPosition(messages.size - 1)

        // Add loading message
        val loadingMessage = Message(role = "assistant", content = "", isLoading = true)
        messages.add(loadingMessage)
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.recyclerView.smoothScrollToPosition(messages.size - 1)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val request = ChatRequest(
                    model = "nousresearch/hermes-3-llama-3.1-405b:free",
                    messages = messages.filterNot { it.isLoading } // Filter out loading message
                )

                withContext(Dispatchers.IO) {
                    val response = OpenRouterClient.api.getChatCompletion(
                        authorization = "Bearer ${BuildConfig.OPENROUTER_API_KEY}",
                        request = request
                    )

                    withContext(Dispatchers.Main) {
                        // Remove loading message
                        messages.removeAt(messages.size - 1)
                        chatAdapter.notifyItemRemoved(messages.size)

                        if (response.isSuccessful) {
                            response.body()?.choices?.firstOrNull()?.message?.let { botMessage ->
                                messages.add(botMessage)
                                chatAdapter.notifyItemInserted(messages.size - 1)
                                binding.recyclerView.smoothScrollToPosition(messages.size - 1)
                            }
                        } else {
                            // Add error message instead
                            messages.add(Message(
                                role = "assistant",
                                content = "Sorry, I encountered an error. Please try again."
                            ))
                            chatAdapter.notifyItemInserted(messages.size - 1)
                            binding.recyclerView.smoothScrollToPosition(messages.size - 1)

                            Toast.makeText(
                                context,
                                "Error: ${response.errorBody()?.string()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Remove loading message
                    messages.removeAt(messages.size - 1)
                    chatAdapter.notifyItemRemoved(messages.size)

                    // Add error message
                    messages.add(Message(
                        role = "assistant",
                        content = "Sorry, I encountered an error. Please try again."
                    ))
                    chatAdapter.notifyItemInserted(messages.size - 1)
                    binding.recyclerView.smoothScrollToPosition(messages.size - 1)

                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentScope.cancel()
    }
}


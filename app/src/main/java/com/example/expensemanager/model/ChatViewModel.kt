
package com.example.expensemanager.model

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.MessageModel
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    // Observable messages for Jetpack Compose
    val messageList = mutableStateListOf<MessageModel>()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Firebase AI Logic model
    private val generativeModel by lazy {
        Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = "gemini-3.5-flash-lite",
            systemInstruction = content {
                text(
                    """
                    You are Expense AI, a helpful personal
                    finance assistant in an expense manager app.

                    Your responsibilities:
                    - Explain income and expenses.
                    - Suggest practical saving strategies.
                    - Help users create monthly budgets.
                    - Analyze spending when data is supplied.
                    - Explain financial concepts clearly.
                    - Answer general questions politely.

                    Give clear, helpful, organized answers.
                    Use simple language and practical examples.
                    Use Indian Rupees (INR) when appropriate.
                    Do not invent the user's expense records.
                    Ask for missing information when necessary.
                    Avoid guaranteeing investment returns.
                    """.trimIndent()
                )
            }
        )
    }

    // Keep one chat session to preserve conversation context
    private val chat by lazy {
        generativeModel.startChat()
    }

    fun sendMessage(question: String) {

        val userMessage = question.trim()

        // Prevent blank and duplicate requests
        if (userMessage.isEmpty() || isLoading) return

        errorMessage = null
        isLoading = true

        // Display user's message immediately
        messageList.add(
            MessageModel(userMessage, "user")
        )

        viewModelScope.launch {
            try {
                // Firebase AI Logic request
                val response = chat.sendMessage(userMessage)

                val answer = response.text
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: "I couldn't generate a response. Please try again."

                messageList.add(
                    MessageModel(answer, "model")
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                Log.e(
                    "ChatViewModel",
                    "AI request failed",
                    e
                )

                errorMessage =
                    "Unable to connect to the AI assistant. " +
                            "Check your internet connection and Firebase setup."

            } finally {
                isLoading = false
            }
        }
    }
}

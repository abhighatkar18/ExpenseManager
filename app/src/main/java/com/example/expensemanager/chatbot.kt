
package com.example.expensemanager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensemanager.model.ChatViewModel
import com.example.expensemanager.ui.theme.green

@Composable
fun ChatbotScreenContent(
    chatViewModel: ChatViewModel = viewModel()
) {
    chatbot(Modifier, chatViewModel)
}

@Composable
fun chatbot(
    modifier: Modifier,
    viewModel: ChatViewModel
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7F6))
    ) {

        Header()

        MessageList(
            modifier = Modifier.weight(1f),
            messageList = viewModel.messageList,
            isLoading = viewModel.isLoading
        )

        viewModel.errorMessage?.let { error ->
            Text(
                text = error,
                color = Color(0xFFB42318),
                fontSize = 13.sp,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
            )
        }

        MessageInput(
            isLoading = viewModel.isLoading,
            onMessageSend = viewModel::sendMessage
        )
    }
}

@Composable
fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(green)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Expense AI",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Your smart financial assistant",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun MessageList(
    modifier: Modifier,
    messageList: List<MessageModel>,
    isLoading: Boolean = false
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messageList.size, isLoading) {
        val totalItems =
            messageList.size +
                    (if (messageList.isEmpty()) 1 else 0) +
                    (if (isLoading) 1 else 0)

        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        if (messageList.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 70.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to Expense AI",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = green
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Ask me about budgeting, savings, expenses, and money management.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Try: How can I save ₹5,000 every month?",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        items(messageList) { message ->
            MessageRow(message)
        }

        if (isLoading) {
            item {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = green,
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Expense AI is thinking...",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun MessageRow(messageModel: MessageModel) {

    val isModel = messageModel.role == "model"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isModel) {
            Arrangement.Start
        } else {
            Arrangement.End
        }
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .background(
                    color = if (isModel) Color.White else green,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        ) {

            Text(
                text = if (isModel) "Expense AI" else "You",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isModel) green else Color.White
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = messageModel.message.replace("**", ""),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = if (isModel) {
                    Color(0xFF222222)
                } else {
                    Color.White
                }
            )
        }
    }
}

@Composable
fun MessageInput(
    onMessageSend: (String) -> Unit,
    isLoading: Boolean = false
) {
    var message by rememberSaveable {
        mutableStateOf("")
    }

    fun send() {
        val text = message.trim()

        if (text.isNotEmpty() && !isLoading) {
            onMessageSend(text)
            message = ""
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier.weight(1f),
            placeholder = {
                Text("Ask something...")
            },
            enabled = !isLoading,
            maxLines = 4,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = { send() }
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = { send() },
            enabled = message.isNotBlank() && !isLoading,
            modifier = Modifier
                .size(48.dp)
                .background(
                    green,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send message",
                tint = Color.White
            )
        }
    }
}

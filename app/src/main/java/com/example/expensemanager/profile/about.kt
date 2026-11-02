package com.example.expensemanager.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun AboutUsScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "About Expense Manager",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF009688),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Expense Manager is a smart financial tracker designed to help you manage your expenses, income, and savings effortlessly. " +
                    "With AI-powered insights, real-time transaction tracking, and personalized financial advice, our app ensures you're always in control of your finances.",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Key Features:",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color(0xFFF1F8E9), shape = RoundedCornerShape(12.dp))
                .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Money, contentDescription = "Track Expenses", tint = Color(0xFF009688))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Track daily expenses and income", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.PieChart, contentDescription = "Set Goals", tint = Color(0xFF009688))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Set financial goals and savings targets", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color(0xFF009688))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Receive notifications for spending limits", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Chat, contentDescription = "AI Chatbot", tint = Color(0xFF009688))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "AI-powered chatbot for financial advice", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Analytics, contentDescription = "Analytics", tint = Color(0xFF009688))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Real-time analytics of your financial health", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = "Security", tint = Color(0xFF009688))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Secure cloud storage for all data", fontSize = 16.sp)
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Version: 1.0.0",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onBackClick() }) {
            Text(text = "Back")
        }
    }
}
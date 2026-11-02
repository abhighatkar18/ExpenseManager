package com.example.expensemanager.budget

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.expensemanager.NotificationReceiver
import com.example.expensemanager.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build

fun saveScheduledTransaction(context: Context, transactionName: String, amount: Double, date: LocalDate) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser!!.uid
    val scheduledTransactionsRef = firestore.collection("users").document(userId).collection("scheduled_transactions")


    val transactionData = hashMapOf(
        "transactionName" to transactionName,
        "amount" to amount,
        "date" to date.toString()
    )

    scheduledTransactionsRef.add(transactionData)
        .addOnSuccessListener {
            Toast.makeText(context, "Transaction scheduled successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
}



fun futureNotification(context: Context, transactionName: String, amount: String, date: LocalDate) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("transactionName", transactionName)
        putExtra("amount", amount)
    }

    val calendar = java.util.Calendar.getInstance().apply {
        set(date.year, date.monthValue - 1, date.dayOfMonth, 9, 0) // Set to 9 AM, adjust as needed
        if (timeInMillis < System.currentTimeMillis()) {
            add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        1001, // Unique request code
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        requestExactAlarmPermission(context) // Request permission if not granted
        return
    }

    try {
        // Schedule the notification
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        Toast.makeText(context, "Notification scheduled for $transactionName", Toast.LENGTH_SHORT).show()
    } catch (e: SecurityException) {
        Toast.makeText(context, "Failed to schedule notification: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}



@Composable
fun MainScreen() {
    // State to determine which screen to show
    var isViewingScheduledTransactions by remember { mutableStateOf(false) }

    if (isViewingScheduledTransactions) {
        ViewScheduledTransactionsScreen { isViewingScheduledTransactions = false } // Pass a lambda to go back
    } else {
        ScheduleTransactionScreen { isViewingScheduledTransactions = true } // Pass a lambda to navigate to the next screen
    }
}

@Composable
fun ScheduleTransactionScreen(onNavigate: () -> Unit) {
    val context = LocalContext.current // Get context here and pass it to functions
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var amount by remember { mutableStateOf("") }
    var transactionName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text(
            text = "Schedule a Future Transaction",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = transactionName,
            onValueChange = { transactionName = it },
            label = { Text("Transaction Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Enter Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date Picker
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Selected Date: ${selectedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))}")
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(onClick = {
                val datePicker = android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        selectedDate = LocalDate.of(year, month + 1, dayOfMonth) // month is 0-based
                    },
                    selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth
                )
                datePicker.show()
            }) {
                Icon(painter = painterResource(id = R.drawable.calendar), contentDescription = "Select Date", tint = Color.Blue, modifier = Modifier.height(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (transactionName.isNotEmpty() && amount.isNotEmpty()) {
                // Check if we need the permission to set exact alarms
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Check if the permission is granted
                    if (context.checkSelfPermission(android.Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                        requestExactAlarmPermission(context) // Request permission
                    } else {
                        // Schedule the notification
                        futureNotification(context, transactionName, amount, selectedDate)
                        saveScheduledTransaction(context, transactionName, amount.toDouble(), selectedDate)
                    }
                } else {
                    // Schedule the notification for pre-Android 12 versions
                    futureNotification(context, transactionName, amount, selectedDate)
                    saveScheduledTransaction(context, transactionName, amount.toDouble(), selectedDate)
                }
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Schedule Transaction")
        }


        Spacer(modifier = Modifier.height(20.dp))

        // Button to view scheduled transactions
        Button(onClick = {
            onNavigate() // Navigate to view transactions
        }) {
            Text(text = "View Scheduled Transactions")
        }
    }
}

@Composable
fun ViewScheduledTransactionsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser!!.uid
    val scheduledTransactionsRef = firestore.collection("users").document(userId).collection("scheduled_transactions")

    // State for scheduled transactions
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }

    // Fetch scheduled transactions from Firestore
    scheduledTransactionsRef.get().addOnSuccessListener { snapshot ->
        transactions = snapshot.documents.map { doc ->
            Transaction(
                id = doc.id,
                transactionName = doc.getString("transactionName") ?: "",
                amount = doc.getDouble("amount") ?: 0.0,
                date = LocalDate.parse(doc.getString("date"))
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text(text = "Scheduled Transactions", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        transactions.forEach { transaction ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "${transaction.transactionName} - $${transaction.amount} on ${transaction.date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))}")
                Spacer(modifier = Modifier.weight(1f))

                // Update button
                IconButton(onClick = {
                    // Implement update logic
                }) {
                    Icon(painter = painterResource(id = R.drawable.pen), contentDescription = "Edit")
                }

                // Delete button
                IconButton(onClick = {
                    // Implement delete logic
                    scheduledTransactionsRef.document(transaction.id).delete()
                }) {
                    Icon(painter = painterResource(id = R.drawable.bin), contentDescription = "Delete")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Button to go back
        Button(onClick = {
            onNavigateBack() // Go back to the previous screen
        }) {
            Text(text = "Back")
        }
    }
}
data class Transaction(
    val id: String,
    val transactionName: String,
    val amount: Double,
    val date: LocalDate
)
fun requestExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "No special permissions required for this version.", Toast.LENGTH_SHORT).show()
    }
}

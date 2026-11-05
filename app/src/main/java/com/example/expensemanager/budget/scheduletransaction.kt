package com.example.expensemanager.budget

import android.content.Context
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
import com.example.expensemanager.format
import com.example.expensemanager.parseAmount
fun scheduleTransaction(context: Context, name: String, amount: Double, date: LocalDate) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val docRef = FirebaseFirestore.getInstance()
        .collection("users").document(userId)
        .collection("scheduled_transactions").document()

    // Alarm first: if exact alarms are not allowed, nothing is saved
    if (!futureNotification(context, name, amount.format(), date, docRef.id.hashCode())) return

    docRef.set(
        hashMapOf(
            "transactionName" to name,
            "amount" to amount,
            "date" to date.toString()
        )
    ).addOnSuccessListener {
        Toast.makeText(context, "Transaction scheduled successfully", Toast.LENGTH_SHORT).show()
    }.addOnFailureListener { e ->
        cancelScheduledAlarm(context, docRef.id)
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// Returns true if the alarm was set
fun futureNotification(context: Context, name: String, amount: String, date: LocalDate, requestCode: Int): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        Toast.makeText(context, "Allow exact alarms for FINO, then tap Schedule again", Toast.LENGTH_LONG).show()
        requestExactAlarmPermission(context)
        return false
    }

    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("transactionName", name)
        putExtra("amount", amount)
        putExtra("notificationId", requestCode)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = java.util.Calendar.getInstance().apply {
        set(date.year, date.monthValue - 1, date.dayOfMonth, 9, 0, 0)
        if (timeInMillis < System.currentTimeMillis()) add(java.util.Calendar.DAY_OF_MONTH, 1)
    }

    return try {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        true
    } catch (e: SecurityException) {
        Toast.makeText(context, "Failed to schedule reminder: ${e.message}", Toast.LENGTH_SHORT).show()
        false
    }
}

fun cancelScheduledAlarm(context: Context, docId: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pi = PendingIntent.getBroadcast(
        context, docId.hashCode(), Intent(context, NotificationReceiver::class.java),
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pi != null) {
        alarmManager.cancel(pi)
        pi.cancel()
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
            val amountValue = parseAmount(amount)
            when {
                transactionName.isBlank() || amountValue == null ->
                    Toast.makeText(context, "Enter a name and an amount greater than 0", Toast.LENGTH_SHORT).show()
                selectedDate.isBefore(LocalDate.now()) ->
                    Toast.makeText(context, "Pick today or a future date", Toast.LENGTH_SHORT).show()
                else -> scheduleTransaction(context, transactionName.trim(), amountValue, selectedDate)
            }
        })  {
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
                Text(text = "${transaction.transactionName} - ${transaction.amount.format()} on ${transaction.date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))}")
                Spacer(modifier = Modifier.weight(1f))

                // Update button
                IconButton(onClick = {
                    // Implement update logic
                }) {
                    Icon(painter = painterResource(id = R.drawable.pen), contentDescription = "Edit")
                }

                // Delete button

                IconButton(onClick = {
                    cancelScheduledAlarm(context, transaction.id)
                    scheduledTransactionsRef.document(transaction.id).delete()
                    transactions = transactions.filter { it.id != transaction.id }
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

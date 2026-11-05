package com.example.expensemanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val transactionName = intent.getStringExtra("transactionName") ?: "Transaction"
        val amount = intent.getStringExtra("amount") ?: "0.0"

        // Check if the notification permission is granted
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val notificationBuilder = NotificationCompat.Builder(context, "scheduled_transaction_channel")
                .setSmallIcon(R.drawable.notification) // Your notification icon
                .setContentTitle("Scheduled Transaction")
                .setContentText("$transactionName of amount $amount is scheduled.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            // Use a try-catch block to handle potential SecurityException
            try {
                NotificationManagerCompat.from(context).notify(
                    intent.getIntExtra("notificationId", 1001),
                    notificationBuilder.build()
                )
            } catch (e: SecurityException) {
                // Handle the exception (e.g., log it or show a message to the user)
                e.printStackTrace() // Replace this with your logging mechanism
            }
        } else {
            // Handle the case where permission is not granted (e.g., request permission)
            // You might want to show a message or prompt the user to enable notifications
        }
    }
}

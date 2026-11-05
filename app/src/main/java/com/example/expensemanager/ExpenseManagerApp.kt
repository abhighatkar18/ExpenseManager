
package com.example.expensemanager

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import android.app.NotificationChannel
import android.app.NotificationManager
class ExpenseManagerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Check whether this is a debug build
        val isDebugBuild =
            (applicationInfo.flags and
                    ApplicationInfo.FLAG_DEBUGGABLE) != 0

        if (isDebugBuild) {

            // Install App Check debug provider
            FirebaseAppCheck.getInstance()
                .installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )

            Log.d(
                "AppCheckSetup",
                "Firebase App Check debug provider installed successfully"
            )

        } else {

            Log.d(
                "AppCheckSetup",
                "Release build: configure a production App Check provider"
            )
        }
        createNotificationChannels()
    }
    private fun createNotificationChannels() {
        // minSdk is 26, so channels are always supported
        val manager = getSystemService(NotificationManager::class.java)
        listOf(
            NotificationChannel("limit_exceeded_channel", "Spending limits", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Alerts when a category limit is exceeded" },
            NotificationChannel("scheduled_transaction_channel", "Scheduled transactions", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Reminders for scheduled transactions" },
            NotificationChannel("goal_reminder_channel", "Savings goals", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Monthly reminders to add to your savings goal" }
        ).forEach { manager.createNotificationChannel(it) }
    }
}

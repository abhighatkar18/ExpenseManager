
package com.example.expensemanager

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log

import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

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
    }
}

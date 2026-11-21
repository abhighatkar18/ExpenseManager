
package com.example.expensemanager

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.expensemanager.ui.auth.LoginScreen
import com.example.expensemanager.ui.theme.ExmanTheme

class loginpage : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ExmanTheme {
                LoginScreen(
                    onSignUp = {
                        startActivity(
                            Intent(this, signUp::class.java)
                        )
                        finish()
                    },

                    onAuthenticated = {
                        startActivity(
                            Intent(this, Dashboard::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        )
                        finish()
                    }
                )
            }
        }
    }
}

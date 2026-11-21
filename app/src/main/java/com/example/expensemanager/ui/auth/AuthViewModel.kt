
package com.example.expensemanager.ui.auth

import android.app.Application
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.expensemanager.data.auth.AuthRepository

class AuthViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repo = AuthRepository(application.applicationContext)

    var busy by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var notice by mutableStateOf<String?>(null)
        private set

    fun clearFeedback() {
        error = null
        notice = null
    }

    private fun validEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        if (busy) return
        clearFeedback()

        when {
            !validEmail(email) -> {
                error = "Enter a valid email address"
                return
            }
            password.isBlank() -> {
                error = "Enter your password"
                return
            }
        }

        busy = true

        repo.signIn(email.trim(), password) { result ->
            busy = false

            result.onSuccess {
                onSuccess()
            }.onFailure {
                error = it.localizedMessage ?: "Unable to sign in"
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        confirm: String,
        onSuccess: () -> Unit
    ) {
        if (busy) return
        clearFeedback()

        when {
            name.trim().length < 2 ->
                error = "Enter your full name"

            !validEmail(email) ->
                error = "Enter a valid email address"

            password.length < 6 ->
                error = "Password must have at least 6 characters"

            password != confirm ->
                error = "Passwords do not match"

            else -> {
                busy = true

                repo.signUp(
                    name.trim(),
                    email.trim(),
                    password
                ) { result ->
                    busy = false

                    result.onSuccess {
                        onSuccess()
                    }.onFailure {
                        error = it.localizedMessage
                            ?: "Unable to create account"
                    }
                }
            }
        }
    }

    fun reset(email: String) {
        if (busy) return
        clearFeedback()

        if (!validEmail(email)) {
            error = "Enter your registered email address"
            return
        }

        busy = true

        repo.resetPassword(email.trim()) { result ->
            busy = false

            result.onSuccess {
                notice = "If this email is registered, a reset link will be sent."
            }.onFailure {
                error = "Unable to send reset email. Please try again."
            }
        }
    }
}


package com.example.expensemanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensemanager.databinding.ActivityLoginpageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class loginpage : AppCompatActivity() {

    private lateinit var binding: ActivityLoginpageBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var logbtn: Button
    private lateinit var signbtn: Button
    private lateinit var forgetbtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityLoginpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize buttons
        logbtn = findViewById(R.id.login)
        signbtn = findViewById(R.id.sign)
        forgetbtn = findViewById(R.id.forget)

        // Forgot Password
        forgetbtn.setOnClickListener {
            resetPassword()
        }

        // Navigate to Sign Up
        signbtn.setOnClickListener {
            startActivity(
                Intent(this, signUp::class.java)
            )
            finish()
        }

        // Login
        logbtn.setOnClickListener {
            loginUser()
        }
    }

    // =============================
    // LOGIN FUNCTION
    // =============================

    private fun loginUser() {

        val email = binding.email.editText
            ?.text?.toString()?.trim().orEmpty()

        val password = binding.password.editText
            ?.text?.toString().orEmpty()

        // Validate empty fields
        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Please fill all the credentials")
            return
        }

        // Validate email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage("Please enter a valid email address")
            return
        }

        // Disable login button while processing
        logbtn.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                val uid = auth.currentUser?.uid

                if (uid == null) {
                    logbtn.isEnabled = true
                    showMessage("Unable to retrieve user information")
                    return@addOnSuccessListener
                }

                // Retrieve user information from Firestore
                db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { document ->

                        logbtn.isEnabled = true

                        if (document.exists()) {

                            val uname = document.getString("uname")
                                ?.takeIf { it.isNotBlank() }
                                ?: email.substringBefore("@")

                            // Save user information
                            val sharedPreferences =
                                getSharedPreferences(
                                    "Credentials",
                                    MODE_PRIVATE
                                )

                            sharedPreferences.edit()
                                .putString("uname", uname)
                                .putString("uemail", email)
                                .apply()

                            showMessage("Login successful")

                            // Navigate to Dashboard
                            val intent = Intent(
                                this@loginpage,
                                Dashboard::class.java
                            )

                            startActivity(intent)
                            finish()

                        } else {
                            showMessage(
                                "User profile not found in database"
                            )
                        }
                    }
                    .addOnFailureListener { exception ->

                        logbtn.isEnabled = true

                        Log.e(
                            "LoginPage",
                            "Firestore error",
                            exception
                        )

                        showMessage(
                            "Unable to load profile. Please try again."
                        )
                    }
            }
            .addOnFailureListener { exception ->

                logbtn.isEnabled = true

                Log.e(
                    "LoginPage",
                    "Authentication failed",
                    exception
                )

                showMessage(
                    "Login failed: ${
                        exception.localizedMessage
                            ?: "Please check your credentials"
                    }"
                )
            }
    }

    // =============================
    // FORGOT PASSWORD
    // =============================

    private fun resetPassword() {

        val email = binding.email.editText
            ?.text?.toString()?.trim().orEmpty()

        if (email.isEmpty()) {
            showMessage("Please enter your registered email")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage("Please enter a valid email address")
            return
        }

        forgetbtn.isEnabled = false

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {

                forgetbtn.isEnabled = true

                showMessage(
                    "If the email is registered, a password reset link will be sent."
                )
            }
            .addOnFailureListener { exception ->

                forgetbtn.isEnabled = true

                Log.e(
                    "LoginPage",
                    "Password reset failed",
                    exception
                )

                showMessage(
                    "Unable to process password reset. Please try again."
                )
            }
    }

    // =============================
    // TOAST MESSAGE
    // =============================

    private fun showMessage(message: String) {
        Toast.makeText(
            this@loginpage,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}

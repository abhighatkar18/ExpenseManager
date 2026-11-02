
package com.example.expensemanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensemanager.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class signUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var logbtn: Button
    private lateinit var signinbtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize buttons
        logbtn = findViewById(R.id.login)
        signinbtn = findViewById(R.id.signin)

        // Register user
        signinbtn.setOnClickListener {
            registerUser()
        }

        // Navigate to Login
        logbtn.setOnClickListener {
            startActivity(
                Intent(this, loginpage::class.java)
            )
            finish()
        }
    }

    // =============================
    // USER REGISTRATION
    // =============================

    private fun registerUser() {

        val name = binding.Name.editText
            ?.text?.toString()?.trim().orEmpty()

        val email = binding.email.editText
            ?.text?.toString()?.trim().orEmpty()

        val password = binding.password.editText
            ?.text?.toString().orEmpty()

        // Validate empty fields
        if (name.isEmpty() ||
            email.isEmpty() ||
            password.isEmpty()
        ) {
            showMessage("Please fill all the credentials")
            return
        }

        // Validate name
        if (name.length < 2) {
            showMessage("Please enter a valid name")
            return
        }

        // Validate email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage("Please enter a valid email address")
            return
        }

        // Validate password
        if (password.length < 6) {
            showMessage(
                "Password must contain at least 6 characters"
            )
            return
        }

        // Prevent multiple registration requests
        signinbtn.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val uid = result.user?.uid

                if (uid == null) {
                    signinbtn.isEnabled = true
                    showMessage(
                        "Unable to retrieve user information"
                    )
                    return@addOnSuccessListener
                }

                // Save user profile in Firestore
                saveUserData(uid, name, email)
            }
            .addOnFailureListener { exception ->

                signinbtn.isEnabled = true

                Log.e(
                    "SignUp",
                    "Registration failed",
                    exception
                )

                showMessage(
                    "Registration failed: ${
                        exception.localizedMessage
                            ?: "Please try again"
                    }"
                )
            }
    }

    // =============================
    // SAVE USER DATA TO FIRESTORE
    // =============================

    private fun saveUserData(
        uid: String,
        name: String,
        email: String
    ) {

        // Do not store passwords in Firestore
        val userData = hashMapOf(
            "uname" to name,
            "email" to email
        )

        // Initial financial values
        val totalsData = hashMapOf(
            "totalBalance" to 0.0,
            "totalIncome" to 0.0,
            "totalExpense" to 0.0
        )

        val userDocRef = db.collection("users")
            .document(uid)

        val totalsDocRef = userDocRef
            .collection("totals")
            .document("totalsDoc")

        // Save both documents together
        val batch = db.batch()

        batch.set(userDocRef, userData)
        batch.set(totalsDocRef, totalsData)

        batch.commit()
            .addOnSuccessListener {

                signinbtn.isEnabled = true

                // Save user preferences
                val sharedPreferences =
                    getSharedPreferences(
                        "Credentials",
                        MODE_PRIVATE
                    )

                sharedPreferences.edit()
                    .putString("uname", name)
                    .putString("uemail", email)
                    .apply()

                showMessage(
                    "Account created successfully"
                )

                // Navigate to Dashboard
                val intent = Intent(
                    this@signUp,
                    Dashboard::class.java
                )

                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->

                signinbtn.isEnabled = true

                Log.e(
                    "SignUp",
                    "Firestore profile setup failed",
                    exception
                )

                showMessage(
                    "Account created, but profile setup failed. " +
                            "Please check your connection and Firestore access."
                )
            }
    }

    // =============================
    // SHOW MESSAGE
    // =============================

    private fun showMessage(message: String) {
        Toast.makeText(
            this@signUp,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}

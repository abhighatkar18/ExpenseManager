
package com.example.expensemanager.data.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AuthRepository(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun signIn(
        email: String,
        password: String,
        done: (Result<Unit>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid

                if (uid == null) {
                    done(Result.failure(
                        IllegalStateException("Unable to find your account")
                    ))
                    return@addOnSuccessListener
                }

                val profile = db.collection("users").document(uid)

                profile.get()
                    .addOnSuccessListener { doc ->
                        val name = doc.getString("uname")
                            ?.takeIf { it.isNotBlank() }
                            ?: email.substringBefore('@')

                        if (doc.exists()) {
                            saveCredentials(name, email)
                            done(Result.success(Unit))
                        } else {
                            profile.set(
                                mapOf(
                                    "uname" to name,
                                    "email" to email
                                ),
                                SetOptions.merge()
                            ).addOnSuccessListener {
                                saveCredentials(name, email)
                                done(Result.success(Unit))
                            }.addOnFailureListener {
                                failSignIn(it, done)
                            }
                        }
                    }
                    .addOnFailureListener {
                        failSignIn(it, done)
                    }
            }
            .addOnFailureListener {
                done(Result.failure(it))
            }
    }

    fun signUp(
        name: String,
        email: String,
        password: String,
        done: (Result<Unit>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user

                if (user == null) {
                    done(Result.failure(
                        IllegalStateException("Unable to create account")
                    ))
                    return@addOnSuccessListener
                }

                val profile = db.collection("users")
                    .document(user.uid)

                val totals = profile.collection("totals")
                    .document("totalsDoc")

                db.batch().apply {
                    set(
                        profile,
                        mapOf(
                            "uname" to name,
                            "email" to email
                        )
                    )

                    set(
                        totals,
                        mapOf(
                            "totalBalance" to 0.0,
                            "totalIncome" to 0.0,
                            "totalExpense" to 0.0
                        )
                    )
                }.commit()
                    .addOnSuccessListener {
                        saveCredentials(name, email)
                        done(Result.success(Unit))
                    }
                    .addOnFailureListener { error ->
                        user.delete().addOnCompleteListener { task ->
                            auth.signOut()

                            val message = if (task.isSuccessful) {
                                "Profile setup failed. Please try again."
                            } else {
                                "Account created, but setup failed. Sign in to finish setup."
                            }

                            done(Result.failure(
                                IllegalStateException(message, error)
                            ))
                        }
                    }
            }
            .addOnFailureListener {
                done(Result.failure(it))
            }
    }

    fun resetPassword(
        email: String,
        done: (Result<Unit>) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                done(Result.success(Unit))
            }
            .addOnFailureListener {
                done(Result.failure(it))
            }
    }

    private fun failSignIn(
        error: Exception,
        done: (Result<Unit>) -> Unit
    ) {
        auth.signOut()

        done(Result.failure(
            IllegalStateException(
                "Unable to load your profile. Try again.",
                error
            )
        ))
    }

    private fun saveCredentials(name: String, email: String) {
        context.getSharedPreferences(
            "Credentials",
            Context.MODE_PRIVATE
        ).edit()
            .putString("uname", name)
            .putString("uemail", email)
            .apply()
    }
}

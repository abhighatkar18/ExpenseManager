
package com.example.expensemanager.data.transactions

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

data class TransactionInput(
    val amount: Double,
    val type: String,
    val category: String,
    val title: String
)

class TransactionRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun save(
        input: TransactionInput,
        onResult: (Result<Unit>) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onResult(
                Result.failure(
                    IllegalStateException("Please sign in again.")
                )
            )
            return
        }

        if (
            !input.amount.isFinite() ||
            input.amount <= 0.0 ||
            input.type !in listOf("income", "expense")
        ) {
            onResult(
                Result.failure(
                    IllegalArgumentException("Invalid transaction.")
                )
            )
            return
        }

        val userRef = db.collection("users").document(uid)

        val transactionRef = userRef
            .collection("transactions")
            .document()

        val totalsRef = userRef
            .collection("totals")
            .document("totalsDoc")

        val isIncome = input.type == "income"
        val difference = if (isIncome) {
            input.amount
        } else {
            -input.amount
        }

        val totals = mutableMapOf<String, Any>(
            "totalBalance" to FieldValue.increment(difference)
        )

        if (isIncome) {
            totals["totalIncome"] =
                FieldValue.increment(input.amount)
        } else {
            totals["totalExpense"] =
                FieldValue.increment(input.amount)
        }

        val transaction = hashMapOf<String, Any>(
            "title" to input.title,
            "amount" to input.amount,
            "type" to input.type,
            "category" to input.category,
            "date" to Timestamp.now(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.batch().apply {
            set(transactionRef, transaction)
            set(totalsRef, totals, SetOptions.merge())
        }.commit()
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { error ->
                onResult(Result.failure(error))
            }
    }
}

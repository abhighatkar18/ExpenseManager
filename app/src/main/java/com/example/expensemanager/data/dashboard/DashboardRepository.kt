
package com.example.expensemanager.data.dashboard

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Date

data class ExmanTransaction(
    val id: String,
    val title: String,
    val category: String,
    val amount: Double,
    val income: Boolean,
    val date: Date?
)

data class DashboardState(
    val name: String = "there",
    val balance: Double = 0.0,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val transactions: List<ExmanTransaction> = emptyList(),
    val loadingBalance: Boolean = true,
    val loadingTransactions: Boolean = true,
    val balanceError: String? = null,
    val transactionError: String? = null
)

class DashboardRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val listeners = mutableListOf<ListenerRegistration>()

    fun observe(onChange: (DashboardState) -> Unit) {
        stop()

        val user = auth.currentUser

        if (user == null) {
            onChange(
                DashboardState(
                    loadingBalance = false,
                    loadingTransactions = false,
                    balanceError = "Please sign in again."
                )
            )
            return
        }

        var state = DashboardState(
            name = user.email?.substringBefore("@") ?: "there"
        )

        fun emit(next: DashboardState) {
            state = next
            onChange(next)
        }

        val userRef = firestore.collection("users")
            .document(user.uid)

        // User profile
        listeners += userRef.addSnapshotListener { doc, error ->
            if (error == null && doc != null) {
                emit(
                    state.copy(
                        name = doc.getString("uname")
                            ?.takeIf { it.isNotBlank() }
                            ?: state.name
                    )
                )
            }
        }

        // Existing Exman totals document
        listeners += userRef.collection("totals")
            .document("totalsDoc")
            .addSnapshotListener { doc, error ->

                if (error != null) {
                    emit(
                        state.copy(
                            loadingBalance = false,
                            balanceError = "Unable to load balance."
                        )
                    )
                    return@addSnapshotListener
                }

                fun amount(key: String): Double {
                    return (doc?.get(key) as? Number)
                        ?.toDouble() ?: 0.0
                }

                emit(
                    state.copy(
                        balance = amount("totalBalance"),
                        income = amount("totalIncome"),
                        expense = amount("totalExpense"),
                        loadingBalance = false,
                        balanceError = null
                    )
                )
            }

        // Compatible with existing transaction documents.
        listeners += userRef.collection("transactions")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    emit(
                        state.copy(
                            loadingTransactions = false,
                            transactionError =
                                "Unable to load recent activity."
                        )
                    )
                    return@addSnapshotListener
                }

                val records = snapshot?.documents.orEmpty()
                    .map { doc ->

                        val type = doc.getString("type")
                            ?: doc.getString("transactionType")
                            ?: "expense"

                        val date = listOf(
                            "date",
                            "createdAt",
                            "timestamp"
                        ).firstNotNullOfOrNull { field ->
                            when (val value = doc.get(field)) {
                                is Timestamp -> value.toDate()
                                is Date -> value
                                else -> null
                            }
                        }

                        ExmanTransaction(
                            id = doc.id,
                            title = doc.getString("title")
                                ?: doc.getString("name")
                                ?: doc.getString("category")
                                ?: "Transaction",
                            category = doc.getString("category")
                                ?: "Other",
                            amount = (doc.get("amount") as? Number)
                                ?.toDouble() ?: 0.0,
                            income = type.equals(
                                "income",
                                ignoreCase = true
                            ),
                            date = date
                        )
                    }
                    .sortedWith(
                        compareByDescending<ExmanTransaction> {
                            it.date?.time ?: Long.MIN_VALUE
                        }.thenByDescending { it.id }
                    )

                emit(
                    state.copy(
                        transactions = records,
                        loadingTransactions = false,
                        transactionError = null
                    )
                )
            }
    }

    fun stop() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }
}

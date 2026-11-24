
package com.example.expensemanager.ui.transactions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.expensemanager.data.transactions.TransactionInput
import com.example.expensemanager.data.transactions.TransactionRepository
import java.math.BigDecimal
import java.math.RoundingMode

class AddTransactionViewModel : ViewModel() {

    private val repository = TransactionRepository()

    var saving by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun clearError() {
        error = null
    }

    fun save(
        amount: String,
        type: String,
        category: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        if (saving) return

        error = null

        val parsed = amount.toBigDecimalOrNull()

        when {
            parsed == null ||
                    parsed <= BigDecimal.ZERO -> {
                error = "Enter an amount greater than zero."
                return
            }

            parsed > BigDecimal("999999999.99") -> {
                error = "Amount is too large."
                return
            }

            category.isBlank() -> {
                error = "Please select a category."
                return
            }
        }

        val finalAmount = parsed!!
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()

        saving = true

        repository.save(
            TransactionInput(
                amount = finalAmount,
                type = type,
                category = category,
                title = description.trim().ifBlank { category }
            )
        ) { result ->
            saving = false

            result.onSuccess {
                onSuccess()
            }.onFailure {
                error = it.localizedMessage
                    ?: "Unable to save transaction."
            }
        }
    }
}

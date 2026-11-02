package com.example.expensemanager.profile

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

fun generateUserReport(context: Context, userId: String, callback: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userTransactionsRef = db.collection("users").document(userId).collection("transactions")

    userTransactionsRef.get().addOnSuccessListener { documents ->
        val reportBuilder = StringBuilder()
        reportBuilder.append("User Transaction Report\n\n")
        reportBuilder.append("Total Transactions: ${documents.size()}\n\n")

        var totalExpenses = 0.0
        var totalIncome = 0.0
        val categoryWiseData = mutableMapOf<String, Double>()

        for (doc in documents) {
            val amount = doc.getDouble("amount") ?: 0.0
            val category = doc.getString("category") ?: "Unknown"
            val type = doc.getString("type") ?: "Expense"
            val date = doc.getString("date") ?: "Unknown"

            reportBuilder.append("Type: $type\n")
            reportBuilder.append("Amount: $amount\n")
            reportBuilder.append("Category: $category\n")
            reportBuilder.append("Date: $date\n\n")

            if (type == "Expense") {
                totalExpenses += amount
                categoryWiseData[category] = categoryWiseData.getOrDefault(category, 0.0) + amount
            } else if (type == "Income") {
                totalIncome += amount
            }
        }

        reportBuilder.append("\nSummary:\n")
        reportBuilder.append("Total Income: ₹$totalIncome\n")
        reportBuilder.append("Total Expenses: ₹$totalExpenses\n")

        reportBuilder.append("\nCategory-wise Breakdown:\n")
        for ((category, totalAmount) in categoryWiseData) {
            reportBuilder.append("$category: ₹$totalAmount\n")
        }

        callback(reportBuilder.toString())
    }.addOnFailureListener { exception ->
        Toast.makeText(context, "Error generating report: ${exception.message}", Toast.LENGTH_SHORT).show()
        callback("Error generating report.")
    }
}





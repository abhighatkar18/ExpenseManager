package com.example.expensemanager.profile

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
// Global variable to hold CSV data
var csvData: String? = null

fun fetchUserDataAndExportCSV(context: Context, userId: String, onDataReady: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val transactionsRef = db.collection("users").document(userId).collection("transactions")

    transactionsRef.get().addOnSuccessListener { snapshot ->
        if (!snapshot.isEmpty) {
            val userData = snapshot.documents.map { it.data }
            csvData = generateCSVFromData(userData)
            onDataReady()
        } else {
            Toast.makeText(context, "No transactions found for user", Toast.LENGTH_SHORT).show()
        }
    }.addOnFailureListener {
        Toast.makeText(context, "Error fetching user data", Toast.LENGTH_SHORT).show()
    }
}

fun saveCSVToUri(context: Context, uri: Uri) {
    val outputStream = context.contentResolver.openOutputStream(uri)
    outputStream?.write(csvData?.toByteArray())
    outputStream?.close()
}

fun generateCSVFromData(userData: List<Map<String, Any>?>): String {
    val stringBuilder = StringBuilder()
    stringBuilder.append("Income/Expense,Name,Amount,Category,Date,Description,Receipt URI\n")

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    userData.forEach { transaction ->
        if (transaction != null) {
            val type = transaction["type"] as? String ?: ""         // Income or Expense
            val name = transaction["name"] as? String ?: ""         // Transaction name
            val amount = transaction["amount"] as? Number ?: 0     // Transaction amount
            val category = transaction["category"] as? String ?: "" // Category (for expense)
            val date = transaction["date"] as? String ?: ""         // Transaction date
            val description = transaction["description"] as? String ?: "" // Transaction description (optional)
            val receiptUri = transaction["receiptUri"]?.toString() ?: "" // Receipt URI (optional)

            val formattedDate = if (date.isNotEmpty()) {
                try {
                    val parsedDate = SimpleDateFormat("d/M/yyyy", Locale.getDefault()).parse(date)
                    "\"${dateFormat.format(parsedDate!!)}\""
                } catch (e: Exception) {
                    date
                }
            } else {
//                ""
                date
            }
            stringBuilder.append("$type,$name,$amount,$category,$formattedDate,$description,$receiptUri\n")
        }
    }

    return stringBuilder.toString()
}
package com.example.expensemanager.budget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanager.DropdownMenuCategories
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Limit(val category: String, val amount: Double, val type: String)

@Composable
fun SetLimitsScreen() {
    val context = LocalContext.current
    var category by remember { mutableStateOf("") }
    var limitAmount by remember { mutableStateOf("") }
    var limitType by remember { mutableStateOf("Daily") }
    var limitsList by remember { mutableStateOf<List<Limit>>(emptyList()) }
    var selectedLimit by remember { mutableStateOf<Limit?>(null) }
    val categories = listOf("Food","Housing", "Entertainment","Transportation", "Investment", "Bills","Healthcare", "Subscription", "Other")
    val notificationChannelId = "limit_exceeded_channel"
    val scope = rememberCoroutineScope()
    // Create Notification Channel
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Limit Exceeded Notification",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification when a category limit is exceeded"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        // Load existing limits
        limitsList = getAllLimits(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)

//            .verticalScroll(rememberScrollState())
        ,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Set Limit", fontSize = 26.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))

        DropdownMenuCategories(
            selectedCategory = category,
            categories = categories,
            onCategorySelected = { category = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = limitAmount,
            onValueChange = { limitAmount = it },
            label = { Text("Set Limit Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Limit Type", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            RadioButton(selected = limitType == "Daily", onClick = { limitType = "Daily" })
            Text("Daily", modifier = Modifier.padding(start = 8.dp))

            Spacer(modifier = Modifier.width(20.dp))

            RadioButton(selected = limitType == "Monthly", onClick = { limitType = "Monthly" })
            Text("Monthly", modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Button to save or update the limit
        Button(
            onClick = {
                if (category.isNotEmpty() && limitAmount.isNotEmpty()) {
                    if (selectedLimit != null) {
                        // Update existing limit
                        updateLimit(context, selectedLimit!!.copy(amount = limitAmount.toDouble()), notificationChannelId)
                    } else {
                        // Save new limit
                        saveLimit(context, category, limitAmount.toDouble(), limitType, notificationChannelId)
                    }
                } else {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                if (selectedLimit != null) "Update Limit" else "Save Limit",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Existing Limits", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        limitsList.forEach { limit ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                Text("${limit.category}: ${limit.amount} (${limit.type})", fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    selectedLimit = limit // Set selected limit to edit
                    category = limit.category
                    limitAmount = limit.amount.toString()
                    limitType = limit.type
                }) {
                    Text("Edit")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(onClick = {
                    scope.launch {
                        removeLimit(context, limit) // Call suspend function inside coroutine
                        limitsList = getAllLimits(context) // Refresh the limits list
                    } // Refresh the limits list
                }) {
                    Text("Remove")
                }
            }
        }
    }
}


fun updateLimit(context: Context, updatedLimit: Limit, channelId: String) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("users")
        .document(userId)
        .collection("limits")
        .whereEqualTo("category", updatedLimit.category)
        .whereEqualTo("type", updatedLimit.type)
        .get()
        .addOnSuccessListener { documents ->
            for (document in documents) {
                firestore.collection("users")
                    .document(userId)
                    .collection("limits")
                    .document(document.id)
                    .update("amount", updatedLimit.amount)
                    .addOnSuccessListener {
                        Toast.makeText(context, "${updatedLimit.category} limit updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error updating limit: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error fetching limits for update: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

fun saveLimit(context: Context, category: String, amount: Double, type: String, channelId: String) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()
    val limitData = hashMapOf(
        "category" to category,
        "amount" to amount,
        "type" to type
    )
    firestore.collection("users")
        .document(userId)
        .collection("limits")
        .add(limitData)
        .addOnSuccessListener {
            Toast.makeText(context, "$category limit saved successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error saving limit: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

suspend fun getAllLimits(context: Context): List<Limit> {
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid ?: return emptyList()
    val firestore = FirebaseFirestore.getInstance()

    val limitsList = mutableListOf<Limit>()
    try {
        val result = firestore.collection("users")
            .document(userId)
            .collection("limits")
            .get()
            .await()

        for (document in result.documents) {
            val category = document.getString("category") ?: ""
            val amount = document.getDouble("amount") ?: 0.0
            val type = document.getString("type") ?: ""

            limitsList.add(Limit(category, amount, type))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error fetching limits: ${e.message}", Toast.LENGTH_SHORT).show()
    }

    return limitsList
}

suspend fun removeLimit(context: Context, limit: Limit) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()

    try {
        val querySnapshot = firestore.collection("users")
            .document(userId)
            .collection("limits")
            .whereEqualTo("category", limit.category)
            .whereEqualTo("type", limit.type)
            .get()
            .await()

        for (document in querySnapshot.documents) {
            firestore.collection("users")
                .document(userId)
                .collection("limits")
                .document(document.id)
                .delete()
                .await()
        }
        Toast.makeText(context, "${limit.category} limit removed", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error removing limit: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

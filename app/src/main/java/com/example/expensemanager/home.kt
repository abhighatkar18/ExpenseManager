package com.example.expensemanager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import android.app.NotificationManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanager.ui.theme.green
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.lazy.items
import androidx.core.app.NotificationCompat
import com.github.mikephil.charting.data.PieEntry
import java.time.LocalDate

@Preview(showBackground = true)
@Composable
fun HomeScreenContent() {
    var monthTransactions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val context = LocalContext.current
    var totalBalance by remember { mutableStateOf(0.0) }
    var totalIncome by remember { mutableStateOf(0.0) }
    var totalExpense by remember { mutableStateOf(0.0) }
    var transactions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var showForm by remember { mutableStateOf(false) }
    var showAllTransactions by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        // Fetch totals
        firestore.collection("users")
            .document(userId)
            .collection("totals")
            .document("totalsDoc")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    totalBalance = document.getDouble("totalBalance") ?: 0.0
                    totalIncome = document.getDouble("totalIncome") ?: 0.0
                    totalExpense = document.getDouble("totalExpense") ?: 0.0
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }

        // FIX: latest 5 transactions for "Recent Transactions" (this block had been deleted)
        firestore.collection("users")
            .document(userId)
            .collection("transactions")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { result ->
                transactions = result.map { it.data }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }

        // Fetch this month's transactions for the pie chart
        val monthStart = LocalDate.now().withDayOfMonth(1)
        firestore.collection("users")
            .document(userId)
            .collection("transactions")
            .whereGreaterThanOrEqualTo("date", monthStart.toString())
            .whereLessThan("date", monthStart.plusMonths(1).toString())
            .get()
            .addOnSuccessListener { result ->
                monthTransactions = result.map { it.data }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    HomeScreen(
        totalBalance = totalBalance,
        totalIncome = totalIncome,
        totalExpense = totalExpense,
        transactions = transactions,
        pieTransactions = monthTransactions,
        showForm = showForm,
        showAllTransactions = showAllTransactions,
        onShowFormChange = { showForm = it },
        onShowAllTransactionsChange = { showAllTransactions = it },
        onTransactionSaved = { refreshKey++ }
    )
}

@Composable
fun HomeScreen(
    totalBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    transactions: List<Map<String, Any>>,
    pieTransactions: List<Map<String, Any>>,
    showForm: Boolean,
    showAllTransactions: Boolean,
    onShowFormChange: (Boolean) -> Unit,
    onShowAllTransactionsChange: (Boolean) -> Unit,
    onTransactionSaved: () -> Unit
) {
    val context = LocalContext.current
    var allTransactions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var pieChartData by remember { mutableStateOf<List<PieEntry>>(emptyList()) }
    // Update pieChartData whenever the month's transactions change
    pieChartData = createPieEntries(pieTransactions)

    LaunchedEffect(showAllTransactions) {
        if (showAllTransactions) {
            val firestore = FirebaseFirestore.getInstance()
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            firestore.collection("users")
                .document(userId)
                .collection("transactions")
                // FIX: removed .limit(5) so "See all" really shows everything
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    allTransactions = result.map { it.data }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(green)
                    .padding(start = 20.dp, top = 10.dp, bottom = 10.dp)
            ) {
                Spacer(modifier = Modifier.size(20.dp))
                Row {
                    Text(text = "FINO", fontSize = 40.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(220.dp))
                    Image(
                        painter = painterResource(id = R.drawable.notification),
                        contentDescription = null,
                        modifier = Modifier
                            .height(45.dp)
                            .width(30.dp)
                    )
                }

                Text(text = "By Abhishek Ghatkar", color = Color.White)

                Spacer(modifier = Modifier.height(40.dp))
                Column {
                    Row {
                        Column {
                            Text(text = "Total Balance")
                            Text(text = "₹${totalBalance.format()}", fontSize = 20.sp, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Row {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.income),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(text = "Total Income")
                            }
                            Text(text = "₹${totalIncome.format()}", fontSize = 20.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(110.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.expense),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(text = "Total Expense")
                            }
                            Text(text = "₹${totalExpense.format()}", fontSize = 20.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            DynamicPieChart(
                pieEntries = pieChartData,
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Column() {
                Row() {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 20.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 20.dp)
                    )
                    Spacer(modifier = Modifier.width(90.dp))
                    Text(text = "See all", fontSize = 20.sp, modifier = Modifier.clickable { onShowAllTransactionsChange(true) })
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
            ) {
                items(if (showAllTransactions) allTransactions else transactions) { transaction ->
                    TransactionItem(transaction)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        Button(
            onClick = { onShowFormChange(true) },
            colors = ButtonDefaults.buttonColors(containerColor = green),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(70.dp)
        ) {
            Text(text = "+", fontSize = 24.sp)
        }
    }

    if (showForm) {
        ExpenseIncomeForm(
            onDismiss = { onShowFormChange(false) },
            onSave = { type, name, amount, description, date, category, receiptUri ->
                val firestore = FirebaseFirestore.getInstance()
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                if (userId == null) {
                    Toast.makeText(context, "Please log in again", Toast.LENGTH_LONG).show()
                } else {
                    val userRef = firestore.collection("users").document(userId)
                    val transactionRef = userRef.collection("transactions").document()
                    val totalsRef = userRef.collection("totals").document("totalsDoc")
                    val isIncome = type == "Income"

                    val transactionData = hashMapOf(
                        "type" to type,
                        "name" to name,
                        "amount" to amount,
                        "description" to description,
                        "date" to date,
                        "category" to category,
                        "receiptUri" to receiptUri?.toString()
                    )
                    val totalsChange = hashMapOf<String, Any>(
                        "totalBalance" to FieldValue.increment(if (isIncome) amount else -amount),
                        "totalIncome" to FieldValue.increment(if (isIncome) amount else 0.0),
                        "totalExpense" to FieldValue.increment(if (isIncome) 0.0 else amount)
                    )

                    firestore.batch()
                        .set(transactionRef, transactionData)
                        .set(totalsRef, totalsChange, SetOptions.merge())
                        .commit()
                        .addOnSuccessListener {
                            onTransactionSaved()
                            if (!isIncome && category != null) {
                                // the new expense is already saved, so the query counts it
                                checkLimitExceeded(context, category, 0.0)
                            }
                            Toast.makeText(context, "Saved successfully", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }

                onShowFormChange(false)
            }
        )
    }
}

fun Double.format(): String {
    return String.format("%.2f", this)
}

@Composable
fun TransactionItem(transaction: Map<String, Any>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        val category = transaction["category"] as? String ?: "Other"
        val name = transaction["name"] as? String ?: "Unknown"
        val date = transaction["date"] as? String ?: "Unknown"
        val amount = (transaction["amount"] as? Number)?.toDouble() ?: 0.0
        val type = transaction["type"] as? String ?: "Expense"
        val categoryIcons = mapOf(
            "Housing" to R.drawable.house,
            "Food" to R.drawable.food,
            "Entertainment" to R.drawable.entertainment,
            "Transportation" to R.drawable.vehicles,
            "Investment" to R.drawable.investment,
            "Healthcare" to R.drawable.healthcare,
            "Bills" to R.drawable.bill,
            "Subscription" to R.drawable.subscription,
            "Others" to R.drawable.others,
            "Other" to R.drawable.others
        )
        val iconResId = categoryIcons[category] ?: categoryIcons["Other"]!!

        val amountText = if (type == "Income") "+₹${amount.format()}" else "-₹${amount.format()}"
        val amountColor = if (type == "Income") green else Color.Red
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = date, fontSize = 14.sp, color = Color.Gray)
        }
        Text(
            text = amountText,
            fontSize = 16.sp,
            color = amountColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// Function to convert transactions to PieEntry
private fun createPieEntries(transactions: List<Map<String, Any>>): List<PieEntry> {
    val categoryMap = mutableMapOf<String, Double>()
    for (transaction in transactions) {
        if (transaction["type"] == "Expense") {
            val category = transaction["category"] as? String ?: "Other"
            val amount = (transaction["amount"] as? Number)?.toDouble() ?: 0.0
            categoryMap[category] = categoryMap.getOrDefault(category, 0.0) + amount
        }
    }
    return categoryMap.map { PieEntry(it.value.toFloat(), it.key) }
}

fun checkLimitExceeded(context: Context, category: String, newExpenseAmount: Double) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()

    // Retrieve all transactions for the user in the specified category
    firestore.collection("users")
        .document(userId)
        .collection("transactions")
        .whereEqualTo("category", category)
        .get()
        .addOnSuccessListener { documents ->
            var totalSpentInCategory = newExpenseAmount
            for (document in documents) {
                val amount = (document.get("amount") as? Number)?.toDouble() ?: 0.0
                if (document.getString("type") == "Expense") {
                    totalSpentInCategory += amount
                }
            }
            checkCategoryLimit(context, category, totalSpentInCategory)
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error fetching transactions: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

private fun checkCategoryLimit(context: Context, category: String, totalSpent: Double) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()

    // Retrieve the user's limits from Firestore
    firestore.collection("users")
        .document(userId)
        .collection("limits")
        .whereEqualTo("category", category)
        .get()
        .addOnSuccessListener { documents ->
            for (document in documents) {
                val limitAmount = document.getDouble("amount") ?: 0.0
                if (totalSpent > limitAmount) {
                    sendNotification(context, category, limitAmount, totalSpent)
                }
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error fetching limits: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

fun sendNotification(context: Context, category: String, limitAmount: Double, expenseAmount: Double) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationId = (System.currentTimeMillis() % 10000).toInt()

    val notification = NotificationCompat.Builder(context, "limit_exceeded_channel")
        .setSmallIcon(R.drawable.logo2)
        .setContentTitle("Limit Exceeded")
        .setContentText("Your limit of $limitAmount for $category has been exceeded by ${expenseAmount - limitAmount}.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(notificationId, notification)
}
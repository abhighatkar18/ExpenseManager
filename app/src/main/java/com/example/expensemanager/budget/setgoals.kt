package com.example.expensemanager.budget
import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.expensemanager.ReminderWorker
import com.example.expensemanager.ui.theme.green
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun SetGoalsScreenWithProgress(userId: String, db: FirebaseFirestore) {
    var itemName by remember { mutableStateOf("") }
    var goalAmount by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var monthlySavings by remember { mutableStateOf("") }
    var suggestedMonths by remember { mutableStateOf(0) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Set Your Purchase Goal",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = green
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = itemName,
            onValueChange = { itemName = it },
            label = { Text("Enter Desired Item") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = goalAmount,
            onValueChange = { goalAmount = it },
            label = { Text("Enter Goal Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Monthly Savings Input
        TextField(
            value = monthlySavings,
            onValueChange = { monthlySavings = it },
            label = { Text("Enter Monthly Savings") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date Picker and Progress Indicator
        Text("Goal Progress: ${(progress * 100).toInt()}%")
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF3F51B5)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date Picker
        Text(
            text = "Select Target Date: ${targetDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}",
            style = MaterialTheme.typography.bodyLarge,
            color = green
        )
        Button(onClick = { showDatePicker = true }) {
            Text("Pick a Date")
        }
        if (showDatePicker) {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    targetDate = LocalDate.of(year, month + 1, day)
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Suggest how many months to complete the goal
        if (goalAmount.isNotEmpty() && monthlySavings.isNotEmpty()) {
            suggestedMonths = calculateMonthsToCompleteGoal(goalAmount.toDouble(), monthlySavings.toDouble())
            Text(
                text = "You need $suggestedMonths months to complete this goal.",
                style = MaterialTheme.typography.bodyLarge,
                color = green
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Goal Button
        Button(
            onClick = {
                saveGoalWithSuggestion(context, userId, db, itemName, goalAmount, targetDate, progress, monthlySavings)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Set Purchase Goal")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // View Goals Section
        ViewGoalsScreen(userId, db)
    }
}

fun calculateMonthsToCompleteGoal(goalAmount: Double, monthlySavings: Double): Int {
    return if (monthlySavings > 0) {
        kotlin.math.ceil(goalAmount / monthlySavings).toInt()
    } else {
        0
    }
}

fun saveGoalWithSuggestion(
    context: Context,
    userId: String,
    db: FirebaseFirestore,
    itemName: String,
    goalAmount: String,
    targetDate: LocalDate,
    progress: Float,
    monthlySavings: String
) {
    val goalData = hashMapOf(
        "itemName" to itemName,
        "goalAmount" to goalAmount.toDouble(),
        "currentSavings" to 0.0, // Start with no savings
        "progress" to progress,
        "monthlySavings" to monthlySavings.toDouble(),
        "targetDate" to targetDate.toString(),
        "createdAt" to Timestamp.now()
    )

    db.collection("users")
        .document(userId)
        .collection("goals")
        .add(goalData)
        .addOnSuccessListener {
            Toast.makeText(context, "Goal saved successfully!", Toast.LENGTH_LONG).show()

            // Schedule the monthly reminder to save
            scheduleMonthlyReminder(context, userId)
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error saving goal: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

fun scheduleMonthlyReminder(context: Context, userId: String) {
    // Schedule a periodic monthly reminder
    val monthlyWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(30, TimeUnit.DAYS).build()
    WorkManager.getInstance(context).enqueue(monthlyWorkRequest)
}

@Composable
fun ViewGoalsScreen(userId: String, db: FirebaseFirestore) {
    val context = LocalContext.current
    val goals = remember { mutableStateListOf<Goal>() } // List to hold the goals

    // Load goals from Firestore
    LaunchedEffect(userId) {
        db.collection("users")
            .document(userId)
            .collection("goals")
            .get()
            .addOnSuccessListener { result ->
                goals.clear()
                for (document in result) {
                    val goal = document.toObject(Goal::class.java).apply {
                        id = document.id // Store the document ID
                    }
                    goals.add(goal)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading goals: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
//            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Your Goals",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = green
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display each goal
        goals.forEach { goal ->
            GoalItem(goal = goal, onRemove = { removeGoal(context, userId, goal.id, db) })
        }
    }
}

@Composable
fun GoalItem(goal: Goal, onRemove: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.LightGray, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(text = "Item: ${goal.itemName}", fontSize = 18.sp)
        Text(text = "Goal Amount: ${goal.goalAmount}", fontSize = 18.sp)
        Text(text = "Current Savings: ${goal.currentSavings}", fontSize = 18.sp)
        Text(text = "Target Date: ${goal.targetDate}", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onRemove) {
            Text("Remove Goal")
        }
    }
}

fun removeGoal(context: Context, userId: String, goalId: String, db: FirebaseFirestore) {
    db.collection("users")
        .document(userId)
        .collection("goals")
        .document(goalId)
        .delete()
        .addOnSuccessListener {
            Toast.makeText(context, "Goal removed successfully!", Toast.LENGTH_LONG).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error removing goal: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

// Data class for Goal
data class Goal(
    var id: String = "", // This will hold the document ID
    val itemName: String = "",
    val goalAmount: Double = 0.0,
    val currentSavings: Double = 0.0,
    val targetDate: String = ""
)





//@Composable
//fun SetGoalsScreen() {
//    val context = LocalContext.current
//    var itemName by remember { mutableStateOf("") }
//    var goalAmount by remember { mutableStateOf("") }
//    var targetDate by remember { mutableStateOf(LocalDate.now()) }
//    var showDatePicker by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .background(Color(0xFFEFEFEF))
//    ) {
//        Text(
//            text = "Set Your Purchase Goal",
//            fontSize = 24.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFF3F51B5)
//        )
//        Spacer(modifier = Modifier.height(20.dp))
//
//        TextField(
//            value = itemName,
//            onValueChange = { itemName = it },
//            label = { Text("Enter Desired Item") },
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(Color.White, shape = RoundedCornerShape(8.dp)),
//            colors = TextFieldDefaults.colors(
//                focusedContainerColor = Color.White,
//                unfocusedContainerColor = Color.White,
//                focusedIndicatorColor = Color(0xFF3F51B5),
//                unfocusedIndicatorColor = Color.Transparent
//            )
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        TextField(
//            value = goalAmount,
//            onValueChange = { goalAmount = it },
//            label = { Text("Enter Goal Amount") },
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(Color.White, shape = RoundedCornerShape(8.dp)),
//            colors = TextFieldDefaults.colors(
//                focusedContainerColor = Color.White,
//                unfocusedContainerColor = Color.White,
//                focusedIndicatorColor = Color(0xFF3F51B5),
//                unfocusedIndicatorColor = Color.Transparent
//            )
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Target Date Picker
//        Text(
//            text = "Select Target Date: ${targetDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}",
//            style = MaterialTheme.typography.bodyLarge,
//            color = green
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Button(
//            onClick = { showDatePicker = true },
//            modifier = Modifier.fillMaxWidth(),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = green,
//                contentColor = Color.White
//            ),
//            shape = RoundedCornerShape(8.dp)
//        ) {
//            Text(text = "Pick a Date")
//        }
//
//        // Date Picker Dialog
//        if (showDatePicker) {
//            val calendar = Calendar.getInstance()
//            val day = calendar.get(Calendar.DAY_OF_MONTH)
//            val month = calendar.get(Calendar.MONTH)
//            val year = calendar.get(Calendar.YEAR)
//
//            DatePickerDialog(
//                LocalContext.current,
//                { _, selectedYear, selectedMonth, selectedDay ->
//                    targetDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
//                    showDatePicker = false
//                },
//                year, month, day
//            ).show()
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//
//
//        Button(
//            onClick = {
//                saveGoal(context, itemName, goalAmount, targetDate)
//
//            },
//            modifier = Modifier.fillMaxWidth(),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color(0xFF3F51B5),
//                contentColor = Color.White
//            ),
//            shape = RoundedCornerShape(8.dp)
//        ) {
//            Text(text = "Set Purchase Goal")
//        }
//    }
//}
//fun saveGoal(context: Context, itemName: String, goalAmount: String, targetDate: LocalDate) {
//
//    val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
//
//    val goalData = hashMapOf(
//        "itemName" to itemName,
//        "goalAmount" to goalAmount,
//        "targetDate" to targetDate.toString(),
//        "createdAt" to Timestamp.now()
//    )
//    val db = FirebaseFirestore.getInstance()
//
//    db.collection("users")
//        .document(userUid)
//        .collection("goals")
//        .add(goalData)
//        .addOnSuccessListener {
//
//            Toast.makeText(context, "Goal saved successfully!", Toast.LENGTH_LONG).show()
//        }
//        .addOnFailureListener { e ->
//            // Handle failure and show error message
//            Toast.makeText(context, "Error saving goal: ${e.message}", Toast.LENGTH_LONG).show()
//
//        }
//}


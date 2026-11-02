
package com.example.expensemanager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.expensemanager.budget.AIBasedBudgetScreen
import com.example.expensemanager.budget.MainScreen
import com.example.expensemanager.budget.SetGoalsScreenWithProgress
import com.example.expensemanager.budget.SetLimitsScreen
import com.example.expensemanager.ui.theme.green

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Preview(showBackground = true)
@Composable
fun BudgetScreenPreview() {
    BudgetScreenContent()
}

@Composable
fun BudgetScreenContent() {

    var selectedOption by remember {
        mutableStateOf("")
    }

    // Prevent Firebase access during Compose Preview
    val isPreview = LocalInspectionMode.current

    // Firebase Authentication
    val uid: String? = if (isPreview) {
        null
    } else {
        FirebaseAuth.getInstance().currentUser?.uid
    }

    // Firestore database
    val db: FirebaseFirestore? = if (isPreview) {
        null
    } else {
        FirebaseFirestore.getInstance()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Budget Management",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = green
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Set Limits
        OptionItem(
            optionText = "Set Limits",
            onClick = {
                selectedOption = "SetLimits"
            },
            icon = Icons.Default.Alarm
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Schedule Transactions
        OptionItem(
            optionText = "Schedule a Future Transaction",
            onClick = {
                selectedOption = "ScheduleTransaction"
            },
            icon = Icons.Default.Schedule
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Set Goals
        OptionItem(
            optionText = "Set Goals",
            onClick = {
                selectedOption = "SetGoals"
            },
            icon = Icons.Default.Star
        )

        Spacer(modifier = Modifier.height(16.dp))

        // AI Budget
        OptionItem(
            optionText = "AI Based Budget",
            onClick = {
                selectedOption = "AIBasedBudget"
            },
            icon = Icons.Default.Money
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Selected screen
        if (uid != null && db != null) {

            when (selectedOption) {

                "SetLimits" -> {
                    SetLimitsScreen()
                }

                "ScheduleTransaction" -> {
                    MainScreen()
                }

                "SetGoals" -> {
                    SetGoalsScreenWithProgress(
                        uid,
                        db
                    )
                }

                "AIBasedBudget" -> {
                    AIBasedBudgetScreen(
                        userId = uid,
                        db = db
                    ) {
                        selectedOption = ""
                    }
                }
            }

        } else if (!isPreview) {

            Text(
                text = "User not logged in",
                color = Color.Red,
                fontSize = 15.sp
            )
        }
    }
}

// Category Dropdown
@Composable
fun DropdownMenuCategories(
    selectedCategory: String,
    categories: List<String>,
    onCategorySelected: (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {

        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            label = {
                Text("Select Category")
            },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Clickable overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable {
                    expanded = true
                }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            categories.forEach { category ->

                DropdownMenuItem(
                    text = {
                        Text(category)
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Budget Option Card
@Composable
fun OptionItem(
    optionText: String,
    onClick: () -> Unit,
    icon: ImageVector
) {

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = green
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )

            Spacer(
                modifier = Modifier.width(16.dp)
            )

            Text(
                text = optionText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

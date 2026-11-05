package com.example.expensemanager

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.expensemanager.ui.theme.green
import android.widget.DatePicker
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import java.util.Calendar
@Composable
fun ExpenseIncomeForm(
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String, String, String?, Uri?) -> Unit
) {
    var selectedType by remember { mutableStateOf("Expense") }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }
    var receiptUri by remember { mutableStateOf<Uri?>(null) }
    var amountError by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        LocalContext.current,
        { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            date = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
        },
        year, month, day
    )

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> receiptUri = uri }
    )

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Add $selectedType", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                // Type toggal button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Type:")
                    Spacer(modifier = Modifier.width(8.dp))
                    SegmentedControl(
                        items = listOf("Expense", "Income"),
                        selectedItem = selectedType,
                        onItemSelected = { selectedType = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it; amountError = false },
                    label = { Text("Amount") },
                    isError = amountError,
                    supportingText = { if (amountError) Text("Enter an amount greater than 0 (max 2 decimals)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date
                OutlinedTextField(
                    value = date,
                    onValueChange = { },
                    label = { Text("Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    readOnly = true,  // Ensures the text field is not editable
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Blue,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.Blue,
                    ),
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedType == "Expense") {
                    Column {
                        Text(text = "Category", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(listOf(
                                "Housing" to R.drawable.house,
                                "Food" to R.drawable.food,
                                "Entertainment" to R.drawable.entertainment,
                                "Transportation" to R.drawable.vehicles,
                                "Investment" to R.drawable.investment,
                                "Healthcare" to R.drawable.healthcare,
                                "Bills" to R.drawable.bill,
                                "Subscription" to R.drawable.subscription,
                                "Others" to R.drawable.others
                            )) { categoryItem ->
                                CategoryItem(categoryItem.first, categoryItem.second, category) { category = it }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Receipt images
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (receiptUri == null) "Add Receipt" else "Change Receipt")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save and cAncel Buttonss
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val parsedAmount = parseAmount(amount)
                        amountError = parsedAmount == null
                        if (parsedAmount != null && name.isNotBlank() &&
                            (selectedType == "Income" || category != null) && date.isNotBlank()
                        ) {
                            onSave(selectedType, name, parsedAmount, description, date, category, receiptUri)
                            onDismiss()
                        } else {
                            Toast.makeText(context, "Please fill in the required fields", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(categoryName: String, iconRes: Int, selectedCategory: String?, onSelectCategory: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onSelectCategory(categoryName) }
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = categoryName,
            modifier = Modifier
                .height(40.dp)
                .width(40.dp)
        )
        Text(
            text = categoryName,
            color = if (selectedCategory == categoryName) green else Color.Gray
        )
    }
}

// A helper composable for the segmented control (Toggle between Expense and Income)
@Composable
fun SegmentedControl(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        items.forEach { item ->
            Button(
                onClick = { onItemSelected(item) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (item == selectedItem) green else Color.Gray
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Text(text = item, color = Color.White)
            }
        }
    }
}
private val AMOUNT_REGEX = Regex("""^\d{1,9}(\.\d{1,2})?$""")

// Positive number, max 2 decimals; otherwise null
internal fun parseAmount(input: String): Double? {
    val text = input.trim()
    if (!AMOUNT_REGEX.matches(text)) return null
    return text.toDouble().takeIf { it > 0.0 }
}
package com.example.expensemanager.ui.transactions

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val Emerald = Color(0xFF087F69)
private val DeepGreen = Color(0xFF064B3D)

@Suppress("SpellCheckingInspection")
private const val APP_BRAND = "Exman."

private data class ExpenseCategory(
    val name: String,
    val icon: ImageVector
)

private val expenseCategories = listOf(
    ExpenseCategory("Food", Icons.Default.Restaurant),
    ExpenseCategory("Shopping", Icons.Default.ShoppingBag),
    ExpenseCategory("Travel", Icons.Default.DirectionsCar),
    ExpenseCategory("Bills", Icons.Default.Receipt),
    ExpenseCategory("Health", Icons.Default.Favorite),
    ExpenseCategory("Education", Icons.Default.School),
    ExpenseCategory("Other", Icons.Default.MoreHoriz)
)

private val incomeCategories = listOf(
    ExpenseCategory("Salary", Icons.Default.Work),
    ExpenseCategory("Business", Icons.Default.Store),
    ExpenseCategory("Gift", Icons.Default.CardGiftcard),
    ExpenseCategory("Other", Icons.Default.MoreHoriz)
)

@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    vm: AddTransactionViewModel = viewModel()
) {
    var type by rememberSaveable { mutableStateOf("expense") }
    var amount by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    val categories = if (type == "expense") {
        expenseCategories
    } else {
        incomeCategories
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    enabled = !vm.saving
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Text(
                    "New transaction",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    APP_BRAND,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.width(8.dp))
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
            ) {
                Button(
                    onClick = {
                        vm.save(
                            amount = amount,
                            type = type,
                            category = category,
                            description = description,
                            onSuccess = onSaved
                        )
                    },
                    enabled = !vm.saving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .heightIn(min = 54.dp),
                    shape = RoundedCornerShape(17.dp)
                ) {
                    if (vm.saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.CheckCircleOutline,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Save transaction",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        // The width constraint is used below; some lint versions misreport this.
        //noinspection UnusedBoxWithConstraintsScope
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            val availableWidth = this@BoxWithConstraints.maxWidth
            val columns = when {
                availableWidth >= 750.dp -> 4
                availableWidth >= 390.dp -> 3
                else -> 2
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Income / Expense switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(18.dp)
                            )
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        TypeButton(
                            "Expense",
                            selected = type == "expense",
                            modifier = Modifier.weight(1f)
                        ) {
                            type = "expense"
                            category = ""
                            vm.clearError()
                        }

                        TypeButton(
                            "Income",
                            selected = type == "income",
                            modifier = Modifier.weight(1f)
                        ) {
                            type = "income"
                            category = ""
                            vm.clearError()
                        }
                    }

                    // Amount-first receipt design
                    AmountPanel(
                        amount = amount,
                        onAmountChange = {
                            val valid = it.matches(
                                Regex("""^\d{0,9}(\.\d{0,2})?$""")
                            )

                            if (valid) {
                                amount = it
                                vm.clearError()
                            }
                        },
                        type = type
                    )

                    Text(
                        "Choose a category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    categories.chunked(columns).forEach { rowItems ->
                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowItems.forEach { item ->
                                CategoryButton(
                                    item = item,
                                    selected = category == item.name,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    category = item.name
                                    vm.clearError()
                                }
                            }

                            repeat(columns - rowItems.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            if (it.length <= 80) description = it
                        },
                        label = { Text("Description (optional)") },
                        placeholder = {
                            Text("e.g. Coffee with friends")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.EditNote,
                                contentDescription = null
                            )
                        },
                        supportingText = {
                            Text("Keep it short and easy to recognize")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Small receipt-style detail
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Today,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(Modifier.width(12.dp))

                            Column {
                                Text(
                                    "Transaction date",
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    "Today",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    vm.error?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun TypeButton(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                title,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AmountPanel(
    amount: String,
    onAmountChange: (String) -> Unit,
    type: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = DeepGreen
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(Emerald, DeepGreen)
                    )
                )
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "ENTER AMOUNT",
                color = Color(0xFFB8EFDA),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                placeholder = {
                    Text(
                        "0.00",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 27.sp
                    )
                },
                prefix = {
                    Text(
                        "₹ ",
                        color = Color.White,
                        fontSize = 24.sp
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor =
                        Color.White.copy(alpha = 0.45f),
                    cursorColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                if (type == "expense") {
                    "Track what you spend"
                } else {
                    "Record money coming in"
                },
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CategoryButton(
    item: ExpenseCategory,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .heightIn(min = 94.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(19.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = if (selected) null else BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                item.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

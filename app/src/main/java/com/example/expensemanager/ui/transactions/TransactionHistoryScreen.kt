
package com.example.expensemanager.ui.transactions

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanager.data.dashboard.DashboardRepository
import com.example.expensemanager.data.dashboard.DashboardState
import com.example.expensemanager.data.dashboard.ExmanTransaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

private val Emerald = Color(0xFF087F69)
private val DeepGreen = Color(0xFF064B3D)
private val ExpenseRed = Color(0xFFE66B73)

private enum class TransactionFilter(val label: String) {
    ALL("All"),
    EXPENSE("Expenses"),
    INCOME("Income")
}

private fun formatMoney(amount: Double): String =
    NumberFormat.getCurrencyInstance(
        Locale.forLanguageTag("en-IN")
    ).apply {
        maximumFractionDigits = 2
    }.format(amount)
@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun TransactionHistoryScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit
) {
    val repository = remember { DashboardRepository() }
    var state by remember { mutableStateOf(DashboardState()) }

    DisposableEffect(repository) {
        repository.observe { state = it }
        onDispose { repository.stop() }
    }

    var search by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable {
        mutableStateOf(TransactionFilter.ALL)
    }

    val filtered = remember(
        state.transactions,
        search,
        selectedFilter
    ) {
        state.transactions.filter { transaction ->
            val matchesSearch =
                transaction.title.contains(search.trim(), true) ||
                        transaction.category.contains(search.trim(), true)

            val matchesType = when (selectedFilter) {
                TransactionFilter.ALL -> true
                TransactionFilter.EXPENSE -> !transaction.income
                TransactionFilter.INCOME -> transaction.income
            }

            matchesSearch && matchesType
        }
    }

    val grouped = remember(filtered) {
        val formatter = SimpleDateFormat(
            "EEEE, dd MMMM yyyy",
            Locale.getDefault()
        )

        filtered.groupBy { item ->
            item.date?.let(formatter::format) ?: "Date unavailable"
        }
    }

    val totalIncome = remember(filtered) {
        filtered.filter { it.income }
            .sumOf { abs(it.amount) }
    }

    val totalExpense = remember(filtered) {
        filtered.filter { !it.income }
            .sumOf { abs(it.amount) }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val availableWidth = this.maxWidth
        val wide = availableWidth >= 600.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = if (wide) 32.dp else 18.dp,
                vertical = 16.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    Modifier
                        .widthIn(max = 860.dp)
                        .fillMaxWidth()

                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }

                            Spacer(Modifier.width(4.dp))

                            Column(Modifier.weight(1f)) {
                                Text(
                                    "ACTIVITY STUDIO",
                                    fontSize = 10.sp,
                                    letterSpacing = 1.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    "Your transactions",
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            FilledIconButton(onClick = onAdd) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add transaction"
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        SummaryPanel(
                            count = filtered.size,
                            income = totalIncome,
                            expense = totalExpense
                        )
                    }
                }
            }

            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .widthIn(max = 860.dp)
                ) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = {
                            Text("Search transactions or categories")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            if (search.isNotEmpty()) {
                                IconButton(onClick = { search = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 860.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionFilter.entries.forEach { option ->
                        FilterChip(
                            selected = selectedFilter == option,
                            onClick = { selectedFilter = option },
                            label = {
                                Text(
                                    option.label,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            when {
                state.loadingTransactions -> {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 860.dp)
                        )
                    }
                }

                state.transactionError != null -> {
                    item {
                        MessagePanel(
                            title = "Couldn't load transactions",
                            description = state.transactionError.orEmpty()
                        )
                    }
                }

                filtered.isEmpty() -> {
                    item {
                        MessagePanel(
                            title = if (search.isNotBlank()) {
                                "Nothing matches your search"
                            } else {
                                "No transactions found"
                            },
                            description =
                                "Try a different filter or add a transaction."
                        )
                    }
                }

                else -> {
                    grouped.forEach { (date, transactions) ->
                        item(key = "date_$date") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 860.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    date,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    "${transactions.size} records",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        items(
                            items = transactions,
                            key = { it.id }
                        ) { transaction ->
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 860.dp)
                            ) {
                                TransactionItem(transaction)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SummaryPanel(
    count: Int,
    income: Double,
    expense: Double
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = DeepGreen,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(Emerald, DeepGreen)
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                "FILTERED ACTIVITY",
                color = Color(0xFFB9F2DE),
                fontSize = 10.sp,
                letterSpacing = 1.5.sp
            )

            Spacer(Modifier.height(9.dp))

            Text(
                "$count transactions",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(18.dp))

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.2f)
            )

            Spacer(Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryValue(
                    "MONEY IN",
                    income,
                    Modifier.weight(1f)
                )

                SummaryValue(
                    "MONEY OUT",
                    expense,
                    Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryValue(
    label: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            label,
            color = Color(0xFFB9F2DE),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(5.dp))

        Text(
            formatMoney(amount),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TransactionItem(item: ExmanTransaction) {
    val categoryIcon = when {
        item.income -> Icons.Default.SouthWest
        item.category.contains("food", true) ->
            Icons.Default.Restaurant
        item.category.contains("transport", true) ->
            Icons.Default.DirectionsCar
        item.category.contains("travel", true) ->
            Icons.Default.Flight
        item.category.contains("shopping", true) ->
            Icons.Default.ShoppingBag
        item.category.contains("health", true) ->
            Icons.Default.Favorite
        else -> Icons.AutoMirrored.Filled.ReceiptLong
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(15.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    categoryIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    item.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = (if (item.income) "+" else "−") +
                        formatMoney(abs(item.amount)),
                color = if (item.income) Emerald else ExpenseRed,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun MessagePanel(
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 860.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                title,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

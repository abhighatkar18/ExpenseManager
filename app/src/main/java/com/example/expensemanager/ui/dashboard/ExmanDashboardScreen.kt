package com.example.expensemanager.ui.dashboard
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanager.data.dashboard.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

private val Green = Color(0xFF087F69)
private val DarkGreen = Color(0xFF064B3D)
private val Mint = Color(0xFF8EEAC5)
private val Coral = Color(0xFFE9797E)

private fun money(value: Double): String =
    NumberFormat.getCurrencyInstance(
        Locale.forLanguageTag("en-IN")
    ).apply {
        maximumFractionDigits = 2
    }.format(value)

@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun ExmanDashboardScreen(
    onAddTransaction: () -> Unit,
    onBudget: () -> Unit,
    onAssistant: () -> Unit,
    onActivity: () -> Unit
) {
    val repository = remember { DashboardRepository() }
    var state by remember { mutableStateOf(DashboardState()) }

    DisposableEffect(repository) {
        repository.observe { state = it }
        onDispose { repository.stop() }
    }

    var balanceVisible by rememberSaveable {
        mutableStateOf(true)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val availableWidth = this.maxWidth
        val wide = availableWidth >= 600.dp
        val horizontalPadding = if (wide) 28.dp else 18.dp
        val contentMax = 1100.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = 20.dp,
                bottom = 28.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Box(Modifier.widthIn(max = contentMax)) {
                    DashboardHeader(state.name)
                }
            }

            item {
                Box(Modifier.widthIn(max = contentMax)) {
                    BalanceHero(
                        state = state,
                        visible = balanceVisible,
                        onVisibilityChange = {
                            balanceVisible = !balanceVisible
                        },
                        onAdd = onAddTransaction
                    )
                }
            }

            if (state.balanceError != null) {
                item {
                    Text(
                        state.balanceError.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (wide) {
                item {
                    Row(
                        modifier = Modifier.widthIn(max = contentMax),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MoneyPulse(state)
                            SpendingSpotlight(state)
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ShortcutSection(
                                onBudget = onBudget,
                                onAssistant = onAssistant
                            )
                            ActivityHeading(onActivity)
                            ActivityPreview(state)
                        }
                    }
                }
            } else {
                item {
                    Box(Modifier.widthIn(max = contentMax)) {
                        MoneyPulse(state)
                    }
                }

                item {
                    Box(Modifier.widthIn(max = contentMax)) {
                        SpendingSpotlight(state)
                    }
                }

                item {
                    Box(Modifier.widthIn(max = contentMax)) {
                        ShortcutSection(
                            onBudget = onBudget,
                            onAssistant = onAssistant
                        )
                    }
                }

                item {
                    Box(Modifier.widthIn(max = contentMax)) {
                        ActivityHeading(onActivity)
                    }
                }

                if (state.loadingTransactions) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .widthIn(max = contentMax)
                                .fillMaxWidth()
                        )
                    }
                } else if (state.transactions.isEmpty()) {
                    item {
                        Box(Modifier.widthIn(max = contentMax)) {
                            EmptyActivity()
                        }
                    }
                } else {
                    items(
                        state.transactions.take(5),
                        key = { it.id }
                    ) { transaction ->
                        Box(Modifier.widthIn(max = contentMax)) {
                            ActivityRow(transaction)
                        }
                    }
                }

                if (state.transactionError != null) {
                    item {
                        Text(
                            state.transactionError.orEmpty(),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "EXMAN / YOUR MONEY SPACE",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.7.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Hello, ${name.trim().substringBefore(" ")}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                "A clearer view of your finances.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                Icons.Default.AccountBalanceWallet,
                contentDescription = "Exman",
                modifier = Modifier.padding(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BalanceHero(
    state: DashboardState,
    visible: Boolean,
    onVisibilityChange: () -> Unit,
    onAdd: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF118A6F),
                        DarkGreen,
                        Color(0xFF052E2D)
                    )
                )
            )
    ) {
        Canvas(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(170.dp)
        ) {
            drawCircle(
                color = Color.White.copy(alpha = 0.07f),
                radius = size.minDimension * 0.47f,
                center = Offset(size.width, 0f),
                style = Stroke(width = 32.dp.toPx())
            )
            drawCircle(
                color = Mint.copy(alpha = 0.11f),
                radius = size.minDimension * 0.26f,
                center = Offset(size.width, 0f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "YOUR TOTAL BALANCE",
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Mint,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onVisibilityChange
                ) {
                    Icon(
                        if (visible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                        contentDescription =
                            if (visible) "Hide balance"
                            else "Show balance",
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (state.loadingBalance) {
                CircularProgressIndicator(
                    modifier = Modifier.size(30.dp),
                    color = Mint,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    if (visible) money(state.balance) else "••••••",
                    fontSize = 32.sp,
                    lineHeight = 39.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    modifier = Modifier.animateContentSize()
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "All-time recorded balance",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )

            Spacer(Modifier.height(24.dp))

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.18f)
            )

            Spacer(Modifier.height(18.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeroAmount(
                    title = "MONEY IN",
                    amount = state.income,
                    icon = Icons.Default.SouthWest,
                    visible = visible,
                    modifier = Modifier.weight(1f)
                )

                HeroAmount(
                    title = "MONEY OUT",
                    amount = state.expense,
                    icon = Icons.Default.NorthEast,
                    visible = visible,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(22.dp))

            Button(
                onClick = onAdd,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = DarkGreen
                )
            ) {
                Icon(
                    Icons.Default.AddCircleOutline,
                    contentDescription = null
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    "Manage transactions",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HeroAmount(
    title: String,
    amount: Double,
    icon: ImageVector,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = Mint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                title,
                fontSize = 10.sp,
                color = Mint,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(7.dp))

        Text(
            if (visible) money(amount) else "••••",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 22.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MoneyPulse(state: DashboardState) {
    val ratio = if (state.income > 0) {
        state.expense / state.income
    } else {
        0.0
    }

    val progress by animateFloatAsState(
        targetValue = ratio.toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 650),
        label = "Spending pulse"
    )

    val percentage = ratio * 100

    val explanation = when {
        state.loadingBalance -> "Loading your financial summary"
        state.income <= 0 -> "Add income to see your spending ratio"
        ratio > 1 -> "Expenses exceed your recorded income"
        ratio >= 0.8 -> "Most of your recorded income is spent"
        else -> "Your expenses are below recorded income"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                "MONEY PULSE",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Box(
                    modifier = Modifier.size(108.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = 11.dp.toPx()
                        val padding = stroke / 2
                        val arcSize = Size(
                            size.width - stroke,
                            size.height - stroke
                        )

                        drawArc(
                            color = Color.Gray.copy(alpha = 0.16f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(padding, padding),
                            size = arcSize,
                            style = Stroke(
                                width = stroke,
                                cap = StrokeCap.Round
                            )
                        )

                        drawArc(
                            color = if (ratio >= 0.8) Coral else Green,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = Offset(padding, padding),
                            size = arcSize,
                            style = Stroke(
                                width = stroke,
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (state.income > 0) {
                                "${percentage.toInt()}%"
                            } else {
                                "—"
                            },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "spent",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        "Spending rhythm",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(7.dp))

                    Text(
                        explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SpendingSpotlight(state: DashboardState) {
    val topCategory = remember(state.transactions) {
        state.transactions
            .filter { !it.income && it.amount != 0.0 }
            .groupBy { it.category }
            .mapValues { (_, records) ->
                records.sumOf { abs(it.amount) }
            }
            .maxByOrNull { it.value }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.TipsAndUpdates,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    "SPENDING SPOTLIGHT",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    topCategory?.key ?: "Your insights start here",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    if (topCategory == null) {
                        "Record expenses to discover your top category."
                    } else {
                        "${money(topCategory.value)} spent in this category"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ShortcutSection(
    onBudget: () -> Unit,
    onAssistant: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Explore your money",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Shortcut(
                title = "Budgets",
                subtitle = "Stay in control",
                icon = Icons.Default.DonutLarge,
                onClick = onBudget,
                modifier = Modifier.weight(1f)
            )

            Shortcut(
                title = "Exman AI",
                subtitle = "Smart insights",
                icon = Icons.Default.AutoAwesome,
                onClick = onAssistant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Shortcut(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )

            Text(
                title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ActivityHeading(onActivity: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Latest activity",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )

        TextButton(onClick = onActivity) {
            Text("View all")
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ActivityPreview(state: DashboardState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when {
            state.loadingTransactions -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            state.transactions.isEmpty() -> EmptyActivity()

            else -> {
                state.transactions.take(5).forEach {
                    ActivityRow(it)
                }
            }
        }

        state.transactionError?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun EmptyActivity() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "Your money story starts here",
                fontWeight = FontWeight.SemiBold
            )

            Text(
                "Your transactions will appear here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActivityRow(item: ExmanTransaction) {
    val icon = when {
        item.income -> Icons.Default.SouthWest
        item.category.contains("food", true) ->
            Icons.Default.Restaurant
        item.category.contains("travel", true) ->
            Icons.Default.DirectionsCar
        item.category.contains("shopping", true) ->
            Icons.Default.ShoppingBag
        else -> Icons.AutoMirrored.Filled.ReceiptLong
    }

    val date = item.date?.let {
        SimpleDateFormat(
            "dd MMM",
            Locale.getDefault()
        ).format(it)
    } ?: "Transaction"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    "${item.category} · $date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(6.dp))

            Text(
                (if (item.income) "+" else "−") +
                        money(abs(item.amount)),
                color = if (item.income) Green else Coral,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
        }
    }
}

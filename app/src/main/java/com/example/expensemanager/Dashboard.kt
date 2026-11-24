
package com.example.expensemanager

import ProfileScreenContent
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.expensemanager.ui.dashboard.ExmanDashboardScreen
import com.example.expensemanager.ui.theme.ExmanTheme
import com.example.expensemanager.ui.transactions.AddTransactionScreen
import com.example.expensemanager.ui.transactions.TransactionHistoryScreen
import com.google.firebase.auth.FirebaseAuth

class Dashboard : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()

        setContent {
            ExmanTheme {
                ExmanApp(
                    onLogout = { logout() }
                )
            }
        }
    }

    private fun requestNotificationPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()

        getSharedPreferences(
            "Credentials",
            MODE_PRIVATE
        ).edit {
            clear()
        }

        startActivity(
            Intent(this, loginpage::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )

        finish()
    }
}

// -------------------------------------
// Main navigation destinations
// -------------------------------------

private object Routes {
    const val HOME = "home"
    const val BUDGET = "budget"
    const val ASSISTANT = "assistant"
    const val PROFILE = "profile"
    const val HISTORY = "history"
    const val ADD_TRANSACTION = "addTransaction"
    const val MANAGE = "manage"
}

private data class MainTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val tabs = listOf(
    MainTab(
        Routes.HOME,
        "Home",
        Icons.Default.Home
    ),
    MainTab(
        Routes.BUDGET,
        "Budget",
        Icons.Default.DonutLarge
    ),
    MainTab(
        Routes.ASSISTANT,
        "Exman AI",
        Icons.Default.AutoAwesome
    ),
    MainTab(
        Routes.PROFILE,
        "Profile",
        Icons.Default.Person
    )
)

// -------------------------------------
// Navigation helpers
// -------------------------------------

private fun openTab(
    navController: NavHostController,
    route: String
) {
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) {
            saveState = true
        }

        launchSingleTop = true
        restoreState = true
    }
}

private fun openScreen(
    navController: NavHostController,
    route: String
) {
    navController.navigate(route) {
        launchSingleTop = true
    }
}

// -------------------------------------
// Main application
// -------------------------------------

@Suppress("UnusedBoxWithConstraintsScope")
@Composable
private fun ExmanApp(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val isMainScreen = tabs.any {
        it.route == currentRoute
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val tablet = this.maxWidth >= 600.dp

        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            // Side navigation on tablets
            if (tablet && isMainScreen) {
                ExmanNavigationRail(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        openTab(navController, route)
                    }
                )
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor =
                    MaterialTheme.colorScheme.background,

                // Bottom navigation on phones
                bottomBar = {
                    if (!tablet && isMainScreen) {
                        ExmanBottomNavigation(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                openTab(navController, route)
                            }
                        )
                    }
                }
            ) { padding ->

                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {

                    // -----------------------------
                    // Home Dashboard
                    // -----------------------------

                    composable(Routes.HOME) {
                        ExmanDashboardScreen(
                            onAddTransaction = {
                                openScreen(
                                    navController,
                                    Routes.ADD_TRANSACTION
                                )
                            },

                            onBudget = {
                                openTab(
                                    navController,
                                    Routes.BUDGET
                                )
                            },

                            onAssistant = {
                                openTab(
                                    navController,
                                    Routes.ASSISTANT
                                )
                            },

                            onActivity = {
                                openScreen(
                                    navController,
                                    Routes.HISTORY
                                )
                            }
                        )
                    }

                    // -----------------------------
                    // Add Income / Expense
                    // -----------------------------

                    composable(Routes.ADD_TRANSACTION) {
                        AddTransactionScreen(
                            onBack = {
                                navController.popBackStack()
                            },

                            onSaved = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // -----------------------------
                    // Transaction History
                    // -----------------------------

                    composable(Routes.HISTORY) {
                        TransactionHistoryScreen(
                            onBack = {
                                navController.popBackStack()
                            },

                            onAdd = {
                                openScreen(
                                    navController,
                                    Routes.ADD_TRANSACTION
                                )
                            }
                        )
                    }

                    // -----------------------------
                    // Legacy Transaction Management
                    // -----------------------------

                    composable(Routes.MANAGE) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 6.dp
                                    ),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        navController.popBackStack()
                                    }
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }

                                Text(
                                    "Transactions",
                                    style = MaterialTheme
                                        .typography.titleLarge
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                HomeScreenContent()
                            }
                        }
                    }

                    // -----------------------------
                    // Budget
                    // -----------------------------

                    composable(Routes.BUDGET) {
                        BudgetScreenContent()
                    }

                    // -----------------------------
                    // Exman AI
                    // -----------------------------

                    composable(Routes.ASSISTANT) {
                        ChatbotScreenContent()
                    }

                    // -----------------------------
                    // Profile
                    // -----------------------------

                    composable(Routes.PROFILE) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 4.dp
                                    ),
                                horizontalArrangement =
                                    Arrangement.End
                            ) {
                                TextButton(
                                    onClick = onLogout
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = null
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    Text("Sign out")
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                ProfileScreenContent()
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------
// Bottom navigation for mobile devices
// -------------------------------------

@Composable
private fun ExmanBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,

                onClick = {
                    if (currentRoute != tab.route) {
                        onNavigate(tab.route)
                    }
                },

                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label
                    )
                },

                label = {
                    Text(tab.label)
                },

                alwaysShowLabel = false,

                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor =
                        MaterialTheme.colorScheme.primary,
                    selectedTextColor =
                        MaterialTheme.colorScheme.primary,
                    indicatorColor =
                        MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

// -------------------------------------
// Navigation rail for larger devices
// -------------------------------------

@Composable
private fun ExmanNavigationRail(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Spacer(Modifier.height(20.dp))

        Icon(
            Icons.Default.AccountBalanceWallet,
            contentDescription = "Exman",
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(24.dp))

        tabs.forEach { tab ->
            NavigationRailItem(
                selected = currentRoute == tab.route,

                onClick = {
                    if (currentRoute != tab.route) {
                        onNavigate(tab.route)
                    }
                },

                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label
                    )
                },

                label = {
                    Text(tab.label)
                },

                alwaysShowLabel = true
            )
        }
    }
}

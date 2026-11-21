package com.example.expensemanager
import com.example.expensemanager.ui.transactions.TransactionHistoryScreen
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.expensemanager.ui.dashboard.ExmanDashboardScreen
import com.example.expensemanager.ui.theme.ExmanTheme
import com.google.firebase.auth.FirebaseAuth

class Dashboard : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        setContent {
            ExmanTheme {
                ExmanApp(
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()

                        getSharedPreferences(
                            "Credentials",
                            MODE_PRIVATE
                        ).edit { clear() }

                        startActivity(
                            Intent(
                                this,
                                loginpage::class.java
                            ).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        )
                        finish()
                    }
                )
            }
        }
    }
}

private data class MainTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val tabs = listOf(
    MainTab("home", "Home", Icons.Default.Home),
    MainTab("budget", "Budget", Icons.Default.DonutLarge),
    MainTab("assistant", "Exman AI", Icons.Default.AutoAwesome),
    MainTab("profile", "Profile", Icons.Default.Person)
)

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

@Suppress("UnusedBoxWithConstraintsScope")
@Composable
private fun ExmanApp(onLogout: () -> Unit) {
    val navController = rememberNavController()

    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    val onMainTab = tabs.any { it.route == route }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val availableWidth = this.maxWidth
        val tablet = availableWidth >= 600.dp

        Row(Modifier.fillMaxSize()) {
            if (tablet && onMainTab) {
                NavigationRail(
                    containerColor =
                        MaterialTheme.colorScheme.surface
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
                            selected = route == tab.route,
                            onClick = {
                                if (route != tab.route) {
                                    openTab(navController, tab.route)
                                }
                            },
                            icon = {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor =
                    MaterialTheme.colorScheme.background,

                bottomBar = {
                    if (!tablet && onMainTab) {
                        NavigationBar(
                            containerColor =
                                MaterialTheme.colorScheme.surface
                        ) {
                            tabs.forEach { tab ->
                                NavigationBarItem(
                                    selected = route == tab.route,
                                    onClick = {
                                        if (route != tab.route) {
                                            openTab(
                                                navController,
                                                tab.route
                                            )
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            tab.icon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(tab.label)
                                    },
                                    alwaysShowLabel = false
                                )
                            }
                        }
                    }
                }
            ) { padding ->

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    composable("history") {
                        TransactionHistoryScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onAdd = {
                                navController.navigate("manage") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable("home") {
                        ExmanDashboardScreen(
                            onAddTransaction = {
                                navController.navigate("manage") {
                                    launchSingleTop = true
                                }
                            },
                            onBudget = {
                                openTab(navController, "budget")
                            },
                            onAssistant = {
                                openTab(navController, "assistant")
                            },
                            onActivity = {
                                navController.navigate("history") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // Existing transaction management screen
                    composable("manage") {
                        Column(Modifier.fillMaxSize()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 6.dp
                                )
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

                    composable("budget") {
                        BudgetScreenContent()
                    }

                    composable("assistant") {
                        ChatbotScreenContent()
                    }

                    composable("profile") {
                        Column(Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement =
                                    Arrangement.End
                            ) {
                                TextButton(onClick = onLogout) {
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

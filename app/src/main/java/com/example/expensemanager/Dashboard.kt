package com.example.expensemanager

import ProfileScreenContent
import com.example.expensemanager.ui.theme.ExpenseManagerTheme
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

class Dashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseManagerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    HomeScreen()
                }
            }

        }
    }
}
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ExpenseManagerTheme {
        HomeScreen()
    }
}

@Composable
fun HomeScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavigationHost(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Budget,
        NavigationItem.Chatbot,
        NavigationItem.Profile
    )
    NavigationBar(
        containerColor = Color(0xFF009688),
        contentColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Box(modifier =Modifier.size(24.dp) ){
                        Icon(painterResource(id = item.icon),
                            contentDescription = stringResource(id = item.title)) }},
                label = { Text(text = stringResource(id = item.title)) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = NavigationItem.Home.route, modifier = modifier) {
        composable(NavigationItem.Home.route) {
            HomeScreenContent()
        }
        composable(NavigationItem.Chatbot.route) {
            ChatbotScreenContent()
        }

        composable(NavigationItem.Budget.route) {
            BudgetScreenContent()
        }
        composable(NavigationItem.Profile.route) {
            ProfileScreenContent()
        }

    }
}

// Define your NavigationItem sealed class with routes and icons
sealed class NavigationItem(var route: String, var icon: Int, var title: Int) {
    object Home : NavigationItem("home", R.drawable.ic_home_black_24dp, R.string.title_home)
    object Chatbot : NavigationItem("chatbot", R.drawable.ai, R.string.title_chatbot)
    object Budget : NavigationItem("budget", R.drawable.budget, R.string.title_budget)
    object Profile : NavigationItem("profile", R.drawable.profile, R.string.title_profile)

}

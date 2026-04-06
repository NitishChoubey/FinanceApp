package com.ebf.financeapp.ui.navigation



import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ebf.financeapp.ui.goals.GoalsScreen
import com.ebf.financeapp.ui.home.HomeScreen
import com.ebf.financeapp.ui.insights.InsightsScreen
import com.ebf.financeapp.ui.settings.SettingsScreen
import com.ebf.financeapp.ui.transactions.AddEditTransactionScreen
import com.ebf.financeapp.ui.transactions.TransactionListScreen

// ─── Route constants ─────────────────────────────────────────────────────────

object Routes {
    const val HOME              = "home"
    const val TRANSACTIONS      = "transactions"
    const val ADD_TRANSACTION   = "add_transaction"
    const val EDIT_TRANSACTION  = "edit_transaction/{transactionId}"
    const val GOALS             = "goals"
    const val INSIGHTS          = "insights"

    const val SETTINGS = "settings"

    fun editTransaction(id: Int) = "edit_transaction/$id"
}

// ─── Bottom nav items ─────────────────────────────────────────────────────────

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val bottomNavItems = listOf(
    BottomNavItem("Home",         Routes.HOME,         Icons.Filled.Home),
    BottomNavItem("Transactions", Routes.TRANSACTIONS, Icons.Filled.Receipt),
    BottomNavItem("Goals",        Routes.GOALS,        Icons.Filled.TrackChanges),
    BottomNavItem("Insights",     Routes.INSIGHTS,     Icons.Filled.BarChart),
)

// ─── Main navigation host ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    onDarkModeChange: (Boolean) -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide bottom bar on add/edit screens
    val showBottomBar = currentDestination?.route !in listOf(
        Routes.ADD_TRANSACTION,
        Routes.EDIT_TRANSACTION
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finance") },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Filled.AccountCircle, "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        } ,

                bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController    = navController,
            startDestination = Routes.HOME,
            modifier         = Modifier.padding(innerPadding),
            enterTransition  = {
                slideInHorizontally(animationSpec = tween(300)) { it / 4 } +
                        fadeIn(animationSpec = tween(300))
            },
            exitTransition   = {
                slideOutHorizontally(animationSpec = tween(300)) { -it / 4 } +
                        fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(animationSpec = tween(300)) { -it / 4 } +
                        fadeIn(animationSpec = tween(300))
            },
            popExitTransition  = {
                slideOutHorizontally(animationSpec = tween(300)) { it / 4 } +
                        fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(Routes.HOME) {
                HomeScreen(navController)
            }
            composable(Routes.TRANSACTIONS) {
                TransactionListScreen(navController)
            }
            composable(Routes.ADD_TRANSACTION) {
                AddEditTransactionScreen(navController)
            }
            composable(
                route     = Routes.EDIT_TRANSACTION,
                arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("transactionId") ?: return@composable
                AddEditTransactionScreen(navController, transactionId = id)
            }
            composable(Routes.GOALS) {
                GoalsScreen(navController)
            }
            composable(Routes.INSIGHTS) {
                InsightsScreen(navController)
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    navController    = navController,
                    onDarkModeChange = onDarkModeChange
                )
            }
        }
    }
}

// Temporary placeholder — replaced screen by screen in Step 4
@Composable
private fun PlaceholderScreen(name: String) {
    androidx.compose.foundation.layout.Box(
        contentAlignment = androidx.compose.ui.Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text = name, style = MaterialTheme.typography.titleLarge)
    }
}
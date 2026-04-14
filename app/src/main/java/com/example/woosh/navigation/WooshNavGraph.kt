package com.example.woosh.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.woosh.ui.screens.*
import com.example.woosh.ui.theme.ElegantDark
import com.example.woosh.ui.theme.PrimaryGold
import com.example.woosh.ui.theme.OffWhite
import com.example.woosh.ui.theme.SurfaceWhite

@Composable
fun WooshApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf("home", "ticket", "profile")) {
                BottomNavBar(navController)
            }
        }
    ) { padding ->
        Surface(color = OffWhite, modifier = Modifier.fillMaxSize().padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = "splash",
                enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(400)) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(400)) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(400)) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(400)) + fadeOut() }
            ) {
                composable("splash") { SplashScreen(navController) }
                composable("login") { LoginScreen(navController) }
                composable("register") { RegisterScreen(navController) }
                composable("home") { HomeScreen(navController) }
                composable(
                    route = "train_list/{dest}/{passengers}",
                    arguments = listOf(
                        navArgument("dest") { type = NavType.StringType },
                        navArgument("passengers") { type = NavType.IntType }
                    )
                ) { backStackEntry -> 
                    val dest = backStackEntry.arguments?.getString("dest") ?: "Halim"
                    val passengers = backStackEntry.arguments?.getInt("passengers") ?: 1
                    TrainListScreen(navController, dest, passengers) 
                }
                composable(
                    route = "seat_selection/{passengers}/{price}/{trainId}/{trainName}",
                    arguments = listOf(
                        navArgument("passengers") { type = NavType.IntType },
                        navArgument("price") { type = NavType.StringType },
                        navArgument("trainId") { type = NavType.StringType },
                        navArgument("trainName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val passengers = backStackEntry.arguments?.getInt("passengers") ?: 1
                    val price = backStackEntry.arguments?.getString("price") ?: "Rp 0"
                    val trainId = backStackEntry.arguments?.getString("trainId") ?: "WOOSH502"
                    val trainName = backStackEntry.arguments?.getString("trainName") ?: "WOOSH 502"
                    SeatSelectionScreen(navController, passengers, price, trainId, trainName)
                }
                composable(
                    route = "checkout/{seats}/{price}/{passengers}/{trainId}/{trainName}",
                    arguments = listOf(
                        navArgument("seats") { type = NavType.StringType },
                        navArgument("price") { type = NavType.StringType },
                        navArgument("passengers") { type = NavType.IntType },
                        navArgument("trainId") { type = NavType.StringType },
                        navArgument("trainName") { type = NavType.StringType }
                    )
                ) { backStackEntry -> 
                    val seats = backStackEntry.arguments?.getString("seats") ?: ""
                    val price = backStackEntry.arguments?.getString("price") ?: "Rp 0"
                    val passengers = backStackEntry.arguments?.getInt("passengers") ?: 1
                    val trainId = backStackEntry.arguments?.getString("trainId") ?: "WOOSH502"
                    val trainName = backStackEntry.arguments?.getString("trainName") ?: "WOOSH 502"
                    CheckoutScreen(navController, seats, price, passengers, trainId, trainName)
                }
                composable(
                    route = "payment/{total}/{seats}/{trainId}/{trainName}",
                    arguments = listOf(
                        navArgument("total") { type = NavType.StringType },
                        navArgument("seats") { type = NavType.StringType },
                        navArgument("trainId") { type = NavType.StringType },
                        navArgument("trainName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val total = backStackEntry.arguments?.getString("total") ?: "Rp 0"
                    val seats = backStackEntry.arguments?.getString("seats") ?: ""
                    val trainId = backStackEntry.arguments?.getString("trainId") ?: "WOOSH502"
                    val trainName = backStackEntry.arguments?.getString("trainName") ?: "WOOSH 502"
                    PaymentScreen(navController, total, seats, trainId, trainName)
                }
                composable("loyalty") { LoyaltyScreen(navController) }
                composable(
                    route = "ticket/{seats}",
                    arguments = listOf(navArgument("seats") { type = NavType.StringType })
                ) { backStackEntry ->
                    val seats = backStackEntry.arguments?.getString("seats") ?: ""
                    TicketScreen(navController, seats)
                }
                composable("ticket") { TicketScreen(navController) } // Default for bottom bar
                composable("profile") { ProfileScreen(navController) }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar(containerColor = SurfaceWhite) {
        NavigationBarItem(selected = currentRoute == "home", onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true }; launchSingleTop = true } }, icon = { Icon(if(currentRoute == "home") Icons.Default.Home else Icons.Outlined.Home, null) }, label = { Text("Home") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = ElegantDark, indicatorColor = ElegantDark.copy(0.1f)))
        NavigationBarItem(selected = currentRoute == "ticket", onClick = { navController.navigate("ticket") { launchSingleTop = true } }, icon = { Icon(if(currentRoute == "ticket") Icons.Default.ConfirmationNumber else Icons.Outlined.ConfirmationNumber, null) }, label = { Text("Tiket") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = ElegantDark, indicatorColor = ElegantDark.copy(0.1f)))
        NavigationBarItem(selected = currentRoute == "profile", onClick = { navController.navigate("profile") { launchSingleTop = true } }, icon = { Icon(if(currentRoute == "profile") Icons.Default.Person else Icons.Outlined.Person, null) }, label = { Text("Profil") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = ElegantDark, indicatorColor = ElegantDark.copy(0.1f)))
    }
}


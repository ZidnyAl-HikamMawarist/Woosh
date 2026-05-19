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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.woosh.ui.screens.*
import com.example.woosh.ui.theme.*

@Composable
fun WooshApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val securityManager = remember { com.example.woosh.utils.SecurityManager(context) }
    
    var currentLanguage by remember { 
        mutableStateOf(
            when(securityManager.getLanguage()) {
                "EN" -> AppLanguage.EN
                "CN" -> AppLanguage.CN
                else -> AppLanguage.ID
            }
        )
    }

    val currentStrings = when(currentLanguage) {
        AppLanguage.ID -> IndonesianStrings
        AppLanguage.EN -> EnglishStrings
        AppLanguage.CN -> ChineseStrings
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    CompositionLocalProvider(LocalWooshStrings provides currentStrings) {
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
                    startDestination = "splash"
                ) {
                    composable("splash") { SplashScreen(navController) }
                    composable("login") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }
                    composable("home") { HomeScreen(navController) }
                    composable(
                        route = "train_list/{dest}/{passengers}/{date}?rescheduleId={rescheduleId}",
                        arguments = listOf(
                            navArgument("dest") { type = NavType.StringType },
                            navArgument("passengers") { type = NavType.IntType },
                            navArgument("date") { type = NavType.LongType },
                            navArgument("rescheduleId") { type = NavType.StringType; nullable = true; defaultValue = null }
                        )
                    ) { backStackEntry -> 
                        val dest = backStackEntry.arguments?.getString("dest") ?: "Halim"
                        val passengers = backStackEntry.arguments?.getInt("passengers") ?: 1
                        val date = backStackEntry.arguments?.getLong("date") ?: System.currentTimeMillis()
                        val rescheduleId = backStackEntry.arguments?.getString("rescheduleId")
                        TrainListScreen(navController, dest, passengers, date, rescheduleId = rescheduleId) 
                    }
                    composable(
                        route = "seat_selection/{passengers}/{price}/{trainId}/{trainName}/{date}?rescheduleId={rescheduleId}",
                        arguments = listOf(
                            navArgument("passengers") { type = NavType.IntType },
                            navArgument("price") { type = NavType.StringType },
                            navArgument("trainId") { type = NavType.StringType },
                            navArgument("trainName") { type = NavType.StringType },
                            navArgument("date") { type = NavType.LongType },
                            navArgument("rescheduleId") { type = NavType.StringType; nullable = true; defaultValue = null }
                        )
                    ) { backStackEntry ->
                        val passengers = backStackEntry.arguments?.getInt("passengers") ?: 1
                        val price = backStackEntry.arguments?.getString("price") ?: "Rp 0"
                        val trainId = backStackEntry.arguments?.getString("trainId") ?: "WOOSH502"
                        val trainName = backStackEntry.arguments?.getString("trainName") ?: "WOOSH 502"
                        val date = backStackEntry.arguments?.getLong("date") ?: System.currentTimeMillis()
                        val rescheduleId = backStackEntry.arguments?.getString("rescheduleId")
                        SeatSelectionScreen(navController, passengers, price, trainId, trainName, date, rescheduleId = rescheduleId)
                    }
                    composable(
                        route = "checkout/{seats}/{price}/{passengers}/{trainId}/{trainName}/{date}?rescheduleId={rescheduleId}",
                        arguments = listOf(
                            navArgument("seats") { type = NavType.StringType },
                            navArgument("price") { type = NavType.StringType },
                            navArgument("passengers") { type = NavType.IntType },
                            navArgument("trainId") { type = NavType.StringType },
                            navArgument("trainName") { type = NavType.StringType },
                            navArgument("date") { type = NavType.LongType },
                            navArgument("rescheduleId") { type = NavType.StringType; nullable = true; defaultValue = null }
                        )
                    ) { backStackEntry -> 
                        val seats = backStackEntry.arguments?.getString("seats") ?: ""
                        val price = backStackEntry.arguments?.getString("price") ?: "Rp 0"
                        val passengers = backStackEntry.arguments?.getInt("passengers") ?: 1
                        val trainId = backStackEntry.arguments?.getString("trainId") ?: "WOOSH502"
                        val trainName = backStackEntry.arguments?.getString("trainName") ?: "WOOSH 502"
                        val date = backStackEntry.arguments?.getLong("date") ?: System.currentTimeMillis()
                        val rescheduleId = backStackEntry.arguments?.getString("rescheduleId")
                        CheckoutScreen(navController, seats, price, passengers, trainId, trainName, date, rescheduleId = rescheduleId)
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
                    composable("notification") { NotificationScreen(navController) }
                    composable("information") { InformationScreen(navController) }
                    composable("whoosher_pass") { WhoosherPassScreen(navController) }
                    composable("group_booking") { GroupBookingScreen(navController) }
                    composable("saved_passengers") { SavedPassengerScreen(navController) }
                    composable("profile") { 
                        ProfileScreen(
                            navController = navController, 
                            onLanguageChange = { 
                                currentLanguage = it
                                securityManager.setLanguage(it.name)
                            },
                            currentLanguage = currentLanguage
                        ) 
                    }
                    composable("settings") {
                        SettingsScreen(
                            navController = navController,
                            onLanguageChange = { 
                                currentLanguage = it
                                securityManager.setLanguage(it.name)
                            },
                            currentLanguage = currentLanguage
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar(containerColor = SurfaceWhite) {
        NavigationBarItem(selected = currentRoute == "home", onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true }; launchSingleTop = true } }, icon = { Icon(if(currentRoute == "home") Icons.Default.Home else Icons.Outlined.Home, null) }, label = { Text(WooshTheme.strings.home) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = WooshRed, indicatorColor = WooshRed.copy(0.1f)))
        NavigationBarItem(selected = currentRoute == "ticket", onClick = { navController.navigate("ticket") { launchSingleTop = true } }, icon = { Icon(if(currentRoute == "ticket") Icons.Default.ConfirmationNumber else Icons.Outlined.ConfirmationNumber, null) }, label = { Text(WooshTheme.strings.tickets) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = WooshRed, indicatorColor = WooshRed.copy(0.1f)))
        NavigationBarItem(selected = currentRoute == "profile", onClick = { navController.navigate("profile") { launchSingleTop = true } }, icon = { Icon(if(currentRoute == "profile") Icons.Default.Person else Icons.Outlined.Person, null) }, label = { Text(WooshTheme.strings.profile) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = WooshRed, indicatorColor = WooshRed.copy(0.1f)))
    }
}

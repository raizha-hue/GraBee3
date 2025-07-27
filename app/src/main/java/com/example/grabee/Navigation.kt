package com.example.grabee

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VENDOR_REGISTRATION = "vendor_registration/{email}"
    const val CUSTOMER_DASHBOARD = "customer_dashboard"
    const val CUSTOMER_DETAILS = "customer_details"
    const val VENDOR_DASHBOARD = "vendor_dashboard"
    const val ADD_FOOD = "add_food"
    const val RIDER_DASHBOARD = "rider_dashboard"
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // Login Screen
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { role ->
                    when (role.lowercase()) {
                        "customer" -> navController.navigate(Routes.CUSTOMER_DASHBOARD) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        "vendor" -> navController.navigate(Routes.VENDOR_DASHBOARD) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        "rider" -> navController.navigate(Routes.RIDER_DASHBOARD) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        // Register Screen
        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onNavigateToVendorRegistration = { email ->
                    navController.navigate("vendor_registration/$email") {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Vendor Registration Screen with email parameter
        composable(
            route = Routes.VENDOR_REGISTRATION,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VendorRegistrationScreen(
                email = email,
                onRegistrationComplete = {
                    navController.navigate(Routes.VENDOR_DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = false }
                    }
                },
                onBackClick = {
                    navController.popBackStack(Routes.REGISTER, false)
                        ?: navController.popBackStack()
                }
            )
        }

        // Customer Dashboard
        composable(Routes.CUSTOMER_DASHBOARD) {
            CustomerDashboardScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0)
                    }
                },
                onNavigateToDetails = {
                    navController.navigate(Routes.CUSTOMER_DETAILS)
                }
            )
        }

        composable(Routes.CUSTOMER_DETAILS) {
            CustomerDetailsScreen(
                onBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        // Vendor Dashboard
        composable(Routes.VENDOR_DASHBOARD) {
            VendorDashboardScreen(
                navController = navController,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0)
                    }
                }
            )
        }

        // Add Food Screen
        composable(Routes.ADD_FOOD) {
            AddFoodScreen(navController = navController)
        }

        // Rider Dashboard
        composable(Routes.RIDER_DASHBOARD) {
            RiderDashboardScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}
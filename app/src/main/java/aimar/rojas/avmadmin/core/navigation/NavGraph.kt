package aimar.rojas.avmadmin.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import aimar.rojas.avmadmin.features.home.presentation.HomeScreen
import aimar.rojas.avmadmin.features.login.presentation.LoginScreen
import aimar.rojas.avmadmin.features.register.presentation.RegisterScreen
import aimar.rojas.avmadmin.features.shipments.presentation.ShipmentsScreen
import aimar.rojas.avmadmin.features.parties.presentation.ProducersScreen
import aimar.rojas.avmadmin.features.parties.presentation.PurchasesScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "login"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        
        composable("register") {
            RegisterScreen(navController = navController)
        }
        
        composable("home") {
            HomeScreen(navController = navController)
        }
        
        composable("shipments") {
            ShipmentsScreen(navController = navController)
        }

        composable("producers") {
            ProducersScreen(navController = navController)
        }

        composable("purchases") {
            PurchasesScreen(navController = navController)
        }
    }
}
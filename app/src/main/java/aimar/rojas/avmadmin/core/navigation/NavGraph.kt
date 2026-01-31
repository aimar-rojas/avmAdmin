package aimar.rojas.avmadmin.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import aimar.rojas.avmadmin.features.home.presentation.HomeScreen
import aimar.rojas.avmadmin.features.login.presentation.LoginScreen
import aimar.rojas.avmadmin.features.register.presentation.RegisterScreen

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
    }
}
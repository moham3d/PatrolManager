package com.patrolshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.patrolshield.presentation.login.LoginScreen
import com.patrolshield.presentation.login.LoginScreen
import com.patrolshield.presentation.dashboard.GuardDashboard
import com.patrolshield.presentation.visitor.VisitorScreen
import com.patrolshield.presentation.shift.ClockInScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userDao: com.patrolshield.data.local.dao.UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        var startDestination = "login"
        kotlinx.coroutines.runBlocking {
            val user = userDao.getUser()
            if (user != null && !user.token.isNullOrEmpty()) {
                startDestination = when (user.role.lowercase()) {
                    "supervisor" -> "supervisor_dashboard"
                    "admin" -> "admin_dashboard"
                    "manager" -> "manager_dashboard"
                    else -> {
                        if (user.activeShiftId != null) "dashboard" else "clock_in"
                    }
                }
            }
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    androidx.compose.foundation.layout.Column {
                        com.patrolshield.presentation.common.OfflineIndicator()
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = startDestination, modifier = Modifier.weight(1f)) {
                            composable("login") {
                            LoginScreen(
                                onNavigateToDashboard = { role ->
                                    val dest = when (role.lowercase()) {
                                        "supervisor" -> "supervisor_dashboard"
                                        "admin" -> "admin_dashboard"
                                        "manager" -> "manager_dashboard"
                                        else -> "clock_in" // Guards must clock in first
                                    }
                                    navController.navigate(dest) {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("clock_in") {
                            ClockInScreen(
                                onClockInSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo("clock_in") { inclusive = true }
                                    }
                                },
                                onLogout = {
                                    kotlinx.coroutines.runBlocking { userDao.clearUser() }
                                    navController.navigate("login") {
                                        popUpTo("clock_in") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("dashboard") {
                            GuardDashboard(
                                onNavigateToProfile = {
                                    navController.navigate("profile")
                                },
                                onStartPatrol = {
                                    navController.navigate("patrol")
                                },
                                onNavigateToVisitors = {
                                    navController.navigate("visitors")
                                }
                            )
                        }
                        composable("visitors") {
                            VisitorScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("supervisor_dashboard") {
                            com.patrolshield.presentation.dashboard.SupervisorDashboard(
                                onLogout = {
                                    kotlinx.coroutines.runBlocking { userDao.clearUser() }
                                    navController.navigate("login") {
                                        popUpTo("supervisor_dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("admin_dashboard") {
                            com.patrolshield.presentation.dashboard.AdminDashboard(
                                onLogout = {
                                    kotlinx.coroutines.runBlocking { userDao.clearUser() }
                                    navController.navigate("login") {
                                        popUpTo("admin_dashboard") { inclusive = true }
                                    }
                                },
                                onNavigateToUsers = { navController.navigate("admin_users") },
                                onNavigateToSites = { navController.navigate("admin_sites") }
                            )
                        }
                        composable("admin_users") {
                            com.patrolshield.presentation.user.UserListScreen(
                                onBack = { navController.popBackStack() },
                                onNavigateToCreate = { navController.navigate("admin_user_create") }
                            )
                        }
                        composable("admin_user_create") {
                            com.patrolshield.presentation.user.UserCreateScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("admin_sites") {
                            com.patrolshield.presentation.site.SiteListScreen(
                                onBack = { navController.popBackStack() },
                                onNavigateToCreate = { navController.navigate("admin_site_create") }
                            )
                        }
                        composable("admin_site_create") {
                            com.patrolshield.presentation.site.SiteCreateScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("manager_dashboard") {
                            com.patrolshield.presentation.dashboard.ManagerDashboard(
                                onLogout = {
                                    kotlinx.coroutines.runBlocking { userDao.clearUser() }
                                    navController.navigate("login") {
                                        popUpTo("manager_dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("patrol") {
                           com.patrolshield.presentation.patrol.PatrolScreen(
                               onEndPatrol = {
                                   navController.navigate("dashboard") {
                                       popUpTo("patrol") { inclusive = true }
                                   }
                               }
                           ) 
                        }
                        composable("profile") {
                            com.patrolshield.presentation.profile.ProfileScreen(
                                onBack = { navController.popBackStack() },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true } // Clear entire stack
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardPlaceholder() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("Welcome to PatrolShield Dashboard!")
    }
}

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrolshield.presentation.login.LoginScreen
import com.patrolshield.presentation.dashboard.GuardDashboard
import com.patrolshield.presentation.dashboard.SupervisorDashboard
import com.patrolshield.presentation.dashboard.SupervisorViewModel
import com.patrolshield.presentation.dashboard.AdminDashboard
import com.patrolshield.presentation.dashboard.ManagerDashboard
import com.patrolshield.presentation.visitor.VisitorScreen
import com.patrolshield.presentation.shift.ClockInScreen
import com.patrolshield.presentation.patrol.PatrolScreen
import com.patrolshield.presentation.profile.ProfileScreen
import com.patrolshield.presentation.user.UserListScreen
import com.patrolshield.presentation.user.UserCreateScreen
import com.patrolshield.presentation.site.SiteListScreen
import com.patrolshield.presentation.site.SiteCreateScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import com.patrolshield.common.NfcManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userDao: com.patrolshield.data.local.dao.UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleNfcIntent(intent)
        
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
                                SupervisorDashboard(
                                    onLogout = {
                                        kotlinx.coroutines.runBlocking { userDao.clearUser() }
                                        navController.navigate("login") {
                                            popUpTo("supervisor_dashboard") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("admin_dashboard") {
                                AdminDashboard(
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
                                UserListScreen(
                                    onBack = { navController.popBackStack() },
                                    onNavigateToCreate = { navController.navigate("admin_user_create") }
                                )
                            }
                            composable("admin_user_create") {
                                UserCreateScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("admin_sites") {
                                SiteListScreen(
                                    onBack = { navController.popBackStack() },
                                    onNavigateToCreate = { navController.navigate("admin_site_create") }
                                )
                            }
                            composable("admin_site_create") {
                                SiteCreateScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("manager_dashboard") {
                                ManagerDashboard(
                                    onLogout = {
                                        kotlinx.coroutines.runBlocking { userDao.clearUser() }
                                        navController.navigate("login") {
                                            popUpTo("manager_dashboard") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("patrol") {
                               PatrolScreen(
                                   onEndPatrol = {
                                       navController.navigate("dashboard") {
                                           popUpTo("patrol") { inclusive = true }
                                       }
                                   }
                               ) 
                            }
                            composable("profile") {
                                ProfileScreen(
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            handleNfcIntent(intent)
        }
    }

    private fun handleNfcIntent(intent: Intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                val tagId = bytesToHexString(it.id)
                CoroutineScope(Dispatchers.Main).launch {
                    NfcManager.onTagDetected(tagId)
                }
            }
        }
    }

    private fun bytesToHexString(src: ByteArray?): String {
        if (src == null || src.isEmpty()) return ""
        val sb = StringBuilder()
        for (b in src) {
            val v = b.toInt() and 0xFF
            val hv = Integer.toHexString(v)
            if (hv.length < 2) sb.append(0)
            sb.append(hv)
        }
        return sb.toString().uppercase()
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
}

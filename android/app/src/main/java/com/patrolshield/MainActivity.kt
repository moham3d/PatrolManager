package com.patrolshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import com.patrolshield.common.NfcManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.patrolshield.presentation.login.LoginScreen
import com.patrolshield.presentation.shift.ClockInScreen
import com.patrolshield.presentation.patrol.PatrolListScreen
import com.patrolshield.presentation.patrol.PatrolExecutionScreen
import com.patrolshield.presentation.incident.IncidentReportScreen
import com.patrolshield.domain.repository.AuthRepository
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.patrolshield.domain.repository.PatrolRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import com.patrolshield.presentation.common.OfflineIndicator

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleNfcIntent(it) }
    }

    private fun handleNfcIntent(intent: Intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                val tagId = bytesToHexString(it.id)
                CoroutineScope(Dispatchers.IO).launch {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    androidx.compose.foundation.layout.Column {
                        OfflineIndicator()
                        val navController = rememberNavController()
                        val startDest = if (authRepository.isAuthenticated()) "clock_in" else "login"
                        
                        NavHost(navController = navController, startDestination = startDest, modifier = Modifier.weight(1f)) {
                            composable("login") {
                                LoginScreen(
                                    onNavigateToDashboard = { role ->
                                        navController.navigate("clock_in") {
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
                                        kotlinx.coroutines.runBlocking { authRepository.logout() }
                                        navController.navigate("login") {
                                            popUpTo("clock_in") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("dashboard") {
                                PatrolListScreen(
                                    onPatrolStarted = { runId, patrolId ->
                                        navController.navigate("patrol_execution/$patrolId/$runId")
                                    }
                                )
                            }
                            composable(
                                route = "patrol_execution/{patrolId}/{runId}",
                                arguments = listOf(
                                    navArgument("patrolId") { type = NavType.IntType },
                                    navArgument("runId") { type = NavType.IntType }
                                )
                            ) { backStackEntry ->
                                val patrolId = backStackEntry.arguments?.getInt("patrolId") ?: -1
                                val runId = backStackEntry.arguments?.getInt("runId") ?: -1
                                PatrolExecutionScreen(
                                    patrolId = patrolId,
                                    runId = runId,
                                    onFinish = {
                                        navController.navigate("dashboard") {
                                            popUpTo("dashboard") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(
                                route = "incident_report/{siteId}/{runId}",
                                arguments = listOf(
                                    navArgument("siteId") { type = NavType.IntType },
                                    navArgument("runId") { type = NavType.IntType; defaultValue = -1 }
                                )
                            ) { backStackEntry ->
                                val siteId = backStackEntry.arguments?.getInt("siteId") ?: -1
                                val runId = backStackEntry.arguments?.getInt("runId").takeIf { it != -1 }
                                IncidentReportScreen(
                                    siteId = siteId,
                                    patrolRunId = runId,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
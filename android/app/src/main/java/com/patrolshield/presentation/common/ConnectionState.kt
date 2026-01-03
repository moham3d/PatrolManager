package com.patrolshield.presentation.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

enum class ConnectionState {
    Available, Unavailable
}

fun Context.observeConnectivityAsFlow(): Flow<ConnectionState> = callbackFlow {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(ConnectionState.Available)
        }

        override fun onLost(network: Network) {
            trySend(ConnectionState.Unavailable)
        }
    }

    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
    
    connectivityManager.registerNetworkCallback(request, callback)

    val initialState = if (isNetworkAvailable(connectivityManager)) {
        ConnectionState.Available
    } else {
        ConnectionState.Unavailable
    }
    trySend(initialState)

    awaitClose {
        connectivityManager.unregisterNetworkCallback(callback)
    }
}.distinctUntilChanged()

private fun isNetworkAvailable(connectivityManager: ConnectivityManager): Boolean {
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

package com.patrolshield.presentation.patrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrolshield.common.Resource
import com.patrolshield.domain.repository.PatrolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckpointScannerViewModel @Inject constructor(
    private val repository: PatrolRepository
) : ViewModel() {

    private val _scanResult = MutableSharedFlow<Resource<String>>()
    val scanResult = _scanResult.asSharedFlow()

    fun onBarcodeDetected(barcode: String, runId: Int, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                // Extract checkpointId from barcode. Data format: "CHECKPOINT:{id}"
                if (barcode.startsWith("CHECKPOINT:")) {
                    val checkpointId = barcode.substringAfter("CHECKPOINT:").toIntOrNull()
                    if (checkpointId != null) {
                        repository.scanCheckpoint(runId, checkpointId, lat, lng).onEach { result ->
                            _scanResult.emit(result)
                        }.launchIn(this)
                    } else {
                        _scanResult.emit(Resource.Error("Invalid QR Code Format"))
                    }
                } else {
                    _scanResult.emit(Resource.Error("Not a PatrolShield QR Code"))
                }
            } catch (e: Exception) {
                _scanResult.emit(Resource.Error("Scan Error: ${e.message}"))
            }
        }
    }
}

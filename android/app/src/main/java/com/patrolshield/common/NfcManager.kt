package com.patrolshield.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NfcManager {
    private val _nfcTag = MutableSharedFlow<String>()
    val nfcTag = _nfcTag.asSharedFlow()

    suspend fun onTagDetected(tagId: String) {
        _nfcTag.emit(tagId)
    }
}

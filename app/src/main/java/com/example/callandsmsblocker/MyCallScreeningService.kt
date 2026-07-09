package com.example.callandsmsblocker

import android.telecom.Call.Details
import android.telecom.CallScreeningService

class MyCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Details) {
        val raw = callDetails.handle?.schemeSpecificPart ?: ""
        val normalized = Utils.normalizeNumber(raw)
        val manager = BlacklistManager.get(this)
        val shouldBlock = manager.isBlockedNumber(normalized)
        val response = if (shouldBlock) {
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(true)
                .setSkipNotification(true)
                .build()
        } else {
            CallResponse.Builder()
                .setDisallowCall(false)
                .build()
        }
        respondToCall(callDetails.callId, response)
    }
}

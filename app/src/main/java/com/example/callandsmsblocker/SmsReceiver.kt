package com.example.callandsmsblocker

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val manager = BlacklistManager.get(context)
            val messages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Telephony.Sms.Intents.getMessagesFromIntent(intent)
            } else {
                arrayOf<SmsMessage>()
            }
            if (messages.isEmpty()) return

            val senderRaw = messages[0].originatingAddress ?: ""
            val body = StringBuilder()
            for (m in messages) body.append(m.messageBody)
            val normalized = Utils.normalizeNumber(senderRaw)

            if (manager.isBlockedNumber(normalized)) {
                Log.i("SmsReceiver", "Blocked SMS from $normalized")
                abortBroadcast()
                return
            }

            if (intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION) {
                val values = ContentValues().apply {
                    put("address", senderRaw)
                    put("body", body.toString())
                    put("date", System.currentTimeMillis())
                    put("read", 0)
                    put("seen", 0)
                }
                try {
                    context.contentResolver.insert(Uri.parse("content://sms/inbox"), values)
                } catch (e: Exception) {
                    Log.w("SmsReceiver", "Insert sms failed: ${e.message}")
                }
                abortBroadcast()
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Error handling SMS: ${e.message}")
        }
    }
}

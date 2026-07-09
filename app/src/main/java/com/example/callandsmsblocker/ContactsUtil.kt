package com.example.callandsmsblocker

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri

object ContactsUtil {
    fun isNumberInContacts(context: Context, numberE164: String): Boolean {
        try {
            val resolver: ContentResolver = context.contentResolver
            val uri = Uri.withAppendedPath(Uri.parse("content://com.android.contacts/phone_lookup"), Uri.encode(numberE164))
            val cursor: Cursor? = resolver.query(uri, arrayOf("_id"), null, null, null)
            cursor?.use {
                if (it.moveToFirst()) return true
            }
        } catch (e: Exception) {
            // ignore
        }
        return false
    }
}

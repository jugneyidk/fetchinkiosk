package com.fetchin.kiosk.security

import android.util.Log

class KioskLogger(private val tag: String = "FetchinKiosk") {
    fun info(message: String) {
        Log.i(tag, message)
    }

    fun warning(message: String) {
        Log.w(tag, message)
    }
}

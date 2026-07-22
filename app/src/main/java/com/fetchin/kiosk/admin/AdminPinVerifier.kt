package com.fetchin.kiosk.admin

interface AdminPinVerifier {
    fun verify(pinCandidate: CharArray): Boolean
}

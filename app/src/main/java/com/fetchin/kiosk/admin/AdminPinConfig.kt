package com.fetchin.kiosk.admin

data class AdminPinConfig(
    val hashBase64: String,
    val saltBase64: String,
    val iterations: Int,
    val keyLengthBits: Int
) {
    val isConfigured: Boolean
        get() = hashBase64.isNotBlank() && saltBase64.isNotBlank() && iterations > 0 && keyLengthBits > 0
}

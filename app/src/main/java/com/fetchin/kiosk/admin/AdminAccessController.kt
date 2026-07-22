package com.fetchin.kiosk.admin

class AdminAccessController(
    private val requiredTapCount: Int,
    private val windowMillis: Long
) {
    private val tapTimes = ArrayDeque<Long>()

    fun recordTap(nowMillis: Long): Boolean {
        tapTimes.addLast(nowMillis)
        while (tapTimes.isNotEmpty() && nowMillis - tapTimes.first() > windowMillis) {
            tapTimes.removeFirst()
        }
        return tapTimes.size >= requiredTapCount
    }

    fun reset() {
        tapTimes.clear()
    }
}

package com.fetchin.kiosk.admin

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminAccessControllerTest {
    @Test
    fun opensAfterRequiredTapsWithinWindow() {
        val controller = AdminAccessController(requiredTapCount = 3, windowMillis = 1_000L)

        assertFalse(controller.recordTap(1_000L))
        assertFalse(controller.recordTap(1_100L))
        assertTrue(controller.recordTap(1_200L))
    }

    @Test
    fun ignoresTapsOutsideWindow() {
        val controller = AdminAccessController(requiredTapCount = 3, windowMillis = 100L)

        assertFalse(controller.recordTap(1_000L))
        assertFalse(controller.recordTap(1_200L))
        assertFalse(controller.recordTap(1_400L))
    }

    @Test
    fun resetClearsProgress() {
        val controller = AdminAccessController(requiredTapCount = 2, windowMillis = 1_000L)

        assertFalse(controller.recordTap(1_000L))
        controller.reset()
        assertFalse(controller.recordTap(1_100L))
    }
}

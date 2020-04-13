package net.sunshow.toolkit.core.enums.base

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * author: sunshow.
 */
class EnableStatusTest {

    @Test
    fun testEnableStatus() {
        Assertions.assertEquals(EnableStatus.DISABLE, EnableStatus[0])
    }

}
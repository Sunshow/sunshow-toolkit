package net.sunshow.toolkit.kotlin.base.enums

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
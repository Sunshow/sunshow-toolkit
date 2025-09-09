package net.sunshow.toolkit.core.qbean.helper.utils

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * TypeCompatibilityUtils 测试类
 */
class TypeCompatibilityUtilsTest {

    @Test
    fun `test primitive and wrapper type compatibility`() {
        // 测试 int 和 Integer
        val intValue = 42.javaClass
        assertTrue(TypeCompatibilityUtils.isAssignableFrom(Int::class.javaPrimitiveType!!, intValue))
        assertTrue(TypeCompatibilityUtils.isAssignableFrom(Int::class.java, intValue))

        // 测试 long 和 Long
        val longValue = 42L.javaClass
        assertTrue(TypeCompatibilityUtils.isAssignableFrom(Long::class.javaPrimitiveType!!, longValue))
        assertTrue(TypeCompatibilityUtils.isAssignableFrom(Long::class.java, longValue))

        // 测试 double 和 Double
        val doubleValue = 42.0.javaClass
        assertTrue(TypeCompatibilityUtils.isAssignableFrom(Double::class.javaPrimitiveType!!, doubleValue))
        assertTrue(TypeCompatibilityUtils.isAssignableFrom(Double::class.java, doubleValue))

        // 测试 boolean 和 Boolean
        val booleanValue = true.javaClass
        assertTrue(TypeCompatibilityUtils.isAssignableFrom(Boolean::class.javaPrimitiveType!!, booleanValue))
        assertTrue(TypeCompatibilityUtils.isAssignableFrom(Boolean::class.java, booleanValue))

        // 测试不匹配的类型
        assertFalse(TypeCompatibilityUtils.isAssignableFrom(String::class.java, intValue))
        assertFalse(TypeCompatibilityUtils.isAssignableFrom(Double::class.java, intValue))
    }

    @Test
    fun `test regular type compatibility`() {
        val stringValue = "hello".javaClass
        assertTrue(TypeCompatibilityUtils.isAssignableFrom(String::class.java, stringValue))
        assertTrue(TypeCompatibilityUtils.isAssignableFrom(CharSequence::class.java, stringValue))
        assertFalse(TypeCompatibilityUtils.isAssignableFrom(Int::class.java, stringValue))
    }
}
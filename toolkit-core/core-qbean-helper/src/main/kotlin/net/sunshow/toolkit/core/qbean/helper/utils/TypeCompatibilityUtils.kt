package net.sunshow.toolkit.core.qbean.helper.utils

/**
 * 类型兼容性工具类，用于处理 Java 基本类型和包装类型之间的兼容性检查
 *
 * @author sunshow
 */
object TypeCompatibilityUtils {

    /**
     * 检查参数类型是否与值的类型兼容，包括基本类型和包装类型的自动装箱/拆箱
     */
    @JvmStatic
    fun isAssignableFrom(parentClass: Class<*>, childClass: Class<*>): Boolean {
        val isAssignable = parentClass.isAssignableFrom(childClass)
        if (isAssignable) {
            return true
        }

        // 装箱后再比对一次
        return parentClass.kotlin.javaObjectType.isAssignableFrom(childClass.kotlin.javaObjectType)
    }

}
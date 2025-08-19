package net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import net.sunshow.toolkit.core.qbean.api.annotation.QBeanCreatorIgnore
import net.sunshow.toolkit.core.qbean.api.annotation.QBeanID
import net.sunshow.toolkit.core.qbean.api.annotation.QBeanUpdaterIgnore

object KspUtils {

    /**
     * 获取类的所有属性（包括构造函数参数和成员属性）
     */
    fun getAllProperties(classDeclaration: KSClassDeclaration): List<KSPropertyDeclaration> {
        val properties = mutableListOf<KSPropertyDeclaration>()

        // 获取所有声明的属性
        properties.addAll(classDeclaration.getAllProperties())

        return properties.distinctBy { it.simpleName.asString() }
    }

    /**
     * 检查属性是否有特定注解
     */
    inline fun <reified T : Annotation> KSPropertyDeclaration.hasAnnotation(): Boolean {
        return annotations.any {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == T::class.qualifiedName
        }
    }

    /**
     * 获取属性的特定注解
     */
    inline fun <reified T : Annotation> KSPropertyDeclaration.findAnnotation(): KSAnnotation? {
        return annotations.find {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == T::class.qualifiedName
        }
    }

    /**
     * 检查类是否有特定注解
     */
    inline fun <reified T : Annotation> KSClassDeclaration.hasAnnotation(): Boolean {
        return annotations.any {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == T::class.qualifiedName
        }
    }

    /**
     * 获取类的特定注解
     */
    inline fun <reified T : Annotation> KSClassDeclaration.findAnnotation(): KSAnnotation? {
        return annotations.find {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == T::class.qualifiedName
        }
    }

    /**
     * 获取注解参数值
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> KSAnnotation.getArgumentValue(name: String): T? {
        return arguments.find { it.name?.asString() == name }?.value as? T
    }

    /**
     * 获取注解参数值，带默认值
     */
    fun <T> KSAnnotation.getArgumentValue(name: String, default: T): T {
        @Suppress("UNCHECKED_CAST")
        return arguments.find { it.name?.asString() == name }?.value as? T ?: default
    }

    /**
     * 将字符串转换为驼峰命名（首字母大写）
     */
    fun toCamelCase(s: String): String {
        return s.replaceFirstChar { it.uppercase() }
    }

    /**
     * 检查属性是否应该被 Creator 忽略
     */
    fun shouldIgnoreForCreator(property: KSPropertyDeclaration): Boolean {
        return property.hasAnnotation<QBeanCreatorIgnore>()
    }

    /**
     * 检查属性是否应该被 Updater 忽略
     */
    fun shouldIgnoreForUpdater(property: KSPropertyDeclaration): Boolean {
        return property.hasAnnotation<QBeanUpdaterIgnore>()
    }

    /**
     * 检查属性是否是 ID 属性
     */
    fun isIdProperty(property: KSPropertyDeclaration): Boolean {
        return property.hasAnnotation<QBeanID>()
    }

    /**
     * 获取类型的 TypeName
     */
    fun KSTypeReference.toTypeName(): TypeName {
        return resolve().toTypeName()
    }

    /**
     * 获取类的包名
     */
    fun KSClassDeclaration.getPackageName(): String {
        return packageName.asString()
    }

    /**
     * 获取类的简单名称
     */
    fun KSClassDeclaration.getSimpleName(): String {
        return simpleName.asString()
    }

    /**
     * 检查类是否是 data class
     */
    fun KSClassDeclaration.isDataClass(): Boolean {
        return modifiers.contains(Modifier.DATA)
    }

    /**
     * 日志辅助方法
     */
    fun KSPLogger.debug(message: String, symbol: KSNode? = null) {
        info(message, symbol)
    }
}
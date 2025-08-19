package net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import net.sunshow.toolkit.core.qbean.api.annotation.QBean
import net.sunshow.toolkit.core.qbean.api.annotation.QBeanID
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.findAnnotation
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.getArgumentValue
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.getPackageName
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.getSimpleName
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.hasAnnotation

class QBeanGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) {
    
    fun generate(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.getPackageName()
        val className = classDeclaration.getSimpleName()
        val qBeanClassName = "Q$className"
        
        logger.info("Generating $qBeanClassName for $className")
        
        // 创建 object
        val objectBuilder = TypeSpec.objectBuilder(qBeanClassName)
            .addModifiers(KModifier.PUBLIC)
        
        val properties = KspUtils.getAllProperties(classDeclaration)
        var hasQBeanId = false
        
        // 添加属性常量
        properties.forEach { property ->
            val propertyName = property.simpleName.asString()
            
            // 检查是否是 ID 属性
            if (property.hasAnnotation<QBeanID>()) {
                hasQBeanId = true
            }
            
            // 添加常量
            objectBuilder.addProperty(
                PropertySpec.builder(propertyName, String::class)
                    .addModifiers(KModifier.CONST)
                    .initializer("%S", propertyName)
                    .build()
            )
        }
        
        // 如果没有找到 @QBeanID，检查是否需要添加默认 ID 属性
        if (!hasQBeanId) {
            val qBeanAnnotation = classDeclaration.findAnnotation<QBean>()
            if (qBeanAnnotation != null) {
                val defaultIdProperty = qBeanAnnotation.getArgumentValue("defaultIdProperty", true)
                if (defaultIdProperty) {
                    val defaultIdPropertyName = qBeanAnnotation.getArgumentValue("defaultIdPropertyName", "id")
                    
                    // 添加默认 ID 常量
                    objectBuilder.addProperty(
                        PropertySpec.builder(defaultIdPropertyName, String::class)
                            .addModifiers(KModifier.CONST)
                            .initializer("%S", defaultIdPropertyName)
                            .build()
                    )
                }
            }
        }
        
        // 创建文件
        val file = FileSpec.builder(packageName, qBeanClassName)
            .addType(objectBuilder.build())
            .build()
        
        // 写入文件
        codeGenerator.createNewFile(
            dependencies = Dependencies(false, classDeclaration.containingFile!!),
            packageName = packageName,
            fileName = qBeanClassName
        ).use { outputStream ->
            outputStream.writer().use { writer ->
                file.writeTo(writer)
            }
        }
    }
}
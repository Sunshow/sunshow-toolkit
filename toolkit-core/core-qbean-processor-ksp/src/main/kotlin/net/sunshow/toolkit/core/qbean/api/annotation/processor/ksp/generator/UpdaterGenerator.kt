package net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import net.sunshow.toolkit.core.qbean.api.annotation.QBean
import net.sunshow.toolkit.core.qbean.api.annotation.QBeanUpdater
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.findAnnotation
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.getArgumentValue
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.getPackageName
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.getSimpleName
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.hasAnnotation
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.hasCompanionObject
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.isIdProperty
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.shouldIgnoreForUpdater
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.shouldFilterForJava
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.toCamelCase
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.toImmutableCollectionType
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanUpdater

class UpdaterGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) {

    fun generate(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.getPackageName()
        val className = classDeclaration.getSimpleName()
        val updaterClassName = "${className}Updater"

        logger.info("Generating $updaterClassName for $className")

        val updaterClassBuilder = TypeSpec.classBuilder(updaterClassName)
            .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
            .addSuperinterface(
                BaseQBeanUpdater::class.asClassName()
                    .parameterizedBy(classDeclaration.toClassName())
            )

        // 查找 ID 属性类型
        var idPropertyType: TypeName? = null
        var idPropertyFound = false

        val properties = KspUtils.getAllProperties(classDeclaration)

        // 首先查找标记了 @QBeanID 的属性
        properties.forEach { property ->
            if (isIdProperty(property)) {
                idPropertyType = property.type.resolve().toTypeName()
                idPropertyFound = true
                return@forEach
            }
        }

        // 如果没有找到 @QBeanID，使用默认配置
        if (!idPropertyFound) {
            val qBeanAnnotation = classDeclaration.findAnnotation<QBean>()
            if (qBeanAnnotation != null) {
                val defaultIdProperty = qBeanAnnotation.getArgumentValue("defaultIdProperty", true)
                if (defaultIdProperty) {
                    val defaultType = qBeanAnnotation.getArgumentValue<KSType>("defaultIdPropertyType")
                    idPropertyType = defaultType?.toTypeName() ?: Long::class.asTypeName()
                    logger.info("Using default id property type: $idPropertyType")
                }
            }
        }

        if (idPropertyType == null) {
            logger.error("Cannot find id property type for $className, using Long as default")
            idPropertyType = Long::class.asTypeName()
        }

        // 添加 updateId 字段
        updaterClassBuilder.addProperty(
            PropertySpec.builder("updateId", idPropertyType)
                .addModifiers(KModifier.PRIVATE)
                .build()
        )

        // 添加 getUpdateId 方法
        updaterClassBuilder.addFunction(
            FunSpec.builder("getUpdateId")
                .addModifiers(KModifier.OVERRIDE)
                .returns(idPropertyType)
                .addStatement("return updateId")
                .build()
        )

        // 添加 updateProperties 字段
        val setOfString = Set::class.asClassName().parameterizedBy(String::class.asClassName())
        updaterClassBuilder.addProperty(
            PropertySpec.builder("updateProperties", setOfString)
                .addModifiers(KModifier.PRIVATE)
                .initializer("mutableSetOf()")
                .mutable(false)
                .build()
        )

        // 添加 getUpdateProperties 方法
        updaterClassBuilder.addFunction(
            FunSpec.builder("getUpdateProperties")
                .addModifiers(KModifier.OVERRIDE)
                .returns(setOfString)
                .addStatement("return updateProperties")
                .build()
        )

        // 收集需要更新的属性
        val updateProperties = mutableListOf<PropertyInfo>()
        val hasClassLevelUpdater = classDeclaration.hasAnnotation<QBeanUpdater>()

        properties.forEach { property ->
            // 跳过 Java 静态/final 字段
            if (property.shouldFilterForJava()) {
                return@forEach
            }

            // 跳过 ID 属性
            if (isIdProperty(property)) {
                return@forEach
            }

            // 跳过被忽略的属性
            if (shouldIgnoreForUpdater(property)) {
                return@forEach
            }

            // 如果类级别有 @QBeanUpdater 或属性有 @QBeanUpdater，则包含该属性
            if (hasClassLevelUpdater || property.hasAnnotation<QBeanUpdater>()) {
                updateProperties.add(
                    PropertyInfo(
                        name = property.simpleName.asString(),
                        type = property.type.resolve().toTypeName()
                    )
                )
            }
        }

        // 添加属性字段（public getter, internal setter）
        updateProperties.forEach { propertyInfo ->
            val fieldType = propertyInfo.type.toImmutableCollectionType().copy(nullable = true)
            updaterClassBuilder.addProperty(
                PropertySpec.builder(propertyInfo.name, fieldType)
                    .mutable(true)
                    .initializer("null")
                    .setter(FunSpec.setterBuilder().addModifiers(KModifier.INTERNAL).build())
                    .build()
            )
        }

        // 添加私有构造函数
        updaterClassBuilder.primaryConstructor(
            FunSpec.constructorBuilder()
                .addModifiers(KModifier.PRIVATE)
                .addParameter("updateId", idPropertyType)
                .build()
        )
        updaterClassBuilder.addInitializerBlock(
            CodeBlock.builder()
                .addStatement("this.updateId = updateId")
                .build()
        )

        // 创建 Builder 类
        val builderClassName = ClassName(packageName, updaterClassName, "Builder")
        val builderClassBuilder = TypeSpec.classBuilder("Builder")
            .addModifiers(KModifier.PUBLIC)

        // Builder 类添加 updater 字段
        builderClassBuilder.addProperty(
            PropertySpec.builder("updater", ClassName(packageName, updaterClassName))
                .addModifiers(KModifier.PRIVATE)
                .build()
        )

        // Builder 构造函数 - 改为 internal
        builderClassBuilder.primaryConstructor(
            FunSpec.constructorBuilder()
                .addModifiers(KModifier.INTERNAL)
                .addParameter("updateId", idPropertyType)
                .build()
        )
        builderClassBuilder.addInitializerBlock(
            CodeBlock.builder()
                .addStatement("this.updater = %T(updateId)", ClassName(packageName, updaterClassName))
                .build()
        )

        // 为每个属性添加 with 方法和属性访问器
        updateProperties.forEach { propertyInfo ->
            // withXxx 方法（保持 Java 兼容）
            builderClassBuilder.addFunction(
                FunSpec.builder("with${toCamelCase(propertyInfo.name)}")
                    .addParameter(propertyInfo.name, propertyInfo.type.toImmutableCollectionType().copy(nullable = true))
                    .returns(builderClassName)
                    .addStatement("updater.${propertyInfo.name} = ${propertyInfo.name}")
                    .addStatement("(updater.updateProperties as MutableSet).add(%S)", propertyInfo.name)
                    .addStatement("return this")
                    .build()
            )

            // Kotlin 属性风格访问器
            val builderFieldType = propertyInfo.type.toImmutableCollectionType().copy(nullable = true)
            builderClassBuilder.addProperty(
                PropertySpec.builder(propertyInfo.name, builderFieldType)
                    .mutable(true)
                    .getter(
                        FunSpec.getterBuilder()
                            .addStatement("return updater.${propertyInfo.name}")
                            .build()
                    )
                    .setter(
                        FunSpec.setterBuilder()
                            .addParameter("value", builderFieldType)
                            .addStatement("updater.${propertyInfo.name} = value")
                            .addStatement("(updater.updateProperties as MutableSet).add(%S)", propertyInfo.name)
                            .build()
                    )
                    .build()
            )
        }

        // 添加 build 方法
        builderClassBuilder.addFunction(
            FunSpec.builder("build")
                .returns(ClassName(packageName, updaterClassName))
                .addStatement("return updater")
                .build()
        )

        // 将 Builder 类添加到 Updater 类
        updaterClassBuilder.addType(builderClassBuilder.build())

        // 添加 companion object 和静态 builder 方法
        val companionObjectBuilder = TypeSpec.companionObjectBuilder()
        companionObjectBuilder.addFunction(
            FunSpec.builder("builder")
                .addAnnotation(JvmStatic::class)
                .addParameter("updateId", idPropertyType)
                .returns(builderClassName)
                .addStatement("return Builder(updateId)")
                .build()
        )
        updaterClassBuilder.addType(companionObjectBuilder.build())

        // 创建文件构建器
        val fileBuilder = FileSpec.builder(packageName, updaterClassName)
            .addType(updaterClassBuilder.build())

        // 检查原始类是否有 companion object
        val hasCompanion = classDeclaration.hasCompanionObject()

        if (hasCompanion) {
            // 如果有 companion，生成 companion 扩展函数
            fileBuilder.addFunction(
                FunSpec.builder("update")
                    .receiver(ClassName(packageName, className).nestedClass("Companion"))
                    .addModifiers(KModifier.INLINE)
                    .addParameter("id", idPropertyType)
                    .addParameter(
                        "block", LambdaTypeName.get(
                            receiver = builderClassName,
                            returnType = Unit::class.asTypeName()
                        )
                    )
                    .returns(ClassName(packageName, updaterClassName))
                    .addStatement(
                        "return %T.builder(id).apply(block).build()",
                        ClassName(packageName, updaterClassName)
                    )
                    .build()
            )
        } else {
            // 如果没有 companion，使用 QBean 类作为扩展函数的接收者
            val qBeanClassName = "Q$className"

            // 添加到 QBean 对象的扩展函数
            fileBuilder.addFunction(
                FunSpec.builder("update")
                    .receiver(ClassName(packageName, qBeanClassName))
                    .addModifiers(KModifier.INLINE)
                    .addParameter("id", idPropertyType)
                    .addParameter(
                        "block", LambdaTypeName.get(
                            receiver = builderClassName,
                            returnType = Unit::class.asTypeName()
                        )
                    )
                    .returns(ClassName(packageName, updaterClassName))
                    .addStatement(
                        "return %T.builder(id).apply(block).build()",
                        ClassName(packageName, updaterClassName)
                    )
                    .build()
            )
        }

        val file = fileBuilder.build()

        // 写入文件
        codeGenerator.createNewFile(
            dependencies = Dependencies(false, classDeclaration.containingFile!!),
            packageName = packageName,
            fileName = updaterClassName
        ).use { outputStream ->
            outputStream.writer().use { writer ->
                file.writeTo(writer)
            }
        }
    }

    private data class PropertyInfo(
        val name: String,
        val type: TypeName
    )
}
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
import net.sunshow.toolkit.core.qbean.api.annotation.QBeanCreator
import net.sunshow.toolkit.core.qbean.api.annotation.QBeanID
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.findAnnotation
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.getArgumentValue
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.getPackageName
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.getSimpleName
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.hasAnnotation
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.shouldIgnoreForCreator
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.toCamelCase
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.hasCompanionObject
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanCreator

class CreatorGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) {

    fun generate(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.getPackageName()
        val className = classDeclaration.getSimpleName()
        val creatorClassName = "${className}Creator"

        logger.info("Generating $creatorClassName for $className")

        val creatorClassBuilder = TypeSpec.classBuilder(creatorClassName)
            .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
            .addSuperinterface(
                BaseQBeanCreator::class.asClassName()
                    .parameterizedBy(classDeclaration.toClassName())
            )

        // 添加 createProperties 字段
        val setOfString = Set::class.asClassName().parameterizedBy(String::class.asClassName())
        creatorClassBuilder.addProperty(
            PropertySpec.builder("createProperties", setOfString)
                .addModifiers(KModifier.PRIVATE)
                .initializer("mutableSetOf()")
                .mutable(false)
                .build()
        )

        // 添加 getCreateProperties 方法
        creatorClassBuilder.addFunction(
            FunSpec.builder("getCreateProperties")
                .addModifiers(KModifier.OVERRIDE)
                .returns(setOfString)
                .addStatement("return createProperties")
                .build()
        )

        // 收集需要创建的属性
        val createProperties = mutableListOf<PropertyInfo>()
        var hasQBeanId = false

        val properties = KspUtils.getAllProperties(classDeclaration)
        val hasClassLevelCreator = classDeclaration.hasAnnotation<QBeanCreator>()

        properties.forEach { property ->
            // 跳过被忽略的属性
            if (shouldIgnoreForCreator(property)) {
                return@forEach
            }

            // 检查是否是 ID 属性
            if (property.hasAnnotation<QBeanID>()) {
                hasQBeanId = true
                return@forEach // ID 属性通常不在 Creator 中
            }

            // 如果类级别有 @QBeanCreator 或属性有 @QBeanCreator，则包含该属性
            if (hasClassLevelCreator || property.hasAnnotation<QBeanCreator>()) {
                createProperties.add(
                    PropertyInfo(
                        name = property.simpleName.asString(),
                        type = property.type!!.resolve().toTypeName()
                    )
                )
            }
        }

        // 处理默认 ID 属性
        if (!hasQBeanId) {
            val qBeanAnnotation = classDeclaration.findAnnotation<QBean>()
            if (qBeanAnnotation != null) {
                val defaultIdProperty = qBeanAnnotation.getArgumentValue("defaultIdProperty", true)
                val defaultIdPropertyCreatorIgnore =
                    qBeanAnnotation.getArgumentValue("defaultIdPropertyCreatorIgnore", true)

                if (defaultIdProperty && !defaultIdPropertyCreatorIgnore) {
                    val defaultIdPropertyName = qBeanAnnotation.getArgumentValue("defaultIdPropertyName", "id")
                    val defaultIdPropertyType = qBeanAnnotation.getArgumentValue<KSType>("defaultIdPropertyType")
                        ?.toTypeName() ?: Long::class.asTypeName()

                    createProperties.add(0, PropertyInfo(defaultIdPropertyName, defaultIdPropertyType))
                }
            }
        }

        // 添加属性字段和 getter
        createProperties.forEach { propertyInfo ->
            // 添加字段 - 使用可空类型
            creatorClassBuilder.addProperty(
                PropertySpec.builder(propertyInfo.name, propertyInfo.type.copy(nullable = true))
                    .addModifiers(KModifier.PRIVATE)
                    .mutable(true)
                    .initializer("null")
                    .build()
            )

            // 添加 getter
            creatorClassBuilder.addFunction(
                FunSpec.builder("get${toCamelCase(propertyInfo.name)}")
                    .returns(propertyInfo.type.copy(nullable = true))
                    .addStatement("return ${propertyInfo.name}")
                    .build()
            )
        }

        // 添加私有构造函数
        creatorClassBuilder.primaryConstructor(
            FunSpec.constructorBuilder()
                .addModifiers(KModifier.PRIVATE)
                .build()
        )

        // 创建 Builder 类
        val builderClassName = ClassName(packageName, creatorClassName, "Builder")
        val builderClassBuilder = TypeSpec.classBuilder("Builder")
            .addModifiers(KModifier.PUBLIC)

        // Builder 类添加 creator 字段
        builderClassBuilder.addProperty(
            PropertySpec.builder("creator", ClassName(packageName, creatorClassName))
                .addModifiers(KModifier.PRIVATE)
                .initializer("%T()", ClassName(packageName, creatorClassName))
                .build()
        )

        // Builder 构造函数 - 改为 internal
        builderClassBuilder.primaryConstructor(
            FunSpec.constructorBuilder()
                .addModifiers(KModifier.INTERNAL)
                .build()
        )

        // 为每个属性添加 with 方法和属性访问器
        createProperties.forEach { propertyInfo ->
            // withXxx 方法（保持 Java 兼容）
            builderClassBuilder.addFunction(
                FunSpec.builder("with${toCamelCase(propertyInfo.name)}")
                    .addParameter(propertyInfo.name, propertyInfo.type)
                    .returns(builderClassName)
                    .addStatement("creator.${propertyInfo.name} = ${propertyInfo.name}")
                    .addStatement("(creator.createProperties as MutableSet).add(%S)", propertyInfo.name)
                    .addStatement("return this")
                    .build()
            )
            
            // Kotlin 属性风格访问器
            builderClassBuilder.addProperty(
                PropertySpec.builder(propertyInfo.name, propertyInfo.type.copy(nullable = true))
                    .mutable(true)
                    .getter(
                        FunSpec.getterBuilder()
                            .addStatement("return creator.${propertyInfo.name}")
                            .build()
                    )
                    .setter(
                        FunSpec.setterBuilder()
                            .addParameter("value", propertyInfo.type.copy(nullable = true))
                            .addStatement("creator.${propertyInfo.name} = value")
                            .addStatement("(creator.createProperties as MutableSet).add(%S)", propertyInfo.name)
                            .build()
                    )
                    .build()
            )
        }

        // 添加 build 方法
        builderClassBuilder.addFunction(
            FunSpec.builder("build")
                .returns(ClassName(packageName, creatorClassName))
                .addStatement("return creator")
                .build()
        )

        // 将 Builder 类添加到 Creator 类
        creatorClassBuilder.addType(builderClassBuilder.build())

        // 添加 companion object 和静态 builder 方法
        val companionObjectBuilder = TypeSpec.companionObjectBuilder()
        companionObjectBuilder.addFunction(
            FunSpec.builder("builder")
                .addAnnotation(JvmStatic::class)
                .returns(builderClassName)
                .addStatement("return Builder()")
                .build()
        )
        creatorClassBuilder.addType(companionObjectBuilder.build())

        // 创建文件构建器
        val fileBuilder = FileSpec.builder(packageName, creatorClassName)
            .addType(creatorClassBuilder.build())
        
        // 检查原始类是否有 companion object
        val hasCompanion = classDeclaration.hasCompanionObject()
        
        if (hasCompanion) {
            // 如果有 companion，生成 companion 扩展函数
            fileBuilder.addFunction(
                FunSpec.builder("create")
                    .receiver(ClassName(packageName, className).nestedClass("Companion"))
                    .addModifiers(KModifier.INLINE)
                    .addParameter("block", LambdaTypeName.get(
                        receiver = builderClassName,
                        returnType = Unit::class.asTypeName()
                    ))
                    .returns(ClassName(packageName, creatorClassName))
                    .addStatement("return %T.builder().apply(block).build()", ClassName(packageName, creatorClassName))
                    .build()
            )
        } else {
            // 如果没有 companion，使用 QBean 类作为扩展函数的接收者
            val qBeanClassName = "Q$className"
            
            // 生成扩展函数，使用 QBean 类作为接收者
            fileBuilder.addFunction(
                FunSpec.builder("create")
                    .receiver(ClassName(packageName, qBeanClassName))
                    .addModifiers(KModifier.INLINE)
                    .addParameter("block", LambdaTypeName.get(
                        receiver = builderClassName,
                        returnType = Unit::class.asTypeName()
                    ))
                    .returns(ClassName(packageName, creatorClassName))
                    .addStatement("return %T.builder().apply(block).build()", ClassName(packageName, creatorClassName))
                    .build()
            )
        }
        
        val file = fileBuilder.build()

        // 写入文件
        codeGenerator.createNewFile(
            dependencies = Dependencies(false, classDeclaration.containingFile!!),
            packageName = packageName,
            fileName = creatorClassName
        ).use { outputStream ->
            outputStream.writer().use { writer ->
                file.writeTo(writer)
            }
        }
    }

    companion object {
        // 添加 companion object 使 builder 方法看起来像静态的
        fun TypeSpec.Builder.addCompanionObject(block: TypeSpec.Builder.() -> Unit): TypeSpec.Builder {
            return this.addType(
                TypeSpec.companionObjectBuilder()
                    .apply(block)
                    .build()
            )
        }
    }

    private data class PropertyInfo(
        val name: String,
        val type: TypeName
    )
}
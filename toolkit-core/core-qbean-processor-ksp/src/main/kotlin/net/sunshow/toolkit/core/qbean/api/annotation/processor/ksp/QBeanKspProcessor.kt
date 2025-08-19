package net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import net.sunshow.toolkit.core.qbean.api.annotation.QBean
import net.sunshow.toolkit.core.qbean.api.annotation.QBeanCreator
import net.sunshow.toolkit.core.qbean.api.annotation.QBeanUpdater
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.generator.CreatorGenerator
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.generator.QBeanGenerator
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.generator.UpdaterGenerator
import net.sunshow.toolkit.core.qbean.api.annotation.processor.ksp.utils.KspUtils.hasAnnotation

class QBeanKspProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {
    
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val qBeanSymbols = resolver.getSymbolsWithAnnotation(QBean::class.qualifiedName!!)
        val unresolvedSymbols = mutableListOf<KSAnnotated>()
        
        qBeanSymbols.forEach { symbol ->
            if (!symbol.validate()) {
                unresolvedSymbols.add(symbol)
                return@forEach
            }
            
            if (symbol is KSClassDeclaration) {
                try {
                    processQBeanClass(symbol)
                } catch (e: Exception) {
                    logger.error("Error processing @QBean class: ${symbol.simpleName.asString()}", symbol)
                    logger.exception(e)
                }
            } else {
                logger.error("@QBean can only be applied to classes", symbol)
            }
        }
        
        return unresolvedSymbols
    }
    
    private fun processQBeanClass(classDeclaration: KSClassDeclaration) {
        logger.info("Processing @QBean class: ${classDeclaration.qualifiedName?.asString()}")
        
        // 只处理 Kotlin 类，跳过 Java 类
        val originFile = classDeclaration.containingFile
        if (originFile != null && originFile.filePath.endsWith(".java")) {
            logger.info("Skipping Java class: ${classDeclaration.qualifiedName?.asString()}")
            return
        }
        
        // 生成 Q{ClassName} object
        val qBeanGenerator = QBeanGenerator(codeGenerator, logger)
        qBeanGenerator.generate(classDeclaration)
        
        // 检查是否需要生成 Creator
        if (shouldGenerateCreator(classDeclaration)) {
            val creatorGenerator = CreatorGenerator(codeGenerator, logger)
            creatorGenerator.generate(classDeclaration)
        }
        
        // 检查是否需要生成 Updater
        if (shouldGenerateUpdater(classDeclaration)) {
            val updaterGenerator = UpdaterGenerator(codeGenerator, logger)
            updaterGenerator.generate(classDeclaration)
        }
    }
    
    private fun shouldGenerateCreator(classDeclaration: KSClassDeclaration): Boolean {
        // 如果类上有 @QBeanCreator 注解，则生成
        if (classDeclaration.hasAnnotation<QBeanCreator>()) {
            return true
        }
        
        // 如果任何属性上有 @QBeanCreator 注解，则生成
        return classDeclaration.getAllProperties().any { property ->
            property.hasAnnotation<QBeanCreator>()
        }
    }
    
    private fun shouldGenerateUpdater(classDeclaration: KSClassDeclaration): Boolean {
        // 如果类上有 @QBeanUpdater 注解，则生成
        if (classDeclaration.hasAnnotation<QBeanUpdater>()) {
            return true
        }
        
        // 如果任何属性上有 @QBeanUpdater 注解，则生成
        return classDeclaration.getAllProperties().any { property ->
            property.hasAnnotation<QBeanUpdater>()
        }
    }
}
package net.sunshow.toolkit.core.qbean.api.annotation.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class QBeanSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {


    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("net.sunshow.toolkit.core.qbean.api.annotation.QBean")
        val unresolvedSymbols = mutableListOf<KSAnnotated>()

        symbols.forEach { symbol ->
            if (symbol is KSClassDeclaration) {
                logger.info("Processing @QBean annotation", symbol)
            } else {
                logger.error("@QBean annotation can only be applied to classes", symbol)
                unresolvedSymbols.add(symbol)
            }
        }
        return unresolvedSymbols
    }
    
}

class QBeanSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return QBeanSymbolProcessor(
            environment.codeGenerator,
            environment.logger,
        )
    }
}
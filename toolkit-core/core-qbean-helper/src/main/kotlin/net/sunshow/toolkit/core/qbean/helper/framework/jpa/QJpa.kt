package net.sunshow.toolkit.core.qbean.helper.framework.jpa

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.Id
import net.sunshow.toolkit.core.qbean.helper.entity.BaseEntity
import java.util.concurrent.ConcurrentHashMap

object QJpa {

    private val logger = KotlinLogging.logger {}

    private val entityMetaCache = ConcurrentHashMap<Class<out BaseEntity>, QJpaEntityMeta>()

    private fun getOrInitEntityMeta(type: Class<out BaseEntity>): QJpaEntityMeta {
        return entityMetaCache
            .getOrPut(type) {
                QJpaEntityMeta(
                    idProperty = resolvePrimaryIdPropertyName(type)
                ).also {
                    logger.debug { "Entity 元数据缓存初始化完成, entity: $type, entityMeta: $it" }
                }
            }
    }

    private fun resolvePrimaryIdPropertyName(type: Class<*>): String? {
        var currentClass: Class<*>? = type
        while (currentClass != null) {
            val fields = currentClass.declaredFields
            for (field in fields) {
                if (field.isAnnotationPresent(Id::class.java)) {
                    return field.name
                }
            }
            currentClass = currentClass.superclass
        }

        return null
    }

    fun getIdProperty(type: Class<out BaseEntity>): String? {
        val entityMeta = getOrInitEntityMeta(type)

        return entityMeta.idProperty
    }
    
}
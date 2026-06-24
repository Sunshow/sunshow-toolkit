package net.sunshow.toolkit.core.qbean.helper.component.request

import io.github.oshai.kotlinlogging.KotlinLogging
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBean
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanUpdater
import net.sunshow.toolkit.core.qbean.helper.entity.BaseEntity
import net.sunshow.toolkit.core.qbean.helper.utils.TypeCompatibilityUtils
import org.apache.commons.beanutils.BeanUtils
import org.apache.commons.beanutils.PropertyUtils
import java.lang.reflect.Method

class QBeanUpdaterHelper private constructor() {

    companion object {

        private val logger = KotlinLogging.logger {}

        private val defaultIgnoredProperties = setOf("updateId", "updateProperties")
        private val defaultForceChangeProperties = setOf("updatedTime")

        // ========== copyUpdaterField ==========

        /**
         * 从 Updater 拷贝属性到 Entity。
         * 拷贝哪些属性由 [BaseQBeanUpdater.getUpdateProperties] 决定。
         */
        @JvmStatic
        fun <Q : BaseQBean, E : BaseEntity, S : BaseQBeanUpdater<Q>> copyUpdaterField(
            entity: E,
            updater: S
        ): E = copyUpdaterField(entity, updater, emptySet())

        /**
         * 从 Updater 拷贝属性到 Entity，支持忽略指定属性。
         *
         * @param ignoreProperties 忽略的属性名集合，这些属性即使出现在 updateProperties 中也不会被拷贝
         */
        @JvmStatic
        fun <Q : BaseQBean, E : BaseEntity, S : BaseQBeanUpdater<Q>> copyUpdaterField(
            entity: E,
            updater: S,
            ignoreProperties: Set<String>
        ): E {
            val updateProperties = updater.updateProperties ?: return entity
            for (fieldName in updateProperties) {
                if (fieldName in ignoreProperties) {
                    continue
                }
                try {
                    val fieldValue = PropertyUtils.getProperty(updater, fieldName)
                    BeanUtils.setProperty(entity, fieldName, fieldValue)
                } catch (e: Exception) {
                    logger.error(e) { "类属性拷贝错误, class=${updater.javaClass}, fieldName=$fieldName" }
                }
            }
            return entity
        }

        /**
         * 判断 Updater 中声明的字段是否会对 Entity 产生实际变更。
         *
         * 显式更新 updatedTime 始终视为有效变更，即使目标值与当前值相同；这是调用方主动要求刷新
         * 更新时间的语义，不应被 no-op 优化吞掉。
         */
        @JvmStatic
        fun <Q : BaseQBean, E : BaseEntity, S : BaseQBeanUpdater<Q>> hasActualChange(
            entity: E,
            updater: S,
            forceChangeProperties: Set<String> = defaultForceChangeProperties
        ): Boolean {
            val updateProperties = updater.updateProperties ?: return false
            for (fieldName in updateProperties) {
                if (fieldName in forceChangeProperties) {
                    return true
                }
                try {
                    if (PropertyUtils.getProperty(entity, fieldName) != PropertyUtils.getProperty(updater, fieldName)) {
                        return true
                    }
                } catch (e: Exception) {
                    logger.warn(e) { "类属性变更比较错误, class=${updater.javaClass}, fieldName=$fieldName，保守认为存在变更" }
                    return true
                }
            }
            return false
        }

        // ========== copyPropertiesToUpdateBuilder ==========

        /**
         * 从源对象反射拷贝属性到 Updater Builder。
         * 始终跳过 class、updateId、updateProperties 三个内置属性。
         */
        @JvmStatic
        fun <Q : BaseQBean, UpdateBuilder, Updater : BaseQBeanUpdater<Q>, PropertiesSource>
                copyPropertiesToUpdateBuilder(
            builder: UpdateBuilder,
            creatorType: Class<Updater>,
            source: PropertiesSource
        ) = copyPropertiesToUpdateBuilder(
            builder, creatorType, source,
            ignoreProperties = emptySet()
        )

        /**
         * 从源对象反射拷贝属性到 Updater Builder，支持额外忽略指定属性。
         * 始终跳过 class 属性。
         *
         * @param ignoreProperties 额外忽略的属性名集合（class 属性始终被忽略）
         */
        @JvmStatic
        fun <Q : BaseQBean, UpdateBuilder, Updater : BaseQBeanUpdater<Q>, PropertiesSource>
                copyPropertiesToUpdateBuilder(
            builder: UpdateBuilder,
            creatorType: Class<Updater>,
            source: PropertiesSource,
            ignoreProperties: Set<String>
        ) {
            val propertyDescriptors = PropertyUtils.getPropertyDescriptors(creatorType)
            val methodNameListMap = (builder as Any).javaClass.methods.groupBy { it.name }

            for (pd in propertyDescriptors) {
                val fieldName = pd.name

                if (fieldName == "class" || fieldName in ignoreProperties || fieldName in defaultIgnoredProperties) {
                    continue
                }

                try {
                    val fieldValue = PropertyUtils.getProperty(source, fieldName)
                        ?: continue

                    val methodName = "with" + fieldName.replaceFirstChar { it.uppercase() }
                    val methods = methodNameListMap[methodName] ?: continue

                    val method = methods.firstOrNull { m: Method ->
                        m.parameterCount == 1 && TypeCompatibilityUtils.isAssignableFrom(
                            m.parameterTypes[0], fieldValue::class.java
                        )
                    }
                    if (method == null) continue

                    method.invoke(builder, fieldValue)
                } catch (ignored: IllegalAccessException) {
                    // did nothing
                } catch (ignored: NoSuchMethodException) {
                    // did nothing
                } catch (e: Exception) {
                    logger.error(e) { "类属性拷贝错误, fieldName=$fieldName" }
                }
            }
        }
    }
}

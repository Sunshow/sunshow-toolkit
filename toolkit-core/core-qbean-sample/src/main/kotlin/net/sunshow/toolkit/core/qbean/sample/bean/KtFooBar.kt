package net.sunshow.toolkit.core.qbean.sample.bean

import net.sunshow.toolkit.core.qbean.api.annotation.*
import net.sunshow.toolkit.core.qbean.api.bean.AbstractQBean
import java.time.LocalDateTime

@QBean(defaultIdPropertyType = Int::class)
@QBeanCreator
@QBeanUpdater
data class KtFooBar(
    @QBeanID
    val id: Long,

    val foo: String,

    val bar: Int,

    val map: Map<String, Int>? = null,

    @QBeanCreatorIgnore
    val createdTime: LocalDateTime,

    @QBeanCreatorIgnore
    val updatedTime: LocalDateTime,
) : AbstractQBean()
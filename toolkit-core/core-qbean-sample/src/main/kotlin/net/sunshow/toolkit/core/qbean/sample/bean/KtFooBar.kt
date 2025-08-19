package net.sunshow.toolkit.core.qbean.sample.bean

import net.sunshow.toolkit.core.qbean.api.annotation.QBean
import net.sunshow.toolkit.core.qbean.api.annotation.QBeanCreator
import net.sunshow.toolkit.core.qbean.api.annotation.QBeanCreatorIgnore
import net.sunshow.toolkit.core.qbean.api.annotation.QBeanUpdater
import net.sunshow.toolkit.core.qbean.api.bean.AbstractQBean
import java.time.LocalDateTime

@QBean(defaultIdPropertyType = Int::class)
@QBeanCreator
@QBeanUpdater
data class KtFooBar(
    val foo: String,

    val bar: Int,

    @QBeanCreatorIgnore
    val createdTime: LocalDateTime,

    @QBeanCreatorIgnore
    val updatedTime: LocalDateTime,
) : AbstractQBean()
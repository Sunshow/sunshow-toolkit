package net.sunshow.toolkit.core.qbean.sample.bean

import net.sunshow.toolkit.core.qbean.helper.entity.BaseEntity
import java.time.LocalDateTime

/**
 * Entity class for testing QBeanHelper with Kotlin generated Creator/Updater
 */
//class KtFooBarEntity : BaseEntity {
//    var id: Int? = null
//    var foo: String? = null
//    var bar: Int? = null
//    var createdTime: LocalDateTime? = null
//    var updatedTime: LocalDateTime? = null
//
//    // Constructors for test convenience
//    constructor()
//
//    constructor(id: Int?, foo: String?, bar: Int?, createdTime: LocalDateTime?, updatedTime: LocalDateTime?) {
//        this.id = id
//        this.foo = foo
//        this.bar = bar
//        this.createdTime = createdTime
//        this.updatedTime = updatedTime
//    }
//}

data class KtFooBarEntity(
    var id: Int? = null,
    var foo: String? = null,
    var bar: Int? = null,
    var createdTime: LocalDateTime? = null,
    var updatedTime: LocalDateTime? = null,
) : BaseEntity
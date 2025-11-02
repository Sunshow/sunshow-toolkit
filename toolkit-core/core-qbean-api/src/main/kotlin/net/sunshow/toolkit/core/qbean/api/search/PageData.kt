package net.sunshow.toolkit.core.qbean.api.search

import java.util.function.Function
import kotlin.math.ceil

open class PageData<E> {
    /* 回写请求时的分页设置 */
    var page: Int = 0
        private set
    var pageSize: Int = 0
        private set

    /* 满足条件的记录总数 */
    var total: Long = 0
        private set

    var data: Collection<E> = emptyList()

    constructor(page: Int, pageSize: Int, data: Collection<E>, total: Long) {
        this.page = page
        this.pageSize = pageSize
        this.data = data
        this.total = total
    }

    constructor(page: Int, pageSize: Int, data: Collection<E>) : this(
        page,
        pageSize,
        data,
        data.size.toLong()
    )

    constructor(page: Int, pageSize: Int) : this(page, pageSize, emptyList())

    constructor()

    open fun <T> map(mapper: Function<E, T>): PageData<T> {
        val mapList = this.data
            .map {
                mapper.apply(it)
            }
        return PageData(this.page, this.pageSize, mapList, this.total)
    }

    open fun getCount(): Int {
        return data.size
    }

    open fun getPageTotal(): Int {
        if (total <= 0) {
            return 0
        }

        return ceil(total.toDouble() / pageSize).toInt()
    }
}

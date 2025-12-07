package net.sunshow.toolkit.core.qbean.api.service

import net.sunshow.toolkit.core.qbean.api.bean.BaseQBean
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanCreator
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanUpdater
import net.sunshow.toolkit.core.qbean.api.request.QPage
import net.sunshow.toolkit.core.qbean.api.request.QRequest
import net.sunshow.toolkit.core.qbean.api.request.QSort
import net.sunshow.toolkit.core.qbean.api.response.QResponse
import net.sunshow.toolkit.core.qbean.api.search.PageSearch
import java.io.Serializable
import java.util.*

interface BaseQService<Q : BaseQBean, ID : Serializable> {

    fun getQBeanClass(): Class<Q>

    fun getQBeanIdClass(): Class<ID>

    fun getById(id: ID): Optional<Q>

    fun getByIdOrNull(id: ID): Q?

    fun getByIdEnsure(id: ID): Q

    fun findByIdCollection(idCollection: Collection<ID>): List<Q>

    fun findOne(request: QRequest, sortList: List<QSort>? = null): Q?

    fun findTopLimit(
        request: QRequest,
        sortList: List<QSort>? = null,
        limit: Int
    ): List<Q>

    fun findAll(request: QRequest, requestPage: QPage): QResponse<Q>

    fun findAllTotal(
        request: QRequest,
        sortList: List<QSort>? = null,
        requestPageSize: Int? = null,
    ): List<Q>

    fun countAll(request: QRequest): Long

    fun search(search: PageSearch): QResponse<Q>

    fun searchTotal(search: PageSearch): List<Q>

    fun <T : BaseQBeanCreator<Q>> save(creator: T): Q

    fun saveAny(creator: Any): Q

    fun <T : BaseQBeanUpdater<Q>> update(updater: T): Q

    fun update(id: ID, updater: Any): Q

    fun deleteById(id: ID)

    /**
     * 在事务中锁定数据行 (悲观锁, FOR UPDATE, 记录不存在就报错)
     */
    fun <T> lockByIdInTransaction(id: ID, action: (Q) -> T): T

    fun <T> doInTransaction(action: () -> T): T

}
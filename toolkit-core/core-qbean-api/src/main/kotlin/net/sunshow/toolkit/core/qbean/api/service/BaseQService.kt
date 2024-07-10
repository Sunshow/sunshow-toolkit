package net.sunshow.toolkit.core.qbean.api.service

import net.sunshow.toolkit.core.qbean.api.bean.BaseQBean
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanCreator
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanUpdater
import net.sunshow.toolkit.core.qbean.api.request.QPage
import net.sunshow.toolkit.core.qbean.api.request.QRequest
import net.sunshow.toolkit.core.qbean.api.response.QResponse
import net.sunshow.toolkit.core.qbean.api.search.PageSearch
import java.io.Serializable
import java.util.*

interface BaseQService<Q : BaseQBean, ID : Serializable> {

    fun getById(id: ID): Optional<Q>

    fun getByIdOrNull(id: ID): Q?

    fun getByIdEnsure(id: ID): Q

    fun findByIdCollection(idCollection: Collection<ID>): List<Q>

    fun findAll(request: QRequest, requestPage: QPage): QResponse<Q>

    fun findAllTotal(request: QRequest): List<Q>

    fun countAll(request: QRequest): Long

    fun search(search: PageSearch): QResponse<Q>

    fun searchTotal(search: PageSearch): List<Q>

    fun save(creator: BaseQBeanCreator): Q

    fun saveAny(creator: Any): Q

    fun update(updater: BaseQBeanUpdater): Q

    fun update(id: ID, updater: Any): Q

    fun deleteById(id: ID)

}
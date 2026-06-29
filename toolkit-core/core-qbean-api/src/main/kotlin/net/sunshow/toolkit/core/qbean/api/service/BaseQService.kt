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

    /**
     * CAS (Compare-And-Swap) 更新：仅当实体指定字段与期待值一致时才执行更新。
     *
     * ## 与 [update] 的区别
     * - **无悲观锁**：通过一条原子 UPDATE 语句的 WHERE 子句校验 CAS 条件，不持有行锁。
     * - **无 entity hook**：不触发 [beforeSetUpdateProperties] / [afterSetUpdateProperties] / [afterPostUpdate]。
     *   子类如需在 CAS 中应用 hook，请 override 实现类的 `casUpdateInternal`。
     * - **保留 [afterCommitUpdate]**：事务提交后触发，可用于异步通知等场景。
     * - **遵守 [shouldSkipUnchangedUpdate]**：updateProperties 为空时仅做原子 predicate 校验，不写入数据库。
     *   非空 updateProperties 无论值是否变化都会执行原子 UPDATE。
     *
     * @param updater         更新器，包含 updateId 和 updateProperties
     * @param expectProperty  期待值与当前值一致的字段名
     * @param expectValue     期待值
     * @return true 表示 CAS 条件满足且更新成功（或 no-op 时 predicate 通过），false 表示条件不满足或记录不存在
     */
    fun <T : BaseQBeanUpdater<Q>> casUpdate(updater: T, expectProperty: String, expectValue: Any?): Boolean

    /**
     * CAS (Compare-And-Swap) 更新：仅当实体所有指定字段与期待值一致时才执行更新。
     * 参见 [casUpdate] 单条件版本的说明。
     *
     * @param updater          更新器
     * @param expectProperties 期待字段与值的映射，所有条件为 AND 关系
     * @return true 表示全部 CAS 条件满足且更新成功，false 表示任一条件不满足或记录不存在
     */
    fun <T : BaseQBeanUpdater<Q>> casUpdate(updater: T, expectProperties: Map<String, Any?>): Boolean

    fun update(id: ID, updater: Any): Q

    fun deleteById(id: ID)

    /**
     * 在事务中锁定数据行 (悲观锁, FOR UPDATE, 记录不存在就报错)
     */
    fun <T> lockByIdInTransaction(id: ID, action: (Q) -> T): T

    fun <T> doInTransaction(action: () -> T): T

}

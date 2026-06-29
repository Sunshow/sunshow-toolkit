package net.sunshow.toolkit.core.qbean.helper.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBean
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanCreator
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanUpdater
import net.sunshow.toolkit.core.qbean.api.request.QPage
import net.sunshow.toolkit.core.qbean.api.request.QRequest
import net.sunshow.toolkit.core.qbean.api.request.QSort
import net.sunshow.toolkit.core.qbean.api.response.QResponse
import net.sunshow.toolkit.core.qbean.api.search.PageSearch
import net.sunshow.toolkit.core.qbean.api.service.BaseQService
import net.sunshow.toolkit.core.qbean.helper.component.request.QBeanCreatorHelper
import net.sunshow.toolkit.core.qbean.helper.component.request.QBeanUpdaterHelper
import net.sunshow.toolkit.core.qbean.helper.component.request.QPageRequestHelper
import net.sunshow.toolkit.core.qbean.helper.entity.BaseEntity
import net.sunshow.toolkit.core.qbean.helper.framework.jpa.QJpa
import net.sunshow.toolkit.core.qbean.helper.repository.BaseRepository
import nxcloud.foundation.core.data.jpa.entity.DeletedField
import nxcloud.foundation.core.data.jpa.entity.UpdatedTimeField
import nxcloud.foundation.core.data.support.annotation.EnableSoftDelete
import org.apache.commons.beanutils.BeanUtils
import org.apache.commons.beanutils.PropertyUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.io.Serializable
import java.lang.reflect.ParameterizedType
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * 默认基础服务实现
 * Created by sunshow.
 */
abstract class DefaultQServiceImpl<Q : BaseQBean, ID : Serializable, ENTITY : BaseEntity, DAO : BaseRepository<ENTITY, ID>>
    : AbstractQServiceImpl<Q>(), BaseQService<Q, ID> {

    // 使用 kotlin-logging 提供的 logger，遮蔽父类的 SLF4J logger
    protected open val logger = KotlinLogging.logger {}

    protected open lateinit var applicationContext: ApplicationContext
        @Autowired set

    protected open lateinit var dao: DAO
        @Autowired set

    @Suppress("UNCHECKED_CAST")
    protected open val idClass: Class<ID>
        get() = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<ID>

    @Suppress("UNCHECKED_CAST")
    protected open val entityClass: Class<ENTITY>
        get() = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[2] as Class<ENTITY>

    protected open fun createNewEntityInstance(): ENTITY {
        val entityClass = entityClass
        try {
            val constructor = entityClass.getConstructor()
            return constructor.newInstance()
        } catch (e: Exception) {
            when (e) {
                is NoSuchMethodException -> {
                    throw RuntimeException("没有空构造方法", e)
                }

                else -> throw RuntimeException("通过空构造方法创建实例出错", e)
            }
        }
    }

    protected open fun getEntityWithNullCheckForUpdate(id: ID): ENTITY {
        val entity = dao.findByIdOrNull(id)
            ?: throw getExceptionSupplier(
                "未获取到 PO 记录, id=$id",
                null,
            ).get()

        dao.detach(entity)

        // 重新锁行获取
        return dao.findByIdForUpdate(id)
    }

    protected open fun getEntityWithNullCheckForUpdateReturningOriginal(id: ID): Pair<ENTITY, ENTITY> {
        val entity = dao.findByIdOrNull(id)
            ?: throw getExceptionSupplier(
                "未获取到 PO 记录, id=$id",
                null,
            ).get()

        dao.detach(entity)

        // 重新锁行获取 并同时返回原始对象
        return dao.findByIdForUpdate(id) to entity
    }

    override fun getQBeanClass(): Class<Q> {
        return actualType
    }

    override fun getQBeanIdClass(): Class<ID> {
        return idClass
    }

    override fun getById(id: ID): Optional<Q> {
        return dao.findById(id)
            .map {
                it.toPojo()
            }
    }

    override fun getByIdEnsure(id: ID): Q {
        return getById(id).orElseThrow(getExceptionSupplier("指定ID的数据不存在: $id", null))
    }

    override fun getByIdOrNull(id: ID): Q? {
        return dao.findByIdOrNull(id)
            ?.toPojo()
    }

    override fun findByIdCollection(idCollection: Collection<ID>): List<Q> {
        return dao.findAllByIdIn(idCollection)
            .map {
                it.toPojo()
            }
    }

    override fun findAll(request: QRequest, requestPage: QPage): QResponse<Q> {
        return convertQResponse(findAllInternal(request, requestPage))
    }

    protected open fun findAllInternal(request: QRequest, requestPage: QPage): Page<ENTITY> {
        return dao.findAll(convertSpecification(request), convertPageable(requestPage))
    }

    protected open fun findAllTotalInternal(request: QRequest): List<ENTITY> {
        return dao.findAll(convertSpecification(request))
    }

    protected open fun findAllTotalSortedInternal(request: QRequest, sortList: List<QSort>): List<ENTITY> {
        return dao.findAll(
            convertSpecification(request),
            Sort.by(
                sortList
                    .map {
                        convertSort(it)
                    }
            )
        )
    }

    protected open fun findAllInternal(request: QRequest): Long {
        return dao.count(convertSpecification(request))
    }

    override fun countAll(request: QRequest): Long {
        return findAllInternal(request)
    }

    override fun findAllTotal(
        request: QRequest,
        sortList: List<QSort>?,
        requestPageSize: Int?
    ): List<Q> {
        if (requestPageSize == null) {
            // 不分页直接查所有
            return findAllTotalSortedInternal(request, sortList ?: emptyList())
                .map {
                    it.toPojo()
                }
        } else {
            return QPageRequestHelper.request(
                request,
                QPage.newInstance()
                    .pagingWithSize(requestPageSize)
                    .apply {
                        sortList
                            ?.onEach {
                                addOrder(it.field, it.order)
                            }
                    },
                this::findAll,
            )
        }
    }

    override fun findOne(
        request: QRequest,
        sortList: List<QSort>?
    ): Q? {
        return findOneInternal(request, sortList)
            ?.toPojo()
    }

    override fun findTopLimit(
        request: QRequest,
        sortList: List<QSort>?,
        limit: Int
    ): List<Q> {
        return findTopLimitInternal(request, sortList, limit)
            .map {
                it.toPojo()
            }
    }

    protected open fun findOneInternal(request: QRequest, sortList: List<QSort>? = null): ENTITY? {
        return findTopLimitInternal(
            request = request,
            sortList = sortList,
            limit = 1,
        ).firstOrNull()
    }

    protected open fun findTopLimitInternal(
        request: QRequest,
        sortList: List<QSort>? = null,
        limit: Int
    ): List<ENTITY> {
        return findAllInternal(
            request = request,
            requestPage = QPage.newInstance()
                .pagingWithSize(limit)
                .apply {
                    sortList
                        ?.onEach {
                            addOrder(it.field, it.order)
                        }
                },
        ).content
    }


    override fun searchTotal(search: PageSearch): List<Q> {
        return findAllTotal(search.toQRequest())
    }

    override fun search(search: PageSearch): QResponse<Q> {
        return findAll(search.toQRequest(), search.toQPage())
    }

    @Transactional
    override fun <T : BaseQBeanCreator<Q>> save(creator: T): Q {
        return convertQBean(saveInternal(creator))
    }

    @Transactional
    override fun saveAny(creator: Any): Q {
        return convertQBean(saveAnyInternal(creator))
    }

    protected open fun <T : BaseQBeanCreator<Q>> saveInternal(creator: T): ENTITY {
        val po = createNewEntityInstance()

        beforeSetSaveProperties(po, creator)

        QBeanCreatorHelper.copyCreatorField(po, creator)

        return saveInternal(po, creator)
    }

    protected open fun saveAnyInternal(creator: Any): ENTITY {
        val po = createNewEntityInstance()

        beforeSetSaveProperties(po, creator)

        // 作为通用对象处理复制 private 属性
        copyProperties(creator, po)

        return saveInternal(po, creator)
    }

    protected open fun saveInternal(entity: ENTITY, creator: Any): ENTITY {
        afterSetSaveProperties(entity, creator)

        val savedPO = dao.saveAndFlush(entity)

        // 重新加载解决 DynamicInsert 问题
        dao.refresh(savedPO)

        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {

            override fun afterCommit() {
                afterCommitSave(entity, creator)
            }

        })

        return afterPostSave(savedPO, creator)
    }

    protected open fun beforeSetSaveProperties(po: ENTITY, creator: Any) {
        // 默认不处理
    }

    protected open fun afterSetSaveProperties(po: ENTITY, creator: Any) {
        // 默认不处理
    }

    protected open fun afterPostSave(po: ENTITY, creator: Any): ENTITY {
        // 默认不处理
        return po
    }

    protected open fun beforeSetUpdateProperties(po: ENTITY, original: ENTITY, updater: Any) {
        // 默认不处理
    }

    protected open fun afterSetUpdateProperties(po: ENTITY, original: ENTITY, updater: Any) {
        // 默认不处理
    }

    protected open fun afterPostUpdate(po: ENTITY, original: ENTITY, updater: Any): ENTITY {
        // 默认不处理
        return po
    }

    // 注意 事务提交后的回调中 PO 对象已经是 detached 状态, 不能再进行任何操作

    /**
     * 新增保存事务提交后的处理
     */
    protected open fun afterCommitSave(po: ENTITY, creator: Any) {
        // 默认不处理
    }

    /**
     * 更新事务提交后的处理
     */
    protected open fun afterCommitUpdate(po: ENTITY, original: ENTITY, updater: Any) {
        // 默认不处理
    }

    /**
     * 删除事务提交后的处理
     */
    protected open fun afterCommitDelete(po: ENTITY) {
        // 默认不处理
    }

    @Transactional
    override fun <T : BaseQBeanUpdater<Q>> update(updater: T): Q {
        return convertQBean(updateInternal(updater))
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun <T : BaseQBeanUpdater<Q>> updateInternal(updater: T): ENTITY {
        val id = updater.updateId as ID
        val skipUnchangedUpdate = shouldSkipUnchangedUpdate(updater)
        if (skipUnchangedUpdate && updater.updateProperties.isNullOrEmpty()) {
            return dao.findByIdOrNull(id)
                ?: throw getExceptionSupplier(
                    "未获取到 PO 记录, id=$id",
                    null,
                ).get()
        }

        val (po, original) = getEntityWithNullCheckForUpdateReturningOriginal(id)

        if (skipUnchangedUpdate
            && !QBeanUpdaterHelper.hasActualChange(po, updater, forceChangePropertiesForUpdate(updater))
        ) {
            return po
        }

        beforeSetUpdateProperties(po, original, updater)

        QBeanUpdaterHelper.copyUpdaterField(po, updater)

        return updateInternal(po, original, updater)
    }

    /**
     * 是否跳过没有实际字段变更的 QBean Updater。
     *
     * 默认开启，避免空 Updater 或同值 Updater 仅刷新 updatedTime，从而制造无意义写入。
     * 如业务依赖旧行为（例如必须触发 afterCommitUpdate），可在具体 Service 中 override 返回 false。
     */
    protected open fun <T : BaseQBeanUpdater<Q>> shouldSkipUnchangedUpdate(updater: T): Boolean {
        return true
    }

    /**
     * 即使字段值与当前 Entity 相同，也应视为有效变更的字段。
     *
     * updatedTime 是调用方显式刷新更新时间的语义，不参与 no-op 跳过。
     */
    protected open fun <T : BaseQBeanUpdater<Q>> forceChangePropertiesForUpdate(updater: T): Set<String> {
        return FORCE_CHANGE_PROPERTIES_FOR_UPDATE
    }

    /**
     * CAS 单条件更新。参见 [BaseQService.casUpdate]。
     */
    @Transactional
    override fun <T : BaseQBeanUpdater<Q>> casUpdate(
        updater: T,
        expectProperty: String,
        expectValue: Any?
    ): Boolean {
        return casUpdate(updater, mapOf(expectProperty to expectValue))
    }

    /**
     * CAS 多条件更新。参见 [BaseQService.casUpdate]。
     */
    @Transactional
    override fun <T : BaseQBeanUpdater<Q>> casUpdate(
        updater: T,
        expectProperties: Map<String, Any?>
    ): Boolean {
        return casUpdateInternal(updater, expectProperties)
    }

    /**
     * CAS 更新的内部实现。
     *
     * ## 路径
     * 1. **no-op**：[shouldSkipUnchangedUpdate] 为 true 且 [BaseQBeanUpdater.getUpdateProperties] 为空时，
     *    通过 [dao.existsByConditions] 做原子 predicate 校验后返回，不写入数据库，不触发任何 hook。
     * 2. **实际更新**：从 updater 提取 SET 子句，通过 [dao.casUpdate] 执行一条原子 UPDATE，
     *    成功后加载更新后的实体并注册 [afterCommitUpdate] 回调。
     *
     * ## 不支持的 hook
     * CAS 路径 **不会** 调用以下扩展点（与 [update] 路径不同）：
     * - [beforeSetUpdateProperties]
     * - [afterSetUpdateProperties]
     * - [afterPostUpdate]
     *
     * 子类如需在 CAS 中应用这些 hook，请 override 本方法。通常的做法是：对于需要 hook 的场景继续走 [update]，
     * 对于纯字段赋值场景使用 CAS。
     */

    @Suppress("UNCHECKED_CAST")
    protected open fun <T : BaseQBeanUpdater<Q>> casUpdateInternal(
        updater: T,
        expectProperties: Map<String, Any?>
    ): Boolean {
        require(expectProperties.isNotEmpty()) {
            "CAS 更新必须提供至少一个条件字段"
        }

        val id = updater.updateId as ID
        val idProperty = QJpa.getIdProperty(entityClass)
            ?: throw IllegalStateException("无法获取实体主键属性名: ${entityClass.name}")

        // 构建 WHERE 条件：主键 + CAS 预期值
        val whereProperties = mutableMapOf<String, Any?>()
        whereProperties.putAll(expectProperties)
        whereProperties[idProperty] = id

        val skipUnchangedUpdate = shouldSkipUnchangedUpdate(updater)

        // no-op：updateProperties 为空，原子 predicate 校验后直接返回
        if (skipUnchangedUpdate && updater.updateProperties.isNullOrEmpty()) {
            return dao.existsByConditions(whereProperties)
        }

        val original = dao.findByIdOrNull(id) ?: return false
        dao.detach(original)

        // 有实际变更：从 updater 提取 SET 子句，执行原子 CAS 更新
        val setProperties = extractCasSetProperties(updater)
        if (UpdatedTimeField::class.java.isAssignableFrom(entityClass)) {
            setProperties["updatedTime"] = LocalDateTime.now()
        }
        // 防御：如果 SET 仍为空（shouldSkipUnchangedUpdate=false 且无 UpdatedTimeField），
        // 取主键自赋值保证 UPDATE 语法合法
        if (setProperties.isEmpty()) {
            setProperties[idProperty] = id
        }

        // 执行原子 CAS 更新
        val affectedRows = dao.casUpdate(setProperties, whereProperties)
        if (affectedRows == 0) {
            return false
        }

        // 加载更新后的实体，注册事务提交回调
        val updatedEntity = dao.findByIdOrNull(id)
        if (updatedEntity != null) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    afterCommitUpdate(updatedEntity, original, updater)
                }
            })
        }

        return true
    }

    /**
     * 从 Updater 的 [BaseQBeanUpdater.getUpdateProperties] 中提取要 SET 的字段和值。
     *
     * 注意：CAS 路径不支持 beforeSetUpdateProperties / afterSetUpdateProperties / afterPostUpdate hook，
     * 子类如需在 CAS 中应用 hook，请 override [casUpdateInternal]。
     */
    protected open fun <T : BaseQBeanUpdater<Q>> extractCasSetProperties(updater: T): MutableMap<String, Any?> {
        val setProperties = mutableMapOf<String, Any?>()
        val updateProperties = updater.updateProperties ?: return setProperties
        for (fieldName in updateProperties) {
            try {
                val fieldValue = PropertyUtils.getProperty(updater, fieldName)
                setProperties[fieldName] = fieldValue
            } catch (e: Exception) {
                logger.error(e) { "CAS 提取 Updater 属性出错, fieldName=$fieldName" }
            }
        }
        return setProperties
    }

    protected open fun updateAnyInternal(id: ID, updater: Any): ENTITY {
        val (po, original) = getEntityWithNullCheckForUpdateReturningOriginal(id)

        beforeSetUpdateProperties(po, original, updater)

        // 作为通用对象处理复制 private 属性
        // 忽略主键更新
        copyProperties(updater, po) {
            it != QJpa.getIdProperty(entityClass)
        }

        return updateInternal(po, original, updater)
    }

    protected open fun updateInternal(entity: ENTITY, original: ENTITY, updater: Any): ENTITY {
        if (UpdatedTimeField::class.java.isAssignableFrom(entityClass)) {
            (entity as UpdatedTimeField).updatedTime = LocalDateTime.now()
            logger.debug {
                "实现了 UpdatedTimeField, 自动维护更新时间"
            }
        }

        afterSetUpdateProperties(entity, original, updater)

        dao.flush()

        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {

            override fun afterCommit() {
                afterCommitUpdate(entity, original, updater)
            }

        })

        return afterPostUpdate(entity, original, updater)
    }

    @Transactional
    override fun update(id: ID, updater: Any): Q {
        return convertQBean(updateAnyInternal(id, updater))
    }

    @Transactional
    override fun deleteById(id: ID) {
        deleteInternal(id)
    }

    protected open fun deleteInternal(id: ID): ENTITY {
        val po = getEntityWithNullCheckForUpdate(id)
        deleteEntityInternal(po)
        return po
    }

    protected open fun deleteEntityInternal(entity: ENTITY) {
        deleteAllInternal(listOf(entity))
    }

    @Transactional
    override fun <T> lockByIdInTransaction(id: ID, action: (Q) -> T): T {
        val entity = getEntityWithNullCheckForUpdate(id)
        return action(entity.toPojo())
    }

    @Transactional
    override fun <T> doInTransaction(action: () -> T): T {
        return action()
    }

    protected open fun shouldSoftDelete(): Boolean {
        return AnnotatedElementUtils.hasAnnotation(this.javaClass, EnableSoftDelete::class.java)
    }

    protected fun deleteAllInternal(entityList: List<ENTITY>) {
        if (entityList.isEmpty()) {
            return
        }

        if (shouldSoftDelete()) {
            if (DeletedField::class.java.isAssignableFrom(entityClass)) {
                val now = LocalDateTime.now()
                val timestamp = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

                for (po in entityList) {
                    (po as DeletedField).deleted = timestamp
                }

                logger.debug {
                    "实现了 DeletedField, 自动维护软删除的删除时间标记"
                }
                if (UpdatedTimeField::class.java.isAssignableFrom(entityClass)) {
                    for (po in entityList) {
                        (po as UpdatedTimeField).updatedTime = now
                    }
                    logger.debug("实现了 UpdatedTimeField, 自动维护更新时间")
                }

                dao.flush()
            } else {
                logger.error {
                    "配置启用了软删除但未实现软删除字段接口, 需要自行实现, 不做任何处理"
                }
            }
        } else {
            dao.deleteAll(entityList)
            dao.flush()
        }

        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {

            override fun afterCommit() {
                entityList
                    .forEach {
                        afterCommitDelete(it)
                    }
            }

        })
    }

    protected open fun copyProperties(source: Any, dest: Any, filter: (String) -> Boolean = { true }) {
        val propertyDescriptors = PropertyUtils.getPropertyDescriptors(source.javaClass)

        for (pd in propertyDescriptors) {
            val propertyName = pd.name

            // 跳过 class 属性
            if (propertyName == "class") {
                continue
            }

            if (!filter(propertyName)) {
                logger.debug {
                    "忽略复制属性: $propertyName"
                }
                continue
            }

            try {
                val value = PropertyUtils.getProperty(source, propertyName)
                    ?: // 默认不处理 null 值
                    continue

                BeanUtils.setProperty(dest, propertyName, value)
            } catch (e: Exception) {
                // throw RuntimeException("解析并设置 PO 属性出错", e)
                logger.error(e) {
                    "解析并设置 PO 属性出错, propertyName=$propertyName"
                }
            }
        }
    }

    protected open fun (ENTITY).toPojo(): Q {
        return convertQBean(this)
    }

    companion object {
        private val FORCE_CHANGE_PROPERTIES_FOR_UPDATE = setOf("updatedTime")
    }

}

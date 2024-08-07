package net.sunshow.toolkit.core.qbean.helper.service.impl

import net.sunshow.toolkit.core.qbean.api.bean.BaseQBean
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanCreator
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanUpdater
import net.sunshow.toolkit.core.qbean.api.request.QPage
import net.sunshow.toolkit.core.qbean.api.request.QRequest
import net.sunshow.toolkit.core.qbean.api.response.QResponse
import net.sunshow.toolkit.core.qbean.api.search.PageSearch
import net.sunshow.toolkit.core.qbean.api.service.BaseQService
import net.sunshow.toolkit.core.qbean.helper.component.request.QBeanCreatorHelper
import net.sunshow.toolkit.core.qbean.helper.component.request.QBeanUpdaterHelper
import net.sunshow.toolkit.core.qbean.helper.entity.BaseEntity
import net.sunshow.toolkit.core.qbean.helper.framework.jpa.QJpa
import net.sunshow.toolkit.core.qbean.helper.repository.BaseRepository
import nxcloud.foundation.core.data.jpa.entity.DeletedField
import nxcloud.foundation.core.data.support.annotation.EnableSoftDelete
import org.apache.commons.beanutils.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.io.Serializable
import java.lang.reflect.ParameterizedType
import java.util.*

/**
 * 默认基础服务实现
 * Created by sunshow.
 */
abstract class DefaultQServiceImpl<Q : BaseQBean, ID : Serializable, ENTITY : BaseEntity, DAO : BaseRepository<ENTITY, ID>>
    : AbstractQServiceImpl<Q>(), BaseQService<Q, ID> {

    @Autowired
    protected open lateinit var applicationContext: ApplicationContext

    @Autowired
    protected open lateinit var dao: DAO

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

    override fun getById(id: ID): Optional<Q> {
        return dao.findById(id)
            .map {
                it.toPojo()
            }
    }

    override fun getByIdEnsure(id: ID): Q {
        return getById(id).orElseThrow(getExceptionSupplier("指定ID的数据不存在", null))
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

    protected open fun findAllInternal(request: QRequest): Long {
        return dao.count(convertSpecification(request))
    }

    override fun countAll(request: QRequest): Long {
        return findAllInternal(request)
    }

    override fun findAllTotal(request: QRequest): List<Q> {
        return convertQBeanToList(findAllTotalInternal(request))
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

        beforeSetSaveProperties(po)

        QBeanCreatorHelper.copyCreatorField(po, creator)

        afterSetSaveProperties(po)

        dao.save(po)

        dao.flush()

        // 重新加载解决 DynamicInsert 问题
        dao.refresh(po)

        return afterPostSave(po)
    }

    protected open fun saveAnyInternal(creator: Any): ENTITY {
        val po = createNewEntityInstance()

        beforeSetSaveProperties(po)

        // 作为通用对象处理复制 private 属性
        copyProperties(creator, po)

        afterSetSaveProperties(po)

        dao.save(po)

        dao.flush()

        // 重新加载解决 DynamicInsert 问题
        dao.refresh(po)

        return afterPostSave(po)
    }

    protected open fun beforeSetSaveProperties(po: ENTITY) {
        // 默认不处理
    }

    protected open fun afterSetSaveProperties(po: ENTITY) {
        // 默认不处理
    }

    protected open fun afterPostSave(po: ENTITY): ENTITY {
        // 默认不处理
        return po
    }

    protected open fun beforeSetUpdateProperties(po: ENTITY, original: ENTITY) {
        // 默认不处理
    }

    protected open fun afterSetUpdateProperties(po: ENTITY, original: ENTITY) {
        // 默认不处理
    }

    protected open fun afterPostUpdate(po: ENTITY, original: ENTITY): ENTITY {
        // 默认不处理
        return po
    }

    @Transactional
    override fun <T : BaseQBeanUpdater<Q>> update(updater: T): Q {
        return convertQBean(updateInternal(updater))
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun <T : BaseQBeanUpdater<Q>> updateInternal(updater: T): ENTITY {
        val (po, original) = getEntityWithNullCheckForUpdateReturningOriginal(updater.updateId as ID)

        beforeSetUpdateProperties(po, original)

        QBeanUpdaterHelper.copyUpdaterField(po, updater)

        afterSetUpdateProperties(po, original)

        dao.flush()

        return afterPostUpdate(po, original)
//        if (updater is BaseQBeanUpdater) {
//            val po = getEntityWithNullCheckForUpdate(updater.updateId as ID)
//            QBeanUpdaterHelper.copyUpdaterField(po, updater)
//            return po
//            //throw new RuntimeException("使用 BaseQBeanUpdater 更新时只支持 Long 主键");
//        } else {
//            // 获取更新 ID
//            try {
//                val fieldValue = PropertyUtils.getProperty(updater, "id")
//                    ?: throw RuntimeException("更新对象中没有 id 属性")
//                if (fieldValue.javaClass.isAssignableFrom(idClass)) {
//                    return updateInternal(fieldValue as ID, updater)
//                } else {
//                    throw RuntimeException(
//                        "更新对象中 id 属性类型不匹配, 需要 ${idClass.name}, 实际 ${fieldValue.javaClass.name}",
//                    )
//                }
//            } catch (e: Exception) {
//                throw RuntimeException("获取更新 ID 出错", e)
//            }
//        }
    }

    protected open fun updateAnyInternal(id: ID, updater: Any): ENTITY {
        val (po, original) = getEntityWithNullCheckForUpdateReturningOriginal(id)

        beforeSetUpdateProperties(po, original)

        // 作为通用对象处理复制 private 属性
        // 忽略主键更新
        copyProperties(updater, po) {
            it != QJpa.getIdProperty(entityClass)
        }

        afterSetUpdateProperties(po, original)

        dao.flush()

        return afterPostUpdate(po, original)
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
        if (shouldSoftDelete()) {
            if (DeletedField::class.java.isAssignableFrom(entityClass)) {
                (entity as DeletedField).deleted = System.currentTimeMillis()
            } else {
                logger.error("配置启用了软删除但未实现软删除字段接口, 需要自行实现, 不做任何处理")
            }
        } else {
            dao.delete(entity)
            dao.flush()
        }
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
                for (po in entityList) {
                    (po as DeletedField).deleted = System.currentTimeMillis()
                }
            } else {
                logger.error("配置启用了软删除但未实现软删除字段接口, 需要自行实现, 不做任何处理")
            }
        } else {
            dao.deleteAll(entityList)
            dao.flush()
        }
    }

    protected open fun copyProperties(source: Any, dest: Any, filter: (String) -> Boolean = { true }) {
        val fields = source.javaClass.declaredFields

        for (field in fields) {
            field.isAccessible = true

            val fieldName = field.name

            if (!filter(fieldName)) {
                logger.info("忽略复制属性: $fieldName")
                continue
            }

            try {
                val value = field[source]
                    ?: // 默认不处理 null 值
                    continue

                BeanUtils.setProperty(dest, fieldName, value)
            } catch (e: Exception) {
                throw RuntimeException("解析并设置 PO 属性出错", e)
            }
        }
    }

    protected open fun (ENTITY).toPojo(): Q {
        return convertQBean(this)
    }

}

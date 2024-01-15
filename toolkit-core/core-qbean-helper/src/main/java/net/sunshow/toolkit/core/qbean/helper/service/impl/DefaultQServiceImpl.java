package net.sunshow.toolkit.core.qbean.helper.service.impl;

import net.sunshow.toolkit.core.qbean.api.bean.BaseQBean;
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanCreator;
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanUpdater;
import net.sunshow.toolkit.core.qbean.api.request.QPage;
import net.sunshow.toolkit.core.qbean.api.request.QRequest;
import net.sunshow.toolkit.core.qbean.api.response.QResponse;
import net.sunshow.toolkit.core.qbean.api.search.PageSearch;
import net.sunshow.toolkit.core.qbean.api.service.BaseQService;
import net.sunshow.toolkit.core.qbean.helper.component.request.QBeanCreatorHelper;
import net.sunshow.toolkit.core.qbean.helper.component.request.QBeanUpdaterHelper;
import net.sunshow.toolkit.core.qbean.helper.entity.BaseEntity;
import net.sunshow.toolkit.core.qbean.helper.repository.BaseRepository;
import nxcloud.foundation.core.data.jpa.entity.DeletedField;
import nxcloud.foundation.core.data.support.annotation.EnableSoftDelete;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 默认基础服务实现
 * Created by sunshow.
 */
public abstract class DefaultQServiceImpl<Q extends BaseQBean, ID extends Serializable, ENTITY extends BaseEntity, DAO extends BaseRepository<ENTITY, ID>>
        extends AbstractQServiceImpl<Q> implements BaseQService<Q, ID> {

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected DAO dao;

    @SuppressWarnings("unchecked")
    protected Class<ENTITY> getIdClass() {
        return (Class<ENTITY>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    }

    @SuppressWarnings("unchecked")
    protected Class<ENTITY> getEntityClass() {
        return (Class<ENTITY>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2];
    }

    protected ENTITY createNewEntityInstance() {
        Class<ENTITY> entityClass = getEntityClass();
        try {
            Constructor<ENTITY> constructor = entityClass.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("没有空构造方法", e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("通过空构造方法创建实例出错", e);
        }
    }

    protected ENTITY getEntityWithNullCheckForUpdate(ID id) {
        ENTITY entity = dao.findById(id).orElseThrow(getExceptionSupplier(String.format("未获取到 PO 记录, id=%s", id), null));
        dao.detach(entity);
        // 重新锁行获取
        return dao.findByIdForUpdate(id);
    }

    @Override
    public Optional<Q> getById(ID id) {
        return dao.findById(id).map(this::convertQBean);
    }

    @Override
    public Q getByIdEnsure(ID id) {
        return getById(id).orElseThrow(getExceptionSupplier("指定ID的数据不存在", null));
    }

    @Override
    public List<Q> findByIdCollection(Collection<ID> ids) {
        return dao.findAllByIdIn(ids).stream().map(this::convertQBean).collect(Collectors.toList());
    }

    @Override
    public QResponse<Q> findAll(QRequest request, QPage requestPage) {
        return convertQResponse(findAllInternal(request, requestPage));
    }

    protected Page<ENTITY> findAllInternal(QRequest request, QPage requestPage) {
        return dao.findAll(convertSpecification(request), convertPageable(requestPage));
    }

    protected List<ENTITY> findAllTotalInternal(QRequest request) {
        return dao.findAll(convertSpecification(request));
    }

    @Override
    public List<Q> findAllTotal(QRequest request) {
        return convertQBeanToList(findAllTotalInternal(request));
    }

    @Override
    public List<Q> searchTotal(PageSearch search) {
        return findAllTotal(search.toQRequest());
    }

    @Override
    public QResponse<Q> search(PageSearch search) {
        return findAll(search.toQRequest(), search.toQPage());
    }

    @Override
    @Transactional
    public Q save(Object creator) {
        return convertQBean(saveInternal(creator));
    }

    protected ENTITY saveInternal(Object creator) {
        ENTITY po = createNewEntityInstance();

        if (creator instanceof BaseQBeanCreator) {
            QBeanCreatorHelper.copyCreatorField(po, (BaseQBeanCreator) creator);
        } else {
            // 作为通用对象处理复制 private 属性
            copyProperties(creator, po);
        }

        return dao.save(po);
    }

    @Override
    @Transactional
    public Q update(Object updater) {
        return convertQBean(updateInternal(updater));
    }

    @SuppressWarnings("unchecked")
    protected ENTITY updateInternal(Object updater) {
        if (updater instanceof BaseQBeanUpdater) {
            BaseQBeanUpdater baseQBeanUpdater = (BaseQBeanUpdater) updater;
            if (Long.class.isAssignableFrom(getIdClass())) {
                ENTITY po = getEntityWithNullCheckForUpdate((ID) baseQBeanUpdater.getUpdateId());
                QBeanUpdaterHelper.copyUpdaterField(po, baseQBeanUpdater);
                return po;
            } else {
                throw new RuntimeException("使用 BaseQBeanUpdater 更新时只支持 Long 主键");
            }
        } else {
            // 获取更新 ID
            try {
                Object fieldValue = PropertyUtils.getProperty(updater, "id");
                if (fieldValue == null) {
                    throw new RuntimeException("更新对象中没有 id 属性");
                }
                if (fieldValue.getClass().isAssignableFrom(getIdClass())) {
                    return updateInternal((ID) fieldValue, updater);
                } else {
                    throw new RuntimeException(String.format("更新对象中 id 属性类型不匹配, 需要 %s, 实际 %s", getIdClass().getName(), fieldValue.getClass().getName()));
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException("获取更新 ID 出错", e);
            }
        }
    }

    protected ENTITY updateInternal(ID id, Object updater) {
        ENTITY po = getEntityWithNullCheckForUpdate(id);

        // 作为通用对象处理复制 private 属性
        copyProperties(updater, po);

        return po;
    }

    @Override
    @Transactional
    public Q update(ID id, Object updater) {
        return convertQBean(updateInternal(id, updater));
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        deleteInternal(id);
    }

    protected ENTITY deleteInternal(ID id) {
        ENTITY po = getEntityWithNullCheckForUpdate(id);
        if (AnnotatedElementUtils.hasAnnotation(this.getClass(), EnableSoftDelete.class)) {
            if (DeletedField.class.isAssignableFrom(getEntityClass())) {
                ((DeletedField) po).setDeleted(System.currentTimeMillis());
            } else {
                logger.error("配置启用了软删除但未实现软删除字段接口, 需要自行实现, 不做任何处理");
            }
        } else {
            dao.delete(po);
        }
        return po;
    }

    protected void copyProperties(Object source, Object dest) {
        Field[] fields = source.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            String fieldName = field.getName();

            try {
                Object value = field.get(source);
                if (value == null) {
                    // 默认不处理 null 值
                    continue;
                }

                BeanUtils.setProperty(dest, fieldName, value);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("解析并设置 PO 属性出错", e);
            }
        }
    }

}

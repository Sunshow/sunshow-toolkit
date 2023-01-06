package net.sunshow.toolkit.core.qbean.helper.service.impl;

import net.sunshow.toolkit.core.qbean.api.bean.BaseQBean;
import net.sunshow.toolkit.core.qbean.api.service.BaseQService;
import net.sunshow.toolkit.core.qbean.helper.entity.BaseEntity;
import net.sunshow.toolkit.core.qbean.helper.repository.BaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;

/**
 * 默认基础服务实现
 * Created by sunshow.
 */
public abstract class DefaultQServiceImpl<Q extends BaseQBean, ID extends Serializable, ENTITY extends BaseEntity, DAO extends BaseRepository<ENTITY, ID>>
        extends AbstractQServiceImpl<Q> implements BaseQService<Q, ID> {

    @Autowired
    protected ApplicationContext applicationContext;

    protected final DAO dao;

    protected DefaultQServiceImpl(DAO dao) {
        this.dao = dao;
    }

    protected DefaultQServiceImpl() {
        this.dao = applicationContext.getBean(getDaoClass());
    }

    @Override
    public Optional<Q> getById(ID id) {
        return dao.findById(id).map(this::convertQBean);
    }

    @Override
    public Q getByIdEnsure(ID id) {
        return getById(id).orElseThrow(getExceptionSupplier("指定ID的数据不存在", null));
    }

    @SuppressWarnings("unchecked")
    protected Class<DAO> getDaoClass() {
        return (Class<DAO>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[3];
    }
}

package net.sunshow.toolkit.core.qbean.helper.service.impl;

import net.sunshow.toolkit.core.qbean.api.bean.BaseQBean;
import net.sunshow.toolkit.core.qbean.api.request.QPage;
import net.sunshow.toolkit.core.qbean.api.request.QRequest;
import net.sunshow.toolkit.core.qbean.api.response.QResponse;
import net.sunshow.toolkit.core.qbean.api.service.BaseQService;
import net.sunshow.toolkit.core.qbean.helper.entity.BaseEntity;
import net.sunshow.toolkit.core.qbean.helper.repository.BaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;

import java.io.Serializable;
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

    private Page<ENTITY> findAllInternal(QRequest request, QPage requestPage) {
        return dao.findAll(convertSpecification(request), convertPageable(requestPage));
    }
    
}

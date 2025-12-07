package net.sunshow.toolkit.core.qbean.helper.service.impl;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Selection;
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBean;
import net.sunshow.toolkit.core.qbean.api.request.QFilter;
import net.sunshow.toolkit.core.qbean.api.request.QPage;
import net.sunshow.toolkit.core.qbean.api.request.QRequest;
import net.sunshow.toolkit.core.qbean.api.request.QSort;
import net.sunshow.toolkit.core.qbean.api.response.QResponse;
import net.sunshow.toolkit.core.qbean.helper.bean.jpa.QPageRequest;
import net.sunshow.toolkit.core.qbean.helper.entity.BaseEntity;
import net.sunshow.toolkit.core.qbean.helper.framework.jpa.QFilterKt;
import net.sunshow.toolkit.core.qbean.helper.repository.BaseRepository;
import nxcloud.foundation.core.bean.mapper.BeanMapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 预留基础服务实现
 * Created by sunshow.
 */
public abstract class AbstractQServiceImpl<Q extends BaseQBean> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected PlatformTransactionManager transactionManager;

    @Autowired
    protected BeanMapperFacade beanMapperFacade;

    @SuppressWarnings("unchecked")
    protected Class<Q> getActualType() {
        ParameterizedType paramType = (ParameterizedType) this.getClass().getGenericSuperclass();

        return (Class<Q>) paramType.getActualTypeArguments()[0];
    }

    protected Supplier<? extends RuntimeException> getExceptionSupplier(String message, Throwable cause) {
        return () -> new RuntimeException(message, cause);
    }

    protected <Entity extends BaseEntity, ID extends Serializable, Rep extends BaseRepository<Entity, ID>> Entity getEntityWithNullCheckForUpdate(ID id, Rep repository) {
        Entity entity = repository.findById(id).orElseThrow(getExceptionSupplier(String.format("未获取到Entity记录, id=%s", id), null));
        repository.detach(entity);
        // 重新锁行获取
        return repository.findByIdForUpdate(id);
    }

    protected <Entity extends BaseEntity, ID extends Serializable, Rep extends BaseRepository<Entity, ID>> Entity findOrCreate(Rep repository, Supplier<Entity> findUnique, Supplier<Entity> creator) {
        // 1) look for the record
        Entity found = findUnique.get();
        if (found != null) {
            return found;
        }
        // 2) if not found, start a new, independent transaction
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        found = tt.execute(status -> {
            try {
                // 3) store the record in this new transaction
                return repository.save(creator.get());
            } catch (DataIntegrityViolationException e) {
                // another thread or process created this already, possibly
                // between 1) and 2)
                status.setRollbackOnly();
                return null;
            }
        });
        // 4) if we failed to create the record in the second transaction, found will
        // still be null; however, this would happen only if another process
        // created the record. let's see what they made for us!
        if (found == null) {
            found = findUnique.get();
        }
        return found;
    }

    protected Sort convertSort(QPage requestPage) {
        if (requestPage.getSortList() != null && !requestPage.getSortList().isEmpty()) {
            List<Sort.Order> orderList = new ArrayList<>();
            for (QSort requestSort : requestPage.getSortList()) {
                orderList.add(this.convertSort(requestSort));
            }
            return Sort.by(orderList);
        }
        return Sort.unsorted();
    }

    protected Sort.Order convertSort(QSort requestSort) {
        Sort.Direction direction;
        if (requestSort.getOrder().equals(QSort.Order.DESC)) {
            direction = Sort.Direction.DESC;
        } else {
            direction = Sort.Direction.ASC;
        }
        return new Sort.Order(direction, requestSort.getField());
    }

    protected Pageable convertPageable(QPage requestPage) {
        QPageRequest pageRequest = new QPageRequest(requestPage.getPageIndex(), requestPage.getPageSize(), this.convertSort(requestPage));
        pageRequest.setWithoutCountQuery(requestPage.isWithoutCountQuery());
        return pageRequest;
    }

    protected <T> Specification<T> convertSpecification(QRequest request) {
        if (request == null) {
            return null;
        }
        return (root, query, cb) -> {
            // 处理 select
            if (request.getSelectList() != null && !request.getSelectList().isEmpty()) {
                // 使用Selection来为查询字段指定别名
                Selection<?>[] selections = request.getSelectList()
                        .stream()
                        .map(select -> {
                                    Selection<?> selection = root.get(select.getField());
                                    if (select.getAlias() != null) {
                                        selection = selection.alias(select.getAlias());
                                    }
                                    return selection;
                                }
                        )
                        .toArray(Selection[]::new);
                query.multiselect(selections);
            }

            List<Predicate> predicateList = new ArrayList<>();
            if (request.getFilterList() != null && !request.getFilterList().isEmpty()) {
                for (QFilter filter : request.getFilterList()) {
                    Predicate predicate = QFilterKt.toPredicate(filter, root, cb);
                    if (predicate != null) {
                        predicateList.add(predicate);
                    }
                }
            }
            query.distinct(request.isDistinct());
            return cb.and(predicateList.toArray(new Predicate[0]));
        };
    }

    protected <T> QResponse<Q> convertQResponse(Page<T> page) {
        QResponse<Q> apiResponse = new QResponse<>();
        apiResponse.setPage(page.getNumber());
        apiResponse.setPageSize(page.getSize());
        apiResponse.setTotal(page.getTotalElements());
        apiResponse.setPagedData(convertStreamQBeanToList(page.getContent().stream()));

        return apiResponse;
    }

    @Deprecated
    protected <T> Q convertQBean(T object) {
        if (object == null) {
            return null;
        }
        return beanMapperFacade.map(object, getActualType());
    }

    @Deprecated
    protected <T> List<Q> convertStreamQBeanToList(Stream<T> stream) {
        return stream.map(this::convertQBean).collect(Collectors.toList());
    }

    @Deprecated
    protected <T> List<Q> convertQBeanToList(Iterable<T> iterable) {
        return convertStreamQBeanToList(StreamSupport.stream(iterable.spliterator(), false));
    }
}

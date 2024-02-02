package net.sunshow.toolkit.core.qbean.helper.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBean;
import net.sunshow.toolkit.core.qbean.api.enums.Operator;
import net.sunshow.toolkit.core.qbean.api.request.QFilter;
import net.sunshow.toolkit.core.qbean.api.request.QPage;
import net.sunshow.toolkit.core.qbean.api.request.QRequest;
import net.sunshow.toolkit.core.qbean.api.request.QSort;
import net.sunshow.toolkit.core.qbean.api.response.QResponse;
import net.sunshow.toolkit.core.qbean.helper.bean.jpa.QPageRequest;
import net.sunshow.toolkit.core.qbean.helper.entity.BaseEntity;
import net.sunshow.toolkit.core.qbean.helper.repository.BaseRepository;
import nxcloud.foundation.core.bean.mapper.BeanMapperFacade;
import org.apache.commons.lang3.StringUtils;
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

    private Sort.Order convertSort(QSort requestSort) {
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

    @SuppressWarnings("unchecked")
    private <T> Predicate convertPredicate(QFilter filter, Root<T> root, CriteriaBuilder cb) {
        switch (filter.getOperator()) {
            case EQUAL:
                return cb.equal(root.get(filter.getField()), filter.getValue());
            case GREATER_EQUAL:
                if (filter.getValue() instanceof Comparable) {
                    return cb.greaterThanOrEqualTo(root.get(filter.getField()), (Comparable<Object>) filter.getValue());
                } else {
                    logger.error("字段({})不是可比较对象, value={}", filter.getField(), filter.getValue());
                    return null;
                }
            case LESS_EQUAL:
                if (filter.getValue() instanceof Comparable) {
                    return cb.lessThanOrEqualTo(root.get(filter.getField()), (Comparable<Object>) filter.getValue());
                } else {
                    logger.error("字段({})不是可比较对象, value={}", filter.getField(), filter.getValue());
                    return null;
                }
            case GREATER_THAN:
                if (filter.getValue() instanceof Comparable) {
                    return cb.greaterThan(root.get(filter.getField()), (Comparable<Object>) filter.getValue());
                } else {
                    logger.error("字段({})不是可比较对象, value={}", filter.getField(), filter.getValue());
                    return null;
                }
            case LESS_THAN:
                if (filter.getValue() instanceof Comparable) {
                    return cb.lessThan(root.get(filter.getField()), (Comparable<Object>) filter.getValue());
                } else {
                    logger.error("字段({})不是可比较对象, value={}", filter.getField(), filter.getValue());
                    return null;
                }
            case BETWEEN:
                Object val1 = filter.getValueList().get(0);
                Object val2 = filter.getValueList().get(1);
                if (val1 instanceof Comparable && val2 instanceof Comparable) {
                    return cb.between(root.get(filter.getField()), (Comparable<Object>) val1, (Comparable<Object>) val2);
                } else {
                    logger.error("字段({})不是可比较对象, value1={}, value2={}", filter.getField(), val1, val2);
                    return null;
                }
            case IN:
                return root.get(filter.getField()).in(filter.getValueList());
            case LIKE:
                return cb.like(root.get(filter.getField()), filter.getValue().toString());
            case LIKE_ESCAPE:
                return cb.like(root.get(filter.getField()), filter.getValueList().get(0).toString(), (char) filter.getValueList().get(1));
            case NOT_NULL:
                return cb.isNotNull(root.get(filter.getField()));
            case IS_NULL:
                return cb.isNull(root.get(filter.getField()));
            case NOT_EQUAL:
                return cb.notEqual(root.get(filter.getField()), filter.getValue());
            case AND:
            case OR:
                if (filter.getValueList() == null || filter.getValueList().isEmpty()) {
                    return null;
                }
                if (StringUtils.isNotBlank(filter.getField())) {
                    logger.error("OR 和 AND 操作不允许指定 field: {}", filter.getField());
                    return null;
                }
                List<Predicate> predicateList = new ArrayList<>();
                for (Object o : filter.getValueList()) {
                    QFilter f = (QFilter) o;
                    // 递归调用
                    Predicate predicate = convertPredicate(f, root, cb);
                    if (predicate != null) {
                        predicateList.add(predicate);
                    }
                }
                if (!predicateList.isEmpty()) {
                    if (filter.getOperator() == Operator.AND) {
                        return cb.and(predicateList.toArray(new Predicate[0]));
                    }
                    if (filter.getOperator() == Operator.OR) {
                        return cb.or(predicateList.toArray(new Predicate[0]));
                    }
                }
                return null;
            default:
                logger.error("不支持的运算符, op={}", filter.getOperator());
                return null;
        }
    }

    protected <T> Specification<T> convertSpecification(QRequest request) {
        if (request == null) {
            return null;
        }
        return (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            if (request.getFilterList() != null && !request.getFilterList().isEmpty()) {
                for (QFilter filter : request.getFilterList()) {
                    Predicate predicate = convertPredicate(filter, root, cb);
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

    protected <T> Q convertQBean(T object) {
        if (object == null) {
            return null;
        }
        return beanMapperFacade.map(object, getActualType());
    }

    protected <T> List<Q> convertStreamQBeanToList(Stream<T> stream) {
        return stream.map(this::convertQBean).collect(Collectors.toList());
    }

    protected <T> List<Q> convertQBeanToList(Iterable<T> iterable) {
        return convertStreamQBeanToList(StreamSupport.stream(iterable.spliterator(), false));
    }
}

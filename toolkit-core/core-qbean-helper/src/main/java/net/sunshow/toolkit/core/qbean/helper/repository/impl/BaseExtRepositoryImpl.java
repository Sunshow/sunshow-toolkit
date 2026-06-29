package net.sunshow.toolkit.core.qbean.helper.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import net.sunshow.toolkit.core.qbean.helper.bean.jpa.QPageRequest;
import net.sunshow.toolkit.core.qbean.helper.repository.BaseExtRepository;
import nxcloud.foundation.core.data.jpa.repository.support.AdvancedJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author qatang
 */
public class BaseExtRepositoryImpl<T, ID extends Serializable> extends AdvancedJpaRepository<T, ID> implements BaseExtRepository<T, ID> {
    private final EntityManager entityManager;

    public BaseExtRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager em) {
        super(entityInformation, em);
        this.entityManager = em;
    }

    public BaseExtRepositoryImpl(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        this.entityManager = em;
    }

    @Override
    public T findByIdForUpdate(ID id) {
        return entityManager.find(this.getDomainClass(), id, LockModeType.PESSIMISTIC_WRITE);
    }

    @Override
    protected <S extends T> Page<S> readPage(TypedQuery<S> query, final Class<S> domainClass, Pageable pageable,
                                             @Nullable Specification<S> spec) {
        if (pageable instanceof QPageRequest) {
            if (((QPageRequest) pageable).isWithoutCountQuery()) {
                if (pageable.isPaged()) {
                    query.setFirstResult((int) pageable.getOffset());
                    query.setMaxResults(pageable.getPageSize());
                }

                List<S> content = query.getResultList();

                return new PageImpl<>(content, pageable, content.size());
            }
        }
        return super.readPage(query, domainClass, pageable, spec);
    }

    @Override
    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        TypedQuery<T> query = getQuery(spec, pageable);
        return readPage(query, getDomainClass(), pageable, spec);
    }

    @Override
    public void clear() {
        entityManager.clear();
    }

    @Override
    public <S extends T> void detach(S s) {
        entityManager.detach(s);
    }

    @Override
    public <S extends T> void detach(Iterable<S> iterable) {
        for (S s : iterable) {
            detach(s);
        }
    }

    @Override
    public List<T> findAllByIdIn(Iterable<ID> ids) {
        return findAllById(ids);
    }

    @Override
    public <S extends T> void refresh(S s) {
        entityManager.refresh(s);
    }

    @Override
    public int casUpdate(Map<String, Object> setProperties, Map<String, Object> whereProperties) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<T> criteriaUpdate = cb.createCriteriaUpdate(getDomainClass());
        Root<T> root = criteriaUpdate.from(getDomainClass());

        for (Map.Entry<String, Object> entry : setProperties.entrySet()) {
            criteriaUpdate.set(root.get(entry.getKey()), entry.getValue());
        }

        Predicate where = null;
        for (Map.Entry<String, Object> entry : whereProperties.entrySet()) {
            Predicate predicate;
            if (entry.getValue() == null) {
                predicate = cb.isNull(root.get(entry.getKey()));
            } else {
                predicate = cb.equal(root.get(entry.getKey()), entry.getValue());
            }
            if (where == null) {
                where = predicate;
            } else {
                where = cb.and(where, predicate);
            }
        }
        criteriaUpdate.where(where);

        return entityManager.createQuery(criteriaUpdate).executeUpdate();
    }

    @Override
    public boolean existsByConditions(Map<String, Object> conditions) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(getDomainClass());
        query.select(cb.count(root));

        Predicate where = null;
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            Predicate predicate;
            if (entry.getValue() == null) {
                predicate = cb.isNull(root.get(entry.getKey()));
            } else {
                predicate = cb.equal(root.get(entry.getKey()), entry.getValue());
            }
            where = (where == null) ? predicate : cb.and(where, predicate);
        }
        query.where(where);

        return entityManager.createQuery(query).getSingleResult() > 0;
    }

}

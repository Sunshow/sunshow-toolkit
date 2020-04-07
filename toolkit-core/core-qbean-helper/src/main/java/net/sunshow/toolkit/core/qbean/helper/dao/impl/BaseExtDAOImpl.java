package net.sunshow.toolkit.core.qbean.helper.dao.impl;

import net.sunshow.toolkit.core.qbean.helper.bean.jpa.QPageRequest;
import net.sunshow.toolkit.core.qbean.helper.dao.BaseExtDAO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.lang.Nullable;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.List;

/**
 * @author qatang
 */
public class BaseExtDAOImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements BaseExtDAO<T, ID> {
    private final EntityManager entityManager;

    public BaseExtDAOImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager em) {
        super(entityInformation, em);
        this.entityManager = em;
    }

    public BaseExtDAOImpl(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        this.entityManager = em;
    }

    @Override
    public T findOneForUpdate(ID id) {
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
}

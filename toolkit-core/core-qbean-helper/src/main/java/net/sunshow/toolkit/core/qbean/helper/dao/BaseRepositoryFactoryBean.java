package net.sunshow.toolkit.core.qbean.helper.dao;

import net.sunshow.toolkit.core.qbean.helper.dao.impl.BaseExtDAOImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * @author sunshow
 */
public class BaseRepositoryFactoryBean<R extends JpaRepository<T, I>, T,
        I extends Serializable> extends JpaRepositoryFactoryBean<R, T, I> {

    public BaseRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
        super(repositoryInterface);
    }

    protected RepositoryFactorySupport createRepositoryFactory(EntityManager em) {
        return new BaseRepositoryFactory(em);
    }

    private static class BaseRepositoryFactory<T, I extends Serializable>
            extends JpaRepositoryFactory {

        public BaseRepositoryFactory(EntityManager em) {
            super(em);
        }

        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            return BaseExtDAOImpl.class;
        }
    }

}

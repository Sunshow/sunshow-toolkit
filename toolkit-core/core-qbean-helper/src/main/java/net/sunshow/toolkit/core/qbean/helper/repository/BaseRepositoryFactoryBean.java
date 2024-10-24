package net.sunshow.toolkit.core.qbean.helper.repository;

import jakarta.persistence.EntityManager;
import net.sunshow.toolkit.core.qbean.helper.repository.impl.BaseExtRepositoryImpl;
import nxcloud.foundation.core.data.jpa.repository.support.AdvancedJpaRepositoryFactory;
import nxcloud.foundation.core.data.jpa.repository.support.AdvancedJpaRepositoryFactoryBean;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

/**
 * @author sunshow
 */
public class BaseRepositoryFactoryBean<R extends JpaRepository<T, I>, T,
        I extends Serializable> extends AdvancedJpaRepositoryFactoryBean<R, T, I> {

    public BaseRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
        super(repositoryInterface);
    }

    @NotNull
    protected RepositoryFactorySupport createRepositoryFactory(@NotNull EntityManager em) {
        return new BaseRepositoryFactory<>(em);
    }

    private static class BaseRepositoryFactory<T, I extends Serializable>
            extends AdvancedJpaRepositoryFactory {

        public BaseRepositoryFactory(EntityManager em) {
            super(em);
        }

        @NotNull
        @Override
        protected Class<?> getRepositoryBaseClass(@NotNull RepositoryMetadata metadata) {
            return BaseExtRepositoryImpl.class;
        }
    }

}

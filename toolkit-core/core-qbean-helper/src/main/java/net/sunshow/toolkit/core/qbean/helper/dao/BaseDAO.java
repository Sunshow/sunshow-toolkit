package net.sunshow.toolkit.core.qbean.helper.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * @author qatang
 */
@NoRepositoryBean
public interface BaseDAO<T, ID extends Serializable> extends BaseExtDAO<T, ID>, JpaSpecificationExecutor<T> {
}

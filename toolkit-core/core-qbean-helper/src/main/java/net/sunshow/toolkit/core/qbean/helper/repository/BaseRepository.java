package net.sunshow.toolkit.core.qbean.helper.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * @author qatang
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends BaseExtRepository<T, ID>, CrudRepository<T, ID>, JpaSpecificationExecutor<T> {

}

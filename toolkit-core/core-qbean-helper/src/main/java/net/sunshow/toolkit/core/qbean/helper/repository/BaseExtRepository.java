package net.sunshow.toolkit.core.qbean.helper.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;
import java.util.List;

/**
 * @author qatang
 */
@NoRepositoryBean
public interface BaseExtRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {

    void flush();

    <S extends T> S saveAndFlush(S s);

    /**
     * 锁行读写的方式通过ID获取对象
     *
     * @param id 对象ID
     * @return 对应的对象
     */
    T findByIdForUpdate(ID id);

    /**
     * 不带 count 执行分页查询, 此时返回的 total 数即本次查询结果数量
     * Returns a {@link Page} of entities matching the given {@link Specification}.
     *
     * @param spec
     * @param pageable
     * @return
     */
    Page<T> findAll(Specification<T> spec, Pageable pageable);

    /**
     * 清除所有被entityManager管理的entity
     * 操作结果等于detachAll
     * 警告: 不只是清除当前类型的entity, 所有已经载入的entity都会被清除, 除非你知道在干什么, 否则不要调用此方法
     */
    void clear();

    /**
     * 将指定entity解除entityManager管理
     *
     * @param s 被管理的entity
     */
    <S extends T> void detach(S s);

    /**
     * 批量detach操作
     *
     * @param iterable 可遍历的entity集合
     * @param <S>      entity类型
     */
    <S extends T> void detach(Iterable<S> iterable);

    /**
     * 按主键批量查找
     *
     * @param ids 可遍历的主键集合
     * @return 找到的实体对象列表
     */
    List<T> findAllByIdIn(Iterable<ID> ids);
}

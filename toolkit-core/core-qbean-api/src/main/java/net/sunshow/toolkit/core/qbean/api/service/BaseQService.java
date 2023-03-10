package net.sunshow.toolkit.core.qbean.api.service;

import net.sunshow.toolkit.core.qbean.api.bean.BaseQBean;
import net.sunshow.toolkit.core.qbean.api.request.QPage;
import net.sunshow.toolkit.core.qbean.api.request.QRequest;
import net.sunshow.toolkit.core.qbean.api.response.QResponse;
import net.sunshow.toolkit.core.qbean.api.search.PageSearch;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BaseQService<Q extends BaseQBean, ID extends Serializable> {

    Optional<Q> getById(ID id);

    Q getByIdEnsure(ID id);

    List<Q> findByIdCollection(Collection<ID> idCollection);

    QResponse<Q> findAll(QRequest request, QPage requestPage);

    QResponse<Q> search(PageSearch search);

    Q save(Object creator);

    Q update(Object updater);

    Q update(ID id, Object updater);

    void deleteById(ID id);

}

package net.sunshow.toolkit.core.qbean.api.service;

import net.sunshow.toolkit.core.qbean.api.bean.BaseQBean;

import java.io.Serializable;
import java.util.Optional;

public interface BaseQService<Q extends BaseQBean, ID extends Serializable> {

    Optional<Q> getById(ID id);

    Q getByIdEnsure(ID id);

}

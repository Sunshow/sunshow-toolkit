package net.sunshow.toolkit.core.qbean.api.bean;

import java.io.Serializable;
import java.util.Set;

/**
 * QBean 的更新器
 *
 * @author sunshow
 */
public interface BaseQBeanUpdater extends Serializable {

    Serializable getUpdateId();

    Set<String> getUpdateProperties();
}

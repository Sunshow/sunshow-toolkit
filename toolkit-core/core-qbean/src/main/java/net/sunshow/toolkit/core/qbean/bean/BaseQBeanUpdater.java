package net.sunshow.toolkit.core.qbean.bean;

import java.io.Serializable;
import java.util.Set;

/**
 * QBean 的更新器
 *
 * @author sunshow
 */
public interface BaseQBeanUpdater extends Serializable {

    Long getUpdateId();

    Set<String> getUpdateProperties();
}

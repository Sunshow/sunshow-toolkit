package net.sunshow.toolkit.core.qbean.api.bean;

import java.io.Serializable;
import java.util.Set;

/**
 * QBean 的创建器
 *
 * @author sunshow
 */
public interface BaseQBeanCreator extends Serializable {

    Set<String> getCreateProperties();

}

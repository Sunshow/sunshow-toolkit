package net.sunshow.toolkit.core.qbean.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 用于生成更新器, 标注于类型则说明全部属性可更新
 *
 * @author sunshow
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Documented
public @interface QBeanUpdater {
}

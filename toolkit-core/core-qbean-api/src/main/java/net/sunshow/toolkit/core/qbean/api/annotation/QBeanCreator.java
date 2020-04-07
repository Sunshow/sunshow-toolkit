package net.sunshow.toolkit.core.qbean.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 用于生成创建器, 标注于类型则说明全部属性可创建初始化
 *
 * @author sunshow
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Documented
public @interface QBeanCreator {
}

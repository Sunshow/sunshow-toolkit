package net.sunshow.toolkit.core.qbean.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author sunshow
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Documented
public @interface QBeanCreatorIgnore {
}

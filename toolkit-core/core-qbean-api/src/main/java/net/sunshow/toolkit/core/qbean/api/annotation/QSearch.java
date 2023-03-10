package net.sunshow.toolkit.core.qbean.api.annotation;

import java.lang.annotation.*;

/**
 * 用于标记查询条件定义
 *
 * @author sunshow
 */
@Target({ElementType.FIELD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface QSearch {

    Class<?> definition() default Void.class;
    
}

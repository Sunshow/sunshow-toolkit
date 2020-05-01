package net.sunshow.toolkit.core.qbean.api.annotation;

import net.sunshow.toolkit.core.qbean.api.enums.Operator;

import java.lang.annotation.*;

/**
 * 用于标记某个类或者某个属性是QBean查询字段
 *
 * @author sunshow
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface QField {

    // 仅在标记在属性上时有效
    String fieldName() default "";

    Operator operator() default Operator.EQUAL;

}

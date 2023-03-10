package net.sunshow.toolkit.core.qbean.api.annotation;

import net.sunshow.toolkit.core.qbean.api.enums.Control;
import net.sunshow.toolkit.core.qbean.api.enums.Operator;
import net.sunshow.toolkit.core.qbean.api.enums.Wildcard;

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

    boolean searchable() default true;

    boolean sortable() default false;

    // 默认排序字段
    boolean defaultSort() default false;

    int sortPriority() default 0;

    // 仅在标记在属性上时有效
    String name() default "";

    Operator operator() default Operator.EQUAL;

    // 仅在操作符是 Like 时有效
    Wildcard wildcard() default Wildcard.BOTH;

    Control control() default Control.INPUT;

    boolean emptyAsNull() default true;

    String label() default "";

    String placeholder() default "";

    String ref() default "";

    String refId() default "id";

    String refName() default "name";

    String template() default "";

    String order() default "ASC";
}

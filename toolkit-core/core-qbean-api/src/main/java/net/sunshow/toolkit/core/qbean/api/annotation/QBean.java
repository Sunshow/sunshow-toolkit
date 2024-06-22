package net.sunshow.toolkit.core.qbean.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 声明该Bean用于API请求, 供注解处理器生成代码
 *
 * @author sunshow
 */
@Target({ElementType.TYPE})
@Documented
public @interface QBean {

    /**
     * 未找到 QBeanID 注解时是否自动添加默认ID属性
     */

    boolean defaultIdProperty() default true;

    String defaultIdPropertyName() default "id";

    Class<?> defaultIdPropertyType() default Long.class;

    boolean defaultIdPropertyCreatorIgnore() default true;

}

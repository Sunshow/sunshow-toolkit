package net.sunshow.toolkit.core.qbean.helper.component.request;

import net.sunshow.toolkit.core.qbean.api.annotation.QField;
import net.sunshow.toolkit.core.qbean.api.request.QRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * author: sunshow.
 */
public final class QBeanSearchHelper {

    private final static Logger logger = LoggerFactory.getLogger(QBeanSearchHelper.class);

    /**
     * 通过解析 @QField 注解来生成请求对象
     *
     * @param object 自定义对象
     * @return 请求对象
     */
    public static QRequest convertQRequest(Object object) {
        QRequest request = QRequest.newInstance();

        Class<?> clazz = object.getClass();
        // 先看类上有没有注解
        QField classAnnotation = clazz.getAnnotation(QField.class);

        // 反射每个属性逐个处理
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);

            // 看属性上有没有注解 有则优先使用属性上的
            QField fieldAnnotation = field.getAnnotation(QField.class);
            try {
                if (fieldAnnotation != null) {
                    Object value = field.get(object);
                    if (value == null) {
                        continue;
                    }
                    String name = field.getName();
                    if (StringUtils.isNotEmpty(fieldAnnotation.fieldName())) {
                        name = fieldAnnotation.fieldName();
                    }
                    request.filter(fieldAnnotation.operator(), name, value);
                } else if (classAnnotation != null) {
                    Object value = field.get(object);
                    if (value == null) {
                        continue;
                    }
                    request.filter(classAnnotation.operator(), field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                logger.error("解析属性拼装请求出错", e);
            }
        }
        return request;
    }
}

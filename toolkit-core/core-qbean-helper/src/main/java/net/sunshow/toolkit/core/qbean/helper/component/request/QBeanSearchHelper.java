package net.sunshow.toolkit.core.qbean.helper.component.request;

import net.sunshow.toolkit.core.qbean.api.annotation.QField;
import net.sunshow.toolkit.core.qbean.api.enums.Operator;
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

            String fieldName = field.getName();

            // 看属性上有没有注解 有则优先使用属性上的
            QField annotation = field.getAnnotation(QField.class);
            if (annotation == null) {
                annotation = classAnnotation;
            } else {
                if (StringUtils.isNotEmpty(annotation.fieldName())) {
                    fieldName = annotation.fieldName();
                }
            }

            if (annotation == null) {
                continue;
            }

            try {
                Object value = field.get(object);
                if (value == null) {
                    // 将来如果要支持 null 值的查询可以在 QField 注解上进行扩展
                    continue;
                }

                // 对 LIKE 进行预处理
                if (annotation.operator() == Operator.LIKE || annotation.operator() == Operator.LIKE_ESCAPE) {
                    // ESCAPE 暂未做完整支持
                    String search = value.toString();
                    switch (annotation.wildcard()) {
                        case BOTH:
                            search = "%" + search + "%";
                            break;
                        case PREFIX:
                            search = "%" + search;
                            break;
                        case SUFFIX:
                            search = search + "%";
                            break;
                    }
                    request.filter(annotation.operator(), fieldName, search);
                } else {
                    request.filter(annotation.operator(), fieldName, value);
                }
            } catch (IllegalAccessException e) {
                logger.error("解析属性拼装请求出错", e);
            }
        }
        return request;
    }
}

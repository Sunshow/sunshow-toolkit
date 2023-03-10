package net.sunshow.toolkit.core.qbean.helper.component.request;

import net.sunshow.toolkit.core.qbean.api.annotation.QField;
import net.sunshow.toolkit.core.qbean.api.enums.Operator;
import net.sunshow.toolkit.core.qbean.api.request.QFieldDef;
import net.sunshow.toolkit.core.qbean.api.request.QRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

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
                if (StringUtils.isNotEmpty(annotation.name())) {
                    fieldName = annotation.name();
                }
            }

            if (annotation == null) {
                continue;
            }

            if (!annotation.searchable()) {
                continue;
            }

            try {
                Object value = field.get(object);
                if (value == null) {
                    // 将来如果要支持 null 值的查询可以在 QField 注解上进行扩展
                    continue;
                }
                if (annotation.emptyAsNull() && value.equals("")) {
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
                } else if (annotation.operator() == Operator.BETWEEN) {
                    if (!object.getClass().isArray()) {
                        logger.error("Between 操作必须传入数组, 忽略");
                        continue;
                    }
                    Object[] array = (Object[]) object;
                    if (array.length != 2) {
                        logger.error("Between 操作必须传入数组有且必须包含两个元素, 忽略");
                        continue;
                    }

                    request.filterBetween(fieldName, array[0], array[1]);
                } else {
                    request.filter(annotation.operator(), fieldName, value);
                }
            } catch (IllegalAccessException e) {
                logger.error("解析属性拼装请求出错", e);
            }
        }
        return request;
    }

    private static List<QFieldDef> convertQFieldDefList(Class<?> clazz, Predicate<QField> predicate) {
        List<QFieldDef> defList = new ArrayList<>();

        // 先看类上有没有注解
        QField classAnnotation = clazz.getAnnotation(QField.class);

        // 反射每个属性逐个处理
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);

            // 看属性上有没有注解 有则优先使用属性上的
            QField annotation = field.getAnnotation(QField.class);
            if (annotation == null) {
                annotation = classAnnotation;
            }

            if (annotation == null) {
                continue;
            }

            if (predicate != null && !predicate.test(annotation)) {
                continue;
            }

            QFieldDef def = new QFieldDef();
            def.setName(field.getName());
            def.setAliasName(annotation.name());
            def.setControl(annotation.control());
            def.setLabel(annotation.label());
            def.setPlaceholder(annotation.placeholder());
            def.setRef(annotation.ref());
            def.setRefId(annotation.refId());
            def.setRefName(annotation.refName());
            def.setTemplate(annotation.template());
            def.setOrder(annotation.order());
            def.setSearchable(annotation.searchable());
            def.setSortable(annotation.sortable());
            def.setDefaultSort(annotation.defaultSort());
            def.setOperator(annotation.operator());
            def.setWildcard(annotation.wildcard());
            def.setSortPriority(annotation.sortPriority());

            if (StringUtils.isEmpty(def.getLabel()) && StringUtils.isNotEmpty(def.getPlaceholder())) {
                def.setLabel(def.getPlaceholder());
            }

            defList.add(def);
        }

        return defList;
    }

    public static List<QFieldDef> convertSearchQFieldDefList(Class<?> clazz) {
        return convertQFieldDefList(clazz, QField::searchable);
    }

    public static List<QFieldDef> convertSortQFieldDefList(Class<?> clazz) {
        return convertQFieldDefList(clazz, QField::sortable);
    }

    public static List<QFieldDef> convertQFieldDefList(Class<?> clazz) {
        return convertQFieldDefList(clazz, null);
    }
}

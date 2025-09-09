package net.sunshow.toolkit.core.qbean.helper.component.request;

import net.sunshow.toolkit.core.qbean.api.bean.BaseQBean;
import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanCreator;
import net.sunshow.toolkit.core.qbean.helper.entity.BaseEntity;
import net.sunshow.toolkit.core.qbean.helper.utils.TypeCompatibilityUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * author: sunshow.
 */
public final class QBeanCreatorHelper {

    private final static Logger logger = LoggerFactory.getLogger(QBeanCreatorHelper.class);

    public static <Q extends BaseQBean, E extends BaseEntity, S extends BaseQBeanCreator<Q>> E copyCreatorField(E entity, S creator) {
        if (creator != null) {
            Set<String> createProperties = creator.getCreateProperties();
            if (createProperties != null) {
                for (String fieldName : createProperties) {
                    try {
                        Object fieldValue = PropertyUtils.getProperty(creator, fieldName);

                        BeanUtils.setProperty(entity, fieldName, fieldValue);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        logger.error("类属性拷贝错误, class={}, fieldName={}", creator.getClass(), fieldName);
                    }
                }

            }
        }
        return entity;
    }

    public static <Q extends BaseQBean, CreatorBuilder, Creator extends BaseQBeanCreator<Q>, PropertiesSource> void copyPropertiesToCreatorBuilder(CreatorBuilder builder, Class<Creator> creatorType, PropertiesSource source) {
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(creatorType);
        Map<String, List<Method>> methodNameListMap = Arrays.stream(builder.getClass().getMethods())
                .collect(Collectors.groupingBy(Method::getName));
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String fieldName = propertyDescriptor.getName();
            if (StringUtils.equalsAny(fieldName, "createProperties", "class")) {
                continue;
            }
            try {
                Object fieldValue = PropertyUtils.getProperty(source, fieldName);
                if (fieldValue != null) {
                    // 反射调用 builder 的 withXXX 方法
                    String methodName = "with" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                    if (methodNameListMap.containsKey(methodName)) {
                        Optional<Method> methodOptional = methodNameListMap.get(methodName).stream()
                                .filter(method -> method.getParameterCount() == 1)
                                .filter(method -> TypeCompatibilityUtils.isAssignableFrom(method.getParameterTypes()[0], fieldValue.getClass()))
                                .findAny();
                        if (methodOptional.isPresent()) {
                            Method method = methodOptional.get();
                            method.invoke(builder, fieldValue);
                        }
                    }
                }
            } catch (IllegalAccessException | NoSuchMethodException e) {
                // did nothing
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                logger.error("类属性拷贝错误, message={}, fieldName={}", e.getMessage(), fieldName);
            }
        }
    }
}

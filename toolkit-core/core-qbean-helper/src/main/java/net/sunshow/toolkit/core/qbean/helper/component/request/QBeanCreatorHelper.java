package net.sunshow.toolkit.core.qbean.helper.component.request;

import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanCreator;
import net.sunshow.toolkit.core.qbean.helper.entity.BaseEntity;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * author: sunshow.
 */
public final class QBeanCreatorHelper {

    private final static Logger logger = LoggerFactory.getLogger(QBeanCreatorHelper.class);

    public static <E extends BaseEntity, S extends BaseQBeanCreator> E copyCreatorField(E entity, S creator) {
        if (creator != null) {
            Set<String> createProperties = creator.getCreateProperties();
            if (createProperties != null) {
                try {
                    for (String fieldName : createProperties) {
                        Object fieldValue = PropertyUtils.getProperty(creator, fieldName);

                        BeanUtils.setProperty(entity, fieldName, fieldValue);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(String.format("类属性拷贝错误, message=%s, class=%s", e.getMessage(), creator));
                }
            }
        }
        return entity;
    }

    public static <CreatorBuilder, Creator extends BaseQBeanCreator, PropertiesSource> void copyPropertiesToCreatorBuilder(CreatorBuilder builder, Class<Creator> creatorType, PropertiesSource source) {
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(creatorType);
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
                    Method method = builder.getClass().getMethod(methodName, fieldValue.getClass());
                    method.invoke(builder, fieldValue);
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

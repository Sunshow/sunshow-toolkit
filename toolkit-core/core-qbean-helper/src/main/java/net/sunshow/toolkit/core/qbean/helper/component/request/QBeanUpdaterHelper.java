package net.sunshow.toolkit.core.qbean.helper.component.request;

import net.sunshow.toolkit.core.qbean.api.bean.BaseQBeanUpdater;
import net.sunshow.toolkit.core.qbean.helper.entity.BaseEntity;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * author: sunshow.
 */
public final class QBeanUpdaterHelper {

    private final static Logger logger = LoggerFactory.getLogger(QBeanUpdaterHelper.class);

    public static <E extends BaseEntity, S extends BaseQBeanUpdater> E copyUpdaterField(E entity, S updater) {
        if (updater != null) {
            Set<String> updateProperties = updater.getUpdateProperties();
            if (updateProperties != null) {
                try {
                    for (String fieldName : updateProperties) {
                        Object fieldValue = PropertyUtils.getProperty(updater, fieldName);

                        BeanUtils.setProperty(entity, fieldName, fieldValue);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(String.format("类属性拷贝错误, message=%s, class=%s", e.getMessage(), updater));
                }
            }
        }
        return entity;
    }
}

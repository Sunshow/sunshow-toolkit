package net.sunshow.toolkit.core.base.enums.converter;

import jakarta.persistence.AttributeConverter;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

public abstract class BaseEnumConverter<T> implements AttributeConverter<T, Integer>, Converter<String, T> {

    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("unchecked")
    protected Class<T> getActualType() {
        ParameterizedType paramType = (ParameterizedType) this.getClass().getGenericSuperclass();

        return (Class<T>) paramType.getActualTypeArguments()[0];
    }

    @Override
    public Integer convertToDatabaseColumn(T type) {
        try {
            return Integer.valueOf(BeanUtils.getProperty(type, "value"));
        } catch (Exception e) {
            logger.error("自定义enum序列化出错, value={}", type);
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T convertToEntityAttribute(Integer integer) {
        Method getMethod = ReflectionUtils.findMethod(this.getActualType(), "get", int.class);
        if (getMethod == null) {
            logger.error("未找到转换方法");
            return null;
        }
        return (T) ReflectionUtils.invokeMethod(getMethod, null, integer);
    }

    @Override
    public T convert(String s) {
        return this.convertToEntityAttribute(Integer.parseInt(s));
    }

}

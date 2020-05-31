package net.sunshow.toolkit.core.base.enums.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 * 自定义enum使用value反序列化
 *
 * @author sunshow
 */
public abstract class CustomValueEnumDeserializer<T> extends JsonDeserializer<T> {
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("unchecked")
    protected Class<T> getActualType() {
        ParameterizedType paramType = (ParameterizedType) this.getClass().getGenericSuperclass();

        return (Class<T>) paramType.getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken currentToken = p.getCurrentToken();
        Integer value = null;
        if (currentToken == JsonToken.VALUE_NUMBER_INT) {
            value = p.getIntValue();
        } else if (currentToken == JsonToken.VALUE_STRING) {
            String s = p.getValueAsString();
            try {
                value = Integer.valueOf(s);
            } catch (NumberFormatException e) {
                logger.error("解析自定义enum出错, 无效的值: " + s, e);
            }
        }

        if (value != null) {
            Method getMethod = ReflectionUtils.findMethod(this.getActualType(), reflectValueMethodName(), int.class);
            if (getMethod == null) {
                logger.error("未找到反序列化方法");
                return null;
            }
            return (T) ReflectionUtils.invokeMethod(getMethod, null, value);
        }

        return null;
    }

    public String reflectValueMethodName() {
        return "get";
    }
}

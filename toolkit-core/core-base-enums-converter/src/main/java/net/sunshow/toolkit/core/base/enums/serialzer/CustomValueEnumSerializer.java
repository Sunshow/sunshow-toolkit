package net.sunshow.toolkit.core.base.enums.serialzer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 自定义enum使用value序列化
 *
 * @author sunshow
 */
public class CustomValueEnumSerializer<T> extends JsonSerializer<T> {

    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            gen.writeNumber(Integer.parseInt(BeanUtils.getProperty(value, "value")));
        } catch (Exception e) {
            logger.error("自定义enum序列化出错, value={}", value);
            logger.error(e.getMessage(), e);
        }
    }
}
